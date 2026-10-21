$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot

& (Join-Path $PSScriptRoot 'run.ps1') -DatabaseOnly

if (-not (Get-Command java -ErrorAction SilentlyContinue)) { throw 'Java non trovato: serve JDK 21.' }
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) { throw 'Maven non trovato: serve Maven 3.9+.' }

Write-Warning 'Reset del database: i dati applicativi saranno sostituiti con quelli di demo.sql.'
mvn -B compile exec:java '-Dexec.mainClass=it.unifi.ing.drivehub.dao.postgres.ResetDatabase'
if ($LASTEXITCODE -ne 0) { throw 'Reset del database non riuscito.' }
