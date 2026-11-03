# Ticket 02 / R01 — invarianti del veicolo

Verifica del 31 ottobre 2026, rilievo **#19**, AC19 e parte pertinente di AC13.
Baseline Git: `c2a81122ee6be44760ded4160fde229bd33e837c`; nessun commit.
Prerequisito controllato: ticket 01 `State: closed`, 6/6 criteri e prove R00 presenti.

## Correzione e confine

`Vehicle.setLicencePlate` normalizza in `cleanPlate`, valida il vincolo con i
chilometri e soltanto dopo assegna il campo. API, normalizzazione e messaggio di
errore restano invariati. Nessun refactor, dipendenza, modifica SQL o fixture.

Le prove usano solo factory, setter e getter pubblici di `Vehicle`, senza mock o
riflessione. Con 100 km, `null`, stringa vuota e soli spazi/tab producono
`IllegalArgumentException` conservando tutti gli otto dati del veicolo e `isUsed()`.
Coperti anche targa normalizzata con successivo aggiornamento dei chilometri,
rimozione dopo azzeramento dei chilometri e rifiuto di chilometri positivi senza targa.

## Ambiente e comandi

Ripreso l'[ambiente R00 documentato](../../testing.md): OpenJDK Linux 21.0.12.1,
Maven 3.9.16, PostgreSQL 16.15. `java -version` e `mvn -version` confermano Java 21.
POM invariato; build completa con 62 sorgenti principali e 8 sorgenti di test.

I comandi seguenti sono eseguiti dalla radice del progetto; il launcher R00 carica
le credenziali private da `/tmp/drivehub-r00-env.json` senza stamparle.

| Comando | Esito | Log |
|---|---|---|
| `python3 /tmp/drivehub-r00-maven.py ticket02-red -Dtest=DomainInvariantsTest#testRejectedVehiclePlateChangeLeavesVehicleUnchanged test` | RED: 3 failure attese, 0 errori/saltati; targa attesa `AB123CD`, ottenuta `null`, `isUsed()` da true a false | [RED](red.txt) |
| `python3 /tmp/drivehub-r00-maven.py ticket02-green -Dtest=DomainInvariantsTest#testRejectedVehiclePlateChangeLeavesVehicleUnchanged test` | GREEN: stessi 3 casi passati dopo la sola correzione del setter | [GREEN](green.txt) |
| `python3 /tmp/drivehub-r00-maven.py ticket02-targeted -Dtest=DomainInvariantsTest test` | 15/15 passati, comprese le modifiche valide | [Mirati](targeted.txt) |
| `python3 /tmp/drivehub-r00-maven.py ticket02-no-db test` | 28/28 senza DB | [Senza DB](no-db.txt) |
| `python3 /tmp/drivehub-r00-maven.py ticket02-full -Ppostgres-tests clean test` | BUILD SUCCESS, 35/35, 0 failure/errori/saltati; 7 prove PostgreSQL incluse | [Completa](full-suite.txt) |

Cluster riavviato con `pg_ctl -D /tmp/drivehub-r00-pgdata -l /tmp/drivehub-r00-postgres.log -o '-h 127.0.0.1 -p 55432 -k /tmp' -w start`, usando i binari e le librerie R00.
Il primo avvio nella sandbox è fallito sull'apertura del socket; avvio e suite DB
sono stati poi eseguiti con escalation autorizzata. Solo `drivehub_test` e la copia
applicativa `drivehub` con sentinella sul cluster temporaneo; nessun accesso al DB
applicativo preesistente. La prova `DatabaseIsolationIntegrationTest` è passata.
Terminata la suite, `pg_ctl -D /tmp/drivehub-r00-pgdata -m fast -w stop` è riuscito.

## Chiamanti e revisione finale

- Ricerca di `setLicencePlate`, `setKilometers` e `Vehicle.create` in `src/main` e
  `src/test`, con relazione esclusa. Unico chiamante applicativo del setter:
  `SaleToDealershipService.submitSaleProposal`, che aggiorna targa e poi chilometri.
  Le modifiche valide restano ammesse; una rimozione con chilometri ancora positivi
  continua a fallire, conservando ora lo stato precedente.
- Controllati anche `ClientDashboardView` (errore mostrato), `CatalogService.addVehicle`,
  factory e mapping/salvataggio `PostgresVehicleDao`, nonché il CHECK SQL
  `chk_vehicle_plate_km`. Nessun adeguamento dei chiamanti, schema o demo necessario.
- Revisione Standards e Spec tramite due revisori indipendenti completata, esiti sotto.
- Verificati riepiloghi dei log e XML Surefire: 35 test, nessun errore o salto.
  [Hash dei due sorgenti verificati](sources.sha256) controllati con `sha256sum -c`.
  I 22 file con modifiche preesistenti hanno hash invariati; HEAD invariato.
  `git diff --check` superato nel perimetro senza relazione; nessuna password ambiente
  trovata nei log conservati.

## Standards

Nessun rilievo sul diff del ticket 02. La correzione mantiene responsabilità nel
dominio, messaggio e API; riusa `cleanPlate` come nella factory. I test verificano
il comportamento pubblico con JUnit già disponibile. Nessuna violazione concreta
degli standard né odore Fowler proporzionato; l'allestimento ripetuto nei test non
giustifica un helper. Il revisore ha controllato staticamente il diff e i chiamanti,
senza rieseguire i test o modificare file.

## Spec

Nessun requisito mancante o parziale nel perimetro del ticket, nessuno scope creep,
nessuna implementazione scorretta. AC19 soddisfatto da validazione prima della
scrittura e prove su tutti gli attributi; modifiche valide e chiamanti compatibili.
Il revisore ha letto le evidenze RED/GREEN e mirate senza rieseguire i test o
modificare file. Coperta la parte pertinente di AC13, che resta un criterio trasversale.

Totale rilievi: Standards 0; Spec 0. Nessuna criticità segnalata nei due assi.

## Limiti e prossimo passo

Warning preesistente nel modello Maven JavaFX 21.0.2; nessuna UI modificata o prova
grafica eseguita. I risultati non certificano i difetti degli altri ticket.
Ambiente e launcher in `/tmp` sono temporanei, da ricreare se rimossi.
`Relazione_SWE` sempre esclusa. Ticket 02 chiuso con 4/4 criteri verificati;
R01 resta parziale fino al ticket 03 sul cambio email, da avviare separatamente.
