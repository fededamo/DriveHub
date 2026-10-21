param([switch]$DatabaseOnly)

$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot

if (-not (Test-Path -LiteralPath '.env' -PathType Leaf)) {
    if (-not (Test-Path -LiteralPath '.env.example' -PathType Leaf)) {
        throw 'File .env.example mancante: impossibile creare la configurazione locale.'
    }
    $template = [System.IO.File]::ReadAllText((Join-Path $PSScriptRoot '.env.example'))
    if (-not [regex]::IsMatch($template, '(?m)^DRIVEHUB_DB_PASSWORD=.*$')) {
        throw 'DRIVEHUB_DB_PASSWORD manca in .env.example.'
    }
    $bytes = [byte[]]::new(24)
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
    $password = [BitConverter]::ToString($bytes).Replace('-', '').ToLowerInvariant()
    $contents = [regex]::Replace($template, '(?m)^DRIVEHUB_DB_PASSWORD=.*$', "DRIVEHUB_DB_PASSWORD=$password")
    [System.IO.File]::WriteAllText((Join-Path $PSScriptRoot '.env'), $contents, [System.Text.UTF8Encoding]::new($false))
    Write-Host 'Creato .env con una password casuale. Il file non è versionato.' -ForegroundColor Cyan
}
if (Select-String -Path '.env' -Pattern '^\s*DRIVEHUB_DB_PASSWORD\s*=\s*change-me-local-only\s*$' -Quiet) {
    throw 'La password in .env è ancora quella di esempio: sostituiscila prima di avviare il database.'
}
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw 'Docker non trovato. Installa Docker Desktop e avvialo.'
}

docker compose version | Out-Null
if ($LASTEXITCODE -ne 0) { throw 'Docker Compose non disponibile.' }

Write-Host 'Avvio PostgreSQL...' -ForegroundColor Cyan
docker compose up -d --wait postgres
if ($LASTEXITCODE -ne 0) { throw 'PostgreSQL non si è avviato: controlla Docker Desktop e docker compose ps.' }

docker compose exec -T postgres sh -c 'PGPASSWORD="$POSTGRES_PASSWORD" psql -h 127.0.0.1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" -q </dev/null'
if ($LASTEXITCODE -ne 0) {
    throw 'Credenziali PostgreSQL non valide: se il volume esisteva già, ripristina la vecchia password in .env.'
}

if ($DatabaseOnly) { return }

if (-not (Get-Command java -ErrorAction SilentlyContinue)) { throw 'Java non trovato: serve JDK 21.' }
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) { throw 'Maven non trovato: serve Maven 3.9+.' }

$seedSetting = Select-String -Path '.env' -Pattern '^\s*DRIVEHUB_DB_SEED_DEMO\s*=\s*true\s*$' -Quiet
if ($seedSetting) {
    throw 'DRIVEHUB_DB_SEED_DEMO=true cancellerebbe i dati a ogni avvio. Imposta false in .env e usa reset-db.bat per il ripopolamento esplicito.'
}

Write-Host 'Avvio DriveHub...' -ForegroundColor Cyan
mvn javafx:run
if ($LASTEXITCODE -ne 0) { throw 'Avvio JavaFX non riuscito.' }
