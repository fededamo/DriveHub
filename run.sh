#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

if [[ ! -f .env ]]; then
  if [[ ! -f .env.example ]]; then
    echo 'File .env.example mancante: impossibile creare la configurazione locale.' >&2
    exit 2
  fi
  if ! grep -q '^DRIVEHUB_DB_PASSWORD=' .env.example; then
    echo 'DRIVEHUB_DB_PASSWORD manca in .env.example.' >&2
    exit 2
  fi
  password="$(od -An -N24 -tx1 /dev/urandom | tr -d '[:space:]')"
  (umask 077; sed "s/^DRIVEHUB_DB_PASSWORD=.*/DRIVEHUB_DB_PASSWORD=$password/" .env.example > .env)
  echo 'Creato .env con una password casuale. Il file non è versionato.'
fi
if grep -Eq '^[[:space:]]*DRIVEHUB_DB_PASSWORD[[:space:]]*=[[:space:]]*change-me-local-only[[:space:]]*$' .env; then
  echo 'La password in .env è ancora quella di esempio: sostituiscila prima di avviare il database.' >&2
  exit 2
fi
command -v docker >/dev/null || { echo 'Docker non trovato.' >&2; exit 2; }
docker compose version >/dev/null || { echo 'Docker Compose non disponibile.' >&2; exit 2; }

echo 'Avvio PostgreSQL...'
docker compose up -d --wait postgres
docker compose exec -T postgres sh -c 'PGPASSWORD="$POSTGRES_PASSWORD" psql -h 127.0.0.1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" -q </dev/null' || {
  echo 'Credenziali PostgreSQL non valide: se il volume esisteva già, ripristina la vecchia password in .env.' >&2
  exit 1
}

if [[ "${1:-}" == '--db-only' ]]; then
  exit 0
fi
if (( $# > 0 )); then
  echo "Argomento sconosciuto: $1" >&2
  exit 2
fi

command -v java >/dev/null || { echo 'Java non trovato: serve JDK 21.' >&2; exit 2; }
command -v mvn >/dev/null || { echo 'Maven non trovato: serve Maven 3.9+.' >&2; exit 2; }

if grep -Eq '^[[:space:]]*DRIVEHUB_DB_SEED_DEMO[[:space:]]*=[[:space:]]*true[[:space:]]*$' .env; then
  echo 'DRIVEHUB_DB_SEED_DEMO=true cancellerebbe i dati a ogni avvio. Imposta false in .env e usa reset-db.sh per il ripopolamento esplicito.' >&2
  exit 2
fi

echo 'Avvio DriveHub...'
exec mvn javafx:run
