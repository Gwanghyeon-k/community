#!/usr/bin/env bash
set -euo pipefail

: "${AWS_REGION:?AWS_REGION is required}"
: "${ECR_REGISTRY:?ECR_REGISTRY is required}"
: "${IMAGE_URI:?IMAGE_URI is required}"
: "${IMAGE_TAG:?IMAGE_TAG is required}"
: "${DEPLOY_DIR:?DEPLOY_DIR is required}"

NETWORK_NAME="community-backend"
NGINX_CONTAINER_NAME="community-nginx"
OLD_SINGLE_CONTAINER_NAME="community-backend"
IMAGE_VOLUME_NAME="community_backend_images"
ACTIVE_COLOR_FILE="${DEPLOY_DIR}/.active-color"
NGINX_CONF_DIR="${DEPLOY_DIR}/nginx/conf.d"
NGINX_CONF_FILE="${NGINX_CONF_DIR}/default.conf"

BLUE_HOST_PORT="${BLUE_HOST_PORT:-8081}"
GREEN_HOST_PORT="${GREEN_HOST_PORT:-8082}"
PUBLIC_HTTP_PORT="${PUBLIC_HTTP_PORT:-80}"
HEALTH_PATH="${HEALTH_PATH:-/actuator/health}"
SWITCH_GRACE_SECONDS="${SWITCH_GRACE_SECONDS:-10}"
KEEP_BACKEND_IMAGE_COUNT="${KEEP_BACKEND_IMAGE_COUNT:-5}"

mkdir -p "${DEPLOY_DIR}" "${NGINX_CONF_DIR}"
cd "${DEPLOY_DIR}"

container_name() {
  printf 'community-backend-%s' "$1"
}

host_port() {
  case "$1" in
    blue) printf '%s' "${BLUE_HOST_PORT}" ;;
    green) printf '%s' "${GREEN_HOST_PORT}" ;;
    *) echo "Unknown color: $1" >&2; exit 1 ;;
  esac
}

opposite_color() {
  case "$1" in
    blue) printf 'green' ;;
    green) printf 'blue' ;;
    none) printf 'blue' ;;
    *) echo "Unknown color: $1" >&2; exit 1 ;;
  esac
}

is_running() {
  [ "$(docker inspect -f '{{.State.Running}}' "$1" 2>/dev/null || true)" = "true" ]
}

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

ensure_nginx() {
  target_color="$1"
  write_nginx_config "${target_color}"

  if docker inspect "${NGINX_CONTAINER_NAME}" >/dev/null 2>&1; then
    docker network connect "${NETWORK_NAME}" "${NGINX_CONTAINER_NAME}" >/dev/null 2>&1 || true
    docker start "${NGINX_CONTAINER_NAME}" >/dev/null
    docker cp "${NGINX_CONF_FILE}" "${NGINX_CONTAINER_NAME}:/etc/nginx/conf.d/default.conf"
  else
    docker run -d \
      --name "${NGINX_CONTAINER_NAME}" \
      --restart unless-stopped \
      --network "${NETWORK_NAME}" \
      -p "${PUBLIC_HTTP_PORT}:80" \
      -v "${NGINX_CONF_DIR}:/etc/nginx/conf.d:ro" \
      nginx:1.27-alpine >/dev/null
  fi

  docker exec "${NGINX_CONTAINER_NAME}" nginx -t
}

reload_nginx_to() {
  target_color="$1"
  previous_color="$2"

  ensure_nginx "${target_color}"

  if ! docker exec "${NGINX_CONTAINER_NAME}" nginx -s reload; then
    if [ "${previous_color}" != "none" ]; then
      write_nginx_config "${previous_color}"
      docker cp "${NGINX_CONF_FILE}" "${NGINX_CONTAINER_NAME}:/etc/nginx/conf.d/default.conf" || true
      docker exec "${NGINX_CONTAINER_NAME}" nginx -s reload || true
    fi
    return 1
  fi
}

wait_for_health() {
  color="$1"
  port="$(host_port "${color}")"
  name="$(container_name "${color}")"

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

cleanup_backend_images() {
  backend_repository="${ECR_REGISTRY}/${ECR_REPOSITORY}"

  if [ "${KEEP_BACKEND_IMAGE_COUNT}" -le 0 ]; then
    return
  fi

  image_rows="$(
    docker image ls "${backend_repository}" --format '{{.Repository}}:{{.Tag}}' \
      | awk '!/:<none>$/ { print }' \
      | while IFS= read -r image_ref; do
          docker image inspect --format '{{.Created}} {{.Id}} {{index .RepoTags 0}}' "${image_ref}" 2>/dev/null || true
        done \
      | sort -r
  )"

  if [ -z "${image_rows}" ]; then
    return
  fi

  running_image_ids="$(docker ps -q | xargs -r docker inspect -f '{{.Image}}' | sort -u || true)"

  kept_count=0
  printf '%s\n' "${image_rows}" | while read -r _created image_id image_ref; do
    kept_count=$((kept_count + 1))

    if [ "${kept_count}" -le "${KEEP_BACKEND_IMAGE_COUNT}" ]; then
      continue
    fi

    if printf '%s\n' "${running_image_ids}" | grep -Fxq "${image_id}"; then
      continue
    fi

    docker rmi "${image_ref}" >/dev/null 2>&1 || true
  done
}

aws ecr get-login-password --region "${AWS_REGION}" \
  | docker login --username AWS --password-stdin "${ECR_REGISTRY}"

docker network create "${NETWORK_NAME}" >/dev/null 2>&1 || true
docker pull "${IMAGE_URI}"

active_color="$(detect_active_color)"
next_color="$(opposite_color "${active_color}")"
next_container="$(container_name "${next_color}")"
next_port="$(host_port "${next_color}")"

echo "Active color: ${active_color}"
echo "Deploying ${IMAGE_URI} to ${next_color} (${next_container})"

docker rm -f "${next_container}" >/dev/null 2>&1 || true

docker run -d \
  --name "${next_container}" \
  --restart unless-stopped \
  --network "${NETWORK_NAME}" \
  --env-file "${DEPLOY_DIR}/.env" \
  -p "127.0.0.1:${next_port}:8080" \
  -v "${IMAGE_VOLUME_NAME}:/backend/image" \
  "${IMAGE_URI}" >/dev/null

if ! wait_for_health "${next_color}"; then
  docker rm -f "${next_container}" >/dev/null 2>&1 || true
  exit 1
fi

if ! reload_nginx_to "${next_color}" "${active_color}"; then
  echo "Failed to switch nginx to ${next_container}"
  docker rm -f "${next_container}" >/dev/null 2>&1 || true
  exit 1
fi

echo "${next_color}" > "${ACTIVE_COLOR_FILE}"

if [ "${SWITCH_GRACE_SECONDS}" -gt 0 ]; then
  sleep "${SWITCH_GRACE_SECONDS}"
fi

if [ "${active_color}" != "none" ]; then
  docker rm -f "$(container_name "${active_color}")" >/dev/null 2>&1 || true
fi

docker rm -f "${OLD_SINGLE_CONTAINER_NAME}" >/dev/null 2>&1 || true
docker image prune -f >/dev/null 2>&1 || true
cleanup_backend_images

echo "Deployed ${IMAGE_URI} to ${next_container}"
