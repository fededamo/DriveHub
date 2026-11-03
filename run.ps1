$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
$container = 'drivehub-quickstart-db'
$image = 'postgres:16'

try {
    foreach ($command in @('docker', 'java', 'mvn')) {
        if (-not (Get-Command $command -ErrorAction SilentlyContinue)) {
            throw "$command non trovato. Installa Docker Desktop, JDK 21 e Maven 3.9+ e verifica il PATH."
        }
    }
    docker info *> $null
    if ($LASTEXITCODE -ne 0) {
        $desktop = Join-Path $env:ProgramFiles 'Docker\Docker\Docker Desktop.exe'
        if (-not (Test-Path $desktop)) { throw 'Avvia Docker Desktop e riprova.' }
        Write-Host 'Avvio Docker Desktop...'
        Start-Process $desktop
        $ready = $false
        for ($i = 0; $i -lt 90; $i++) {
            Start-Sleep -Seconds 2
            docker info *> $null
            if ($LASTEXITCODE -eq 0) { $ready = $true; break }
        }
        if (-not $ready) { throw 'Docker Desktop non e pronto. Controlla Docker Desktop e riprova.' }
    }

    $existing = docker container ls -a --filter "name=^/$container$" --format '{{.Names}}'
    if ($LASTEXITCODE -ne 0) { throw 'Impossibile controllare i contenitori Docker.' }
    $created = $false
    if (-not $existing) {
        $volume = docker volume ls --filter 'name=^drivehub-quickstart-data$' --format '{{.Name}}'
        if ($LASTEXITCODE -ne 0) { throw 'Impossibile controllare i volumi Docker.' }
        if ($volume) { throw 'Il volume drivehub-quickstart-data esiste gia senza il contenitore: non verra reinizializzato.' }
        Write-Host 'Creo il database demo persistente (il primo avvio puo richiedere alcuni minuti)...'
        docker run -d --name $container --label 'it.unifi.drivehub.quickstart=1' -p '127.0.0.1:55432:5432' -e 'POSTGRES_USER=drivehub' -e 'POSTGRES_PASSWORD=drivehub-demo' -e 'POSTGRES_DB=drivehub' -v 'drivehub-quickstart-data:/var/lib/postgresql/data' $image
        if ($LASTEXITCODE -ne 0) { throw 'Creazione del contenitore fallita. Controlla Docker e la porta 55432.' }
        $created = $true
    } else {
        $labelsJson = docker inspect --format '{{json .Config.Labels}}' $container
        if ($LASTEXITCODE -ne 0) { throw 'Impossibile verificare il contenitore Docker.' }
        $labels = $labelsJson | ConvertFrom-Json
        if ($labels.'it.unifi.drivehub.quickstart' -ne '1') { throw "Il contenitore $container esiste ma non appartiene a questo avvio. Non verra modificato." }
        docker start $container | Out-Null
        if ($LASTEXITCODE -ne 0) { throw 'Impossibile avviare il database. Controlla Docker e la porta 55432.' }
    }

    $ready = $false
    for ($i = 0; $i -lt 60; $i++) {
        docker exec $container pg_isready -h 127.0.0.1 -U drivehub -d drivehub *> $null
        if ($LASTEXITCODE -eq 0) { $ready = $true; break }
        Start-Sleep -Seconds 2
    }
    if (-not $ready) { throw 'PostgreSQL non e pronto. Controlla i log del contenitore.' }

    if ($created) {
        foreach ($script in @('schema.sql', 'demo.sql')) {
            docker cp "src/main/resources/db/$script" "${container}:/tmp/$script"
            if ($LASTEXITCODE -ne 0) { throw "Copia di $script fallita." }
            docker exec $container psql -X -U drivehub -d drivehub -v ON_ERROR_STOP=1 --single-transaction -f "/tmp/$script"
            if ($LASTEXITCODE -ne 0) { throw "Inizializzazione $script fallita; i dati non verranno ripristinati automaticamente." }
        }
        docker exec $container psql -X -U drivehub -d drivehub -v ON_ERROR_STOP=1 -c 'CREATE TABLE drivehub_quickstart_initialized (version integer NOT NULL); INSERT INTO drivehub_quickstart_initialized VALUES (1);'
        if ($LASTEXITCODE -ne 0) { throw 'Impossibile completare inizializzazione demo.' }
    }
    docker exec $container psql -X -U drivehub -d drivehub -v ON_ERROR_STOP=1 -tAc 'SELECT version FROM drivehub_quickstart_initialized' *> $null
    if ($LASTEXITCODE -ne 0) { throw 'Il database demo non risulta inizializzato: controlla i log e i dati prima di intervenire.' }

    $env:DRIVEHUB_DB_URL = 'jdbc:postgresql://127.0.0.1:55432/drivehub'
    $env:DRIVEHUB_DB_USER = 'drivehub'
    $env:DRIVEHUB_DB_PASSWORD = 'drivehub-demo'
    $env:MAVEN_OPTS = '-Xms32m -Xmx192m -XX:+UseSerialGC'
    Write-Host 'Avvio DriveHub. Accesso demo: mario.rossi@email.it / password'
    & mvn javafx:run
    if ($LASTEXITCODE -ne 0) { throw 'Avvio Maven non riuscito.' }
} catch {
    Write-Host "Errore: $_" -ForegroundColor Red
    exit 1
}
