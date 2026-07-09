#!/usr/bin/env bash
set -euo pipefail

: "${AWS_REGION:?AWS_REGION is required}"
: "${ECR_REGISTRY:?ECR_REGISTRY is required}"
: "${IMAGE_URI:?IMAGE_URI is required}"
: "${IMAGE_TAG:?IMAGE_TAG is required}"
: "${DEPLOY_DIR:?DEPLOY_DIR is required}"

NETWORK_NAME="community-backend"
CONTAINER_NAME="community-backend"

mkdir -p "${DEPLOY_DIR}"
cd "${DEPLOY_DIR}"

aws ecr get-login-password --region "${AWS_REGION}" \
  | docker login --username AWS --password-stdin "${ECR_REGISTRY}"

docker network create "${NETWORK_NAME}" >/dev/null 2>&1 || true
docker pull "${IMAGE_URI}"

docker rm -f "${CONTAINER_NAME}" >/dev/null 2>&1 || true

docker run -d \
  --name "${CONTAINER_NAME}" \
  --restart unless-stopped \
  --network "${NETWORK_NAME}" \
  --env-file "${DEPLOY_DIR}/.env" \
  -p "8080:8080" \
  -v community_backend_images:/backend/image \
  "${IMAGE_URI}"

for attempt in $(seq 1 30); do
  if curl -fsS "http://127.0.0.1:8080/actuator/health" >/dev/null; then
    break
  fi

  if [ "${attempt}" -eq 30 ]; then
    echo "Health check failed for ${CONTAINER_NAME} (${IMAGE_TAG})"
    docker logs --tail 200 "${CONTAINER_NAME}" || true
    exit 1
  fi

  sleep 2
done

docker image prune -f >/dev/null 2>&1 || true

echo "Deployed ${IMAGE_URI} to ${CONTAINER_NAME}"
