#!/usr/bin/env bash
# 엄격 모드입니다.
# -e: 명령 실패 시 즉시 종료, -u: unset 변수 사용 금지, pipefail: pipeline 중간 실패 감지.
# 의도: 배포 중 애매하게 계속 진행되는 상황을 막습니다.
set -euo pipefail

# CD workflow가 SSM payload를 만들 때 export해서 넘겨주는 필수 값입니다.
# 값이 없으면 배포를 시작하지 않고 즉시 실패합니다.
: "${AWS_REGION:?AWS_REGION is required}"
: "${ECR_REGISTRY:?ECR_REGISTRY is required}"
: "${IMAGE_URI:?IMAGE_URI is required}"
: "${IMAGE_TAG:?IMAGE_TAG is required}"
: "${DEPLOY_DIR:?DEPLOY_DIR is required}"

# backend와 nginx가 붙을 Docker network 이름입니다.
# 같은 network에 있어야 nginx가 container name으로 backend에 접근할 수 있습니다.
NETWORK_NAME="community-backend"
# 트래픽 전환을 담당하는 nginx 컨테이너 이름입니다.
NGINX_CONTAINER_NAME="community-nginx"
# blue/green 적용 전 기존 단일 컨테이너 배포에서 쓰던 이름입니다.
# 첫 blue/green 배포 성공 후 정리 대상입니다.
OLD_SINGLE_CONTAINER_NAME="community-backend"
# backend 컨테이너들이 공유하는 이미지 저장 volume입니다.
IMAGE_VOLUME_NAME="community_backend_images"
# 현재 active 색상(blue/green)을 저장하는 파일입니다.
ACTIVE_COLOR_FILE="${DEPLOY_DIR}/.active-color"
# nginx 설정 파일을 EC2 배포 디렉터리에 생성해 컨테이너에 mount/copy합니다.
NGINX_CONF_DIR="${DEPLOY_DIR}/nginx/conf.d"
NGINX_CONF_FILE="${NGINX_CONF_DIR}/default.conf"

# blue 컨테이너의 host health check 포트입니다.
BLUE_HOST_PORT="${BLUE_HOST_PORT:-8081}"
# green 컨테이너의 host health check 포트입니다.
GREEN_HOST_PORT="${GREEN_HOST_PORT:-8082}"
# 외부 사용자가 접근하는 nginx host port입니다.
PUBLIC_HTTP_PORT="${PUBLIC_HTTP_PORT:-80}"
# Spring Boot actuator health endpoint입니다.
HEALTH_PATH="${HEALTH_PATH:-/actuator/health}"
# nginx 전환 후 old 컨테이너 제거 전 대기 시간입니다.
# 의도: 기존 연결이 마무리될 짧은 시간을 줍니다.
SWITCH_GRACE_SECONDS="${SWITCH_GRACE_SECONDS:-10}"
# EC2 로컬에 남길 backend image 개수입니다.
# ECR이 원본 저장소이므로 EC2에는 최근 이미지 일부만 보관합니다.
KEEP_BACKEND_IMAGE_COUNT="${KEEP_BACKEND_IMAGE_COUNT:-5}"

# 배포 디렉터리와 nginx 설정 디렉터리를 보장합니다.
mkdir -p "${DEPLOY_DIR}" "${NGINX_CONF_DIR}"
# 이후 상대 경로 작업은 배포 디렉터리 기준으로 수행합니다.
cd "${DEPLOY_DIR}"

# 색상 값을 컨테이너 이름으로 변환합니다.
# 예: blue -> community-backend-blue
container_name() {
  printf 'community-backend-%s' "$1"
}

# 색상별 host port를 반환합니다.
# health check는 nginx 전환 전에도 접근 가능해야 하므로 localhost port를 사용합니다.
host_port() {
  case "$1" in
    blue) printf '%s' "${BLUE_HOST_PORT}" ;;
    green) printf '%s' "${GREEN_HOST_PORT}" ;;
    *) echo "Unknown color: $1" >&2; exit 1 ;;
  esac
}

