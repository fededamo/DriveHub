#!/usr/bin/env bash
# Ephemeral PostgreSQL 16 verification, never the application's persistent volume.
set -euo pipefail
cd "$(dirname "$0")/.."
export DRIVEHUB_TEST_DB_PASSWORD="$(python3 -c 'import secrets; print(secrets.token_urlsafe(24))')"
export DRIVEHUB_TEST_DB_PORT="${DRIVEHUB_TEST_DB_PORT:-15432}"
export DRIVEHUB_TEST_DB_URL="jdbc:postgresql://127.0.0.1:${DRIVEHUB_TEST_DB_PORT}/drivehub_test"
export DRIVEHUB_TEST_DB_USER=drivehub_test
project="drivehub-tests-$$"
cleanup() { docker compose -p "$project" -f compose.test.yaml down; }
trap cleanup EXIT
docker compose -p "$project" -f compose.test.yaml up -d --wait
docker compose -p "$project" -f compose.test.yaml exec -T postgres-test postgres --version
mvn -Ppostgres clean test "$@"
