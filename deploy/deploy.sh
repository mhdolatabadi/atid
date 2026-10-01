#!/usr/bin/env bash
# Pulls one validated commit and its prebuilt image, then restarts atid's stack.
#
#   deploy.sh [revision]   check out <revision> (default master), then run that commit's copy of
#                          this script, so a deploy never runs an older script against newer files.
set -euo pipefail
cd "$(dirname "$0")"

if [[ "${1:-}" != "--checked-out" ]]; then
  git fetch --prune origin master
  git checkout --detach "${1:-master}"
  exec bash ./deploy.sh --checked-out
fi

main() {
  if [[ ! -f .env ]]; then
    echo "deploy/.env is missing; copy .env.example and fill in real values first." >&2
    exit 1
  fi

  if ! grep -qE '^ATID_DOMAIN=' .env; then
    echo "deploy/.env has no ATID_DOMAIN; see deploy/README.md." >&2
    exit 1
  fi

  # The project name is what keeps this deploy away from every other stack on the server.
  if ! grep -qx 'name: atid' compose.yaml; then
    echo "compose.yaml must pin the project name (name: atid); refusing to deploy." >&2
    exit 1
  fi

  local network
  network="$(grep -E '^ATID_PROXY_NETWORK=' .env | cut -d= -f2- || true)"
  if ! docker network inspect "${network:-deploy_edge}" > /dev/null 2>&1; then
    echo "Proxy network '${network:-deploy_edge}' does not exist; set ATID_PROXY_NETWORK in deploy/.env (docker network ls)." >&2
    exit 1
  fi

  # Images are tagged with the exact source commit. Pull before changing running containers,
  # so a missing image leaves production untouched.
  export ATID_IMAGE_TAG="$(git rev-parse HEAD)"
  docker compose pull atid-web
  # The pinned project name means --remove-orphans can only ever remove atid's own containers.
  docker compose up -d --no-build --remove-orphans
  docker image prune -f

  local domain
  domain="$(grep -E '^ATID_DOMAIN=' .env | cut -d= -f2-)"
  for attempt in $(seq 1 30); do
    if curl -fsS -o /dev/null "https://${domain}/"; then
      echo "Deployed $(git rev-parse --short HEAD) to https://${domain}"
      exit 0
    fi
    sleep 5
  done

  echo "Health check failed for https://${domain}/ (is the proxy's site block for ${domain} in place?)" >&2
  docker compose ps
  docker compose logs --tail=100 atid-web
  exit 1
}

# Everything runs from a function that bash has fully read before it starts.
main
