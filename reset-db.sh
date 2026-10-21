#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

./run.sh --db-only
command -v java >/dev/null || { echo 'Java non trovato: serve JDK 21.' >&2; exit 2; }
command -v mvn >/dev/null || { echo 'Maven non trovato: serve Maven 3.9+.' >&2; exit 2; }

echo 'Reset del database: i dati applicativi saranno sostituiti con quelli di demo.sql.' >&2
exec mvn -B compile exec:java '-Dexec.mainClass=it.unifi.ing.drivehub.dao.postgres.ResetDatabase'