# 현재 active 색상의 반대 색상을 반환합니다.
# active가 없으면 첫 배포는 blue부터 시작합니다.
opposite_color() {
  case "$1" in
    blue) printf 'green' ;;
    green) printf 'blue' ;;
    none) printf 'blue' ;;
    *) echo "Unknown color: $1" >&2; exit 1 ;;
  esac
}

# 특정 컨테이너가 실행 중인지 확인합니다.
# docker inspect 실패는 false로 처리합니다.
is_running() {
  [ "$(docker inspect -f '{{.State.Running}}' "$1" 2>/dev/null || true)" = "true" ]
}

# 현재 active 색상을 결정합니다.
# 1순위: .active-color 파일
# 2순위: 실제 실행 중인 blue/green 컨테이너 탐색
# 의도: 상태 파일이 없어도 기존 컨테이너 상태를 바탕으로 복구 가능하게 합니다.
detect_active_color() {
  if [ -f "${ACTIVE_COLOR_FILE}" ]; then
    active_color="$(tr -d '[:space:]' < "${ACTIVE_COLOR_FILE}")"
    if [ "${active_color}" = "blue" ] || [ "${active_color}" = "green" ]; then
      if is_running "$(container_name "${active_color}")"; then
        printf '%s' "${active_color}"
        return
      fi
    fi
  fi

  if is_running "$(container_name blue)"; then
    printf 'blue'
  elif is_running "$(container_name green)"; then
    printf 'green'
  else
    printf 'none'
  fi
}

