#!/usr/bin/env bash
# Pulls one validated commit and its prebuilt image, then restarts the stack.
set -euo pipefail

revision="${1:-master}"
cd "$(dirname "$0")"

if [[ ! -f .env ]]; then
  echo "deploy/.env is missing; copy .env.example and fill in real values first." >&2
  exit 1
fi

if ! grep -qE '^ATID_DOMAIN=' .env; then
  echo "deploy/.env has no ATID_DOMAIN; see deploy/README.md." >&2
  exit 1
fi

git fetch --prune origin master
git checkout --detach "$revision"

# Images are tagged with the exact source commit. Pull before changing running
# containers, so a missing image leaves production untouched.
export ATID_IMAGE_TAG="$(git rev-parse HEAD)"
docker compose pull web
docker compose up -d --no-build --remove-orphans
docker image prune -f

domain="$(grep -E '^ATID_DOMAIN=' .env | cut -d= -f2-)"
for attempt in $(seq 1 30); do
  if curl -fsS -o /dev/null "https://${domain}/"; then
    echo "Deployed $(git rev-parse --short HEAD) to https://${domain}"
    exit 0
  fi
  sleep 5
done

echo "Health check failed for https://${domain}/" >&2
docker compose ps
docker compose logs --tail=100 web caddy
exit 1
