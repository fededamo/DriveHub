#!/usr/bin/env bash
# Full JDK 21 + JavaFX + PostgreSQL 16 verification in disposable containers.
set -euo pipefail
cd "$(dirname "$0")/.."

export DRIVEHUB_TEST_DB_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(24))')"
project="drivehub-full-stack-$$"
network="${project}_default"
workspace="$(pwd)"
owner_uid="$(id -u)"
owner_gid="$(id -g)"
commit_base="$(git rev-parse HEAD)"

cleanup() {
  docker compose -p "$project" -f compose.test.yaml down
}
trap cleanup EXIT

docker compose -p "$project" -f compose.test.yaml up -d --wait
docker compose -p "$project" -f compose.test.yaml exec -T postgres-test postgres --version

docker run --rm \
  --network "$network" \
  -e DRIVEHUB_TEST_DB_URL=jdbc:postgresql://postgres-test:5432/drivehub_test \
  -e DRIVEHUB_TEST_DB_USER=drivehub_test \
  -e DRIVEHUB_TEST_DB_PASSWORD="$DRIVEHUB_TEST_DB_PASSWORD" \
  -e DRIVEHUB_COMMIT_BASE="$commit_base" \
  -v "$workspace:/workspace" \
  -w /workspace \
  maven:3.9.11-eclipse-temurin-21 \
  bash -lc "set -o pipefail;
    apt-get update -qq &&
    apt-get install -y -qq xvfb libgtk-3-0 libxtst6 libxrender1 libxi6 >/dev/null &&
    timeout 240s xvfb-run -a mvn -B -ntp -Pgui,postgres -DexcludedTestGroups= \
      -Ddrivehub.ui.screenshots=/workspace/docs/screenshots clean test \
      | tee /tmp/drivehub-full-stack-maven.log;
    rc=\${PIPESTATUS[0]};
    if [[ \$rc -eq 0 ]]; then
      python3 scripts/record_verification.py full-stack \
        --command 'mvn -B -ntp -Pgui,postgres -DexcludedTestGroups= clean test' \
        --log /tmp/drivehub-full-stack-maven.log || rc=\$?;
    fi;
    chown -R ${owner_uid}:${owner_gid} /workspace/target /workspace/docs/screenshots;
    chown -R ${owner_uid}:${owner_gid} /workspace/docs/evidence/full-stack 2>/dev/null || true;
    exit \$rc"