# nginx가 바라볼 backend 컨테이너를 설정 파일로 생성합니다.
# 이 파일은 blue/green 전환 시 proxy_pass 대상만 바뀝니다.
write_nginx_config() {
  target_container="$(container_name "$1")"

  cat > "${NGINX_CONF_FILE}" <<EOF
server {
    listen 80;
    server_name _;

    client_max_body_size 10m;

    location / {
        proxy_pass http://${target_container}:8080;
        proxy_http_version 1.1;

        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_set_header X-Forwarded-Host \$host;
        proxy_set_header X-Forwarded-Port \$server_port;

        proxy_connect_timeout 5s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
}
EOF
}

# nginx 컨테이너를 보장하고 설정 문법을 검사합니다.
# 기존 nginx가 있으면 config만 교체하고, 없으면 새로 띄웁니다.
ensure_nginx() {
  target_color="$1"
  write_nginx_config "${target_color}"

  if docker inspect "${NGINX_CONTAINER_NAME}" >/dev/null 2>&1; then
    # 이미 있는 nginx를 backend network에 연결합니다. 이미 연결되어 있으면 무시합니다.
    docker network connect "${NETWORK_NAME}" "${NGINX_CONTAINER_NAME}" >/dev/null 2>&1 || true
    # stop 상태일 수도 있으므로 start를 보장합니다.
    docker start "${NGINX_CONTAINER_NAME}" >/dev/null
    # 실행 중인 컨테이너에 새 설정을 복사합니다.
    docker cp "${NGINX_CONF_FILE}" "${NGINX_CONTAINER_NAME}:/etc/nginx/conf.d/default.conf"
  else
    # nginx가 없으면 새로 실행합니다.
    # 설정 디렉터리는 read-only로 mount합니다.
    docker run -d \
      --name "${NGINX_CONTAINER_NAME}" \
      --restart unless-stopped \
      --network "${NETWORK_NAME}" \
      -p "${PUBLIC_HTTP_PORT}:80" \
      -v "${NGINX_CONF_DIR}:/etc/nginx/conf.d:ro" \
      nginx:1.27-alpine >/dev/null
  fi

  # reload 전에 설정 문법을 검증합니다.
  docker exec "${NGINX_CONTAINER_NAME}" nginx -t
}

# nginx를 target color로 전환합니다.
# reload 실패 시 previous color로 되돌리는 best-effort rollback을 수행합니다.
reload_nginx_to() {
  target_color="$1"
  previous_color="$2"

  # nginx 존재와 설정 파일 생성을 보장합니다.
  ensure_nginx "${target_color}"

  # nginx reload는 컨테이너 교체가 아니라 설정만 다시 읽는 동작입니다.
  if ! docker exec "${NGINX_CONTAINER_NAME}" nginx -s reload; then
    # 전환 실패 시 기존 active가 있었다면 이전 설정으로 되돌립니다.
    if [ "${previous_color}" != "none" ]; then
      write_nginx_config "${previous_color}"
      docker cp "${NGINX_CONF_FILE}" "${NGINX_CONTAINER_NAME}:/etc/nginx/conf.d/default.conf" || true
      docker exec "${NGINX_CONTAINER_NAME}" nginx -s reload || true
    fi
    return 1
  fi
}

# 새 컨테이너가 트래픽을 받을 준비가 되었는지 확인합니다.
# nginx 전환 전 localhost port로 직접 health check합니다.
wait_for_health() {
  color="$1"
  port="$(host_port "${color}")"
  name="$(container_name "${color}")"

  # 최대 30회 * 2초 = 약 60초 대기합니다.
  # 개선 가능점: CPU 제한을 걸거나 cold start가 길면 retry/interval을 환경변수화할 수 있습니다.
  for attempt in $(seq 1 30); do
    if curl -fsS "http://127.0.0.1:${port}${HEALTH_PATH}" >/dev/null; then
      return 0
    fi

    if [ "${attempt}" -eq 30 ]; then
      echo "Health check failed for ${name} (${IMAGE_TAG})"
      docker logs --tail 200 "${name}" || true
      return 1
    fi

    sleep 2
  done
}

# EC2 로컬 Docker image store에서 백엔드 이미지를 정리합니다.
# 최신 KEEP_BACKEND_IMAGE_COUNT개와 현재 실행 중인 이미지 ID는 보존합니다.
# nginx/curl 등 다른 repository 이미지는 건드리지 않습니다.
cleanup_backend_images() {
  backend_repository="${ECR_REGISTRY}/${ECR_REPOSITORY}"

  # 0 이하로 설정하면 이미지 정리를 비활성화합니다.
  if [ "${KEEP_BACKEND_IMAGE_COUNT}" -le 0 ]; then
    return
  fi

  # backend repository의 tag 이미지 목록을 created timestamp 역순으로 정렬합니다.
  # docker image ls의 CreatedAt 문자열 대신 image inspect의 Created ISO timestamp를 사용합니다.
  image_rows="$(
    docker image ls "${backend_repository}" --format '{{.Repository}}:{{.Tag}}' \
      | awk '!/:<none>$/ { print }' \
      | while IFS= read -r image_ref; do
          docker image inspect --format '{{.Created}} {{.Id}} {{index .RepoTags 0}}' "${image_ref}" 2>/dev/null || true
        done \
      | sort -r
  )"

  # 정리할 backend 이미지가 없으면 조용히 종료합니다.
  if [ -z "${image_rows}" ]; then
    return
  fi

  # 실행 중인 컨테이너가 사용하는 image ID 목록입니다.
  # 의도: 정렬 기준이 꼬여도 active image는 절대 삭제하지 않습니다.
  running_image_ids="$(docker ps -q | xargs -r docker inspect -f '{{.Image}}' | sort -u || true)"

  kept_count=0
  printf '%s\n' "${image_rows}" | while read -r _created image_id image_ref; do
    kept_count=$((kept_count + 1))

    # 최신 N개는 rollback/pull layer 재사용을 위해 유지합니다.
    if [ "${kept_count}" -le "${KEEP_BACKEND_IMAGE_COUNT}" ]; then
      continue
    fi

    # 현재 실행 중인 컨테이너가 쓰는 이미지는 삭제하지 않습니다.
    if printf '%s\n' "${running_image_ids}" | grep -Fxq "${image_id}"; then
      continue
    fi

    # 삭제 실패는 배포 실패로 보지 않습니다.
    # 이유: 이미지 정리는 부가 작업이고, 배포 성공 자체를 되돌릴 이유는 아닙니다.
    docker rmi "${image_ref}" >/dev/null 2>&1 || true
  done
}

# EC2에서 ECR 이미지를 pull할 수 있도록 Docker login을 수행합니다.
aws ecr get-login-password --region "${AWS_REGION}" \
  | docker login --username AWS --password-stdin "${ECR_REGISTRY}"

# backend/nginx가 공유할 Docker network를 생성합니다. 이미 있으면 무시합니다.
docker network create "${NETWORK_NAME}" >/dev/null 2>&1 || true
# CI가 ECR에 push한 release 이미지를 EC2 로컬로 가져옵니다.
docker pull "${IMAGE_URI}"

# 현재 active 색상을 찾고 다음 배포 색상을 결정합니다.
active_color="$(detect_active_color)"
next_color="$(opposite_color "${active_color}")"
next_container="$(container_name "${next_color}")"
next_port="$(host_port "${next_color}")"

# SSM/GitHub Actions 로그에서 배포 상태를 추적하기 위한 출력입니다.
echo "Active color: ${active_color}"
echo "Deploying ${IMAGE_URI} to ${next_color} (${next_container})"

# 이전에 실패하고 남은 inactive 컨테이너가 있으면 제거합니다.
# active 컨테이너는 여기서 건드리지 않습니다.
docker rm -f "${next_container}" >/dev/null 2>&1 || true

# 새 버전 컨테이너를 inactive color로 실행합니다.
# host port는 127.0.0.1에만 bind해서 외부 트래픽은 nginx 전환 전까지 받지 않습니다.
docker run -d \
  --name "${next_container}" \
  --restart unless-stopped \
  --network "${NETWORK_NAME}" \
  --env-file "${DEPLOY_DIR}/.env" \
  -p "127.0.0.1:${next_port}:8080" \
  -v "${IMAGE_VOLUME_NAME}:/backend/image" \
  "${IMAGE_URI}" >/dev/null

# 새 컨테이너 health check가 실패하면 새 컨테이너만 제거하고 배포를 중단합니다.
# 기존 active 컨테이너와 nginx 설정은 유지됩니다.
if ! wait_for_health "${next_color}"; then
  docker rm -f "${next_container}" >/dev/null 2>&1 || true
  exit 1
fi

# health check가 통과한 뒤에만 nginx를 새 컨테이너로 전환합니다.
# reload 실패 시 기존 nginx 설정으로 rollback을 시도합니다.
if ! reload_nginx_to "${next_color}" "${active_color}"; then
  echo "Failed to switch nginx to ${next_container}"
  docker rm -f "${next_container}" >/dev/null 2>&1 || true
  exit 1
fi

# 전환 성공 후 active color 상태 파일을 갱신합니다.
echo "${next_color}" > "${ACTIVE_COLOR_FILE}"

# 기존 연결이 마무리될 시간을 잠깐 줍니다.
if [ "${SWITCH_GRACE_SECONDS}" -gt 0 ]; then
  sleep "${SWITCH_GRACE_SECONDS}"
fi

# 이전 active 컨테이너를 제거합니다.
# active가 none이면 첫 배포이므로 제거할 blue/green 컨테이너가 없습니다.
if [ "${active_color}" != "none" ]; then
  docker rm -f "$(container_name "${active_color}")" >/dev/null 2>&1 || true
fi

# blue/green 도입 전 단일 컨테이너 이름으로 떠 있던 기존 컨테이너를 정리합니다.
docker rm -f "${OLD_SINGLE_CONTAINER_NAME}" >/dev/null 2>&1 || true
# dangling image만 먼저 가볍게 정리합니다.
docker image prune -f >/dev/null 2>&1 || true
# 백엔드 이미지는 최근 N개만 남겨 EC2 EBS 사용량이 계속 늘어나는 것을 막습니다.
cleanup_backend_images

# 최종 성공 로그입니다.
echo "Deployed ${IMAGE_URI} to ${next_container}"
