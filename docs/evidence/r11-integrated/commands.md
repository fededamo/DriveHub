# Comandi ed esiti della sessione18

Directory iniziale `/home/federico/dev/Drivehub`, repository `DriveHub/`.
Esecuzioni sulla copia privata `/tmp/drivehub-ticket18-session/install/DriveHub`.
Nessuno staging/commit/push o pubblicazione. `Relazione_SWE` esclusa.

## Fotografia e strumenti

Fotografia byte per byte di progetto, tracker e piano, senza `.git` e relazione,
**includendo target e ignorati**.1796file,326ignorati; SHA256 in
`baseline-sha256.json`, HEAD/indice/stato/ignorati in `initial-*`.
Verificati17ticket chiusi e senza checkbox aperte: `prerequisites.json`.

Fase1: verificati file/binari Java21/PG16/Xvfb ancora presenti in
`/tmp/drivehub-ticket16-tools/root`; hash in `tools-rechecked.json`.
`prepare.py` ha copiato lo stato locale, escluso target, sostituito soltanto
`application.properties` nella copia e creato il cluster nuovo. Segreti casuali
in env.json0600 sotto sessione0700, mai nei comandi o negli output conservati.

```bash
python3 DriveHub/docs/evidence/r11-integrated/prepare.py
# Con PATH, JAVA_HOME e LD_LIBRARY_PATH dell'ambiente privato:
pg_ctl -D /tmp/drivehub-ticket18-session/pgdata \
  -l /home/federico/dev/Drivehub/DriveHub/docs/evidence/r11-integrated/postgres.log -w start
/tmp/drivehub-ticket16-tools/root/usr/bin/Xvfb :90 \
  -screen 0 1440x1000x24 -nolisten tcp -nolisten unix -ac
python3 DriveHub/docs/evidence/r11-integrated/setup-db.py
python3 DriveHub/docs/evidence/r11-integrated/check-db.py before-suite
```

Processi/server avviati fuori sandbox per persistenza dei socket. Porta55436
e display90 controllati liberi prima dell'avvio. `setup-db.py` crea nuovi ruoli
non amministrativi `drivehub_test`/`drivehub`, ciascuno proprietario del solo DB
omonimo; nessuna appartenenza del ruolo test. Connessione applicativa: soltanto
copia sentinella nuova, mai il DB preesistente/porta5432. Schema vuoto, demo
facoltativa e marchio R00_SENTINEL caricati nella sola copia sacrificabile.

## Prove mirate e GUI — fase1

```bash
python3 DriveHub/docs/evidence/r11-integrated/run-maven.py targeted \
  -Ppostgres-tests \
  -Dtest=TestDatabaseConfigurationTest,DatabaseIsolationIntegrationTest,DomainInvariantsTest,AuthServiceIntegrationTest,PriceSelectionIntegrationTest,PurchaseAgreementIntegrationTest,PurchaseConcurrencyIntegrationTest,BalanceConcurrencyIntegrationTest,SaleProposalIntegrationTest,SaleOutcomeIntegrationTest,BookingConcurrencyIntegrationTest,TestDriveCancellationIntegrationTest,VehicleDestinationIntegrationTest,IdentityDeletionIntegrationTest,CatalogListingIntegrationTest \
  test
```

Primo tentativo:363casi,0failure/4errori/0salti; conservato come
`targeted-attempt1*`. Due errori nel setup Booking, uno nella preparazione
rimborsi Balance e uno in apertura connessione (SocketTimeoutException).
Diagnosi `pg_ctl status` e `pg_stat_activity` sul solo cluster18: server attivo,
nessun backend test residuo bloccato. Identico comando, sorgenti e timeout:
**363/363**, exit0, `targeted*`. La causa dei timeout non è dimostrata;
nessuna correzione produttiva ricavata da questo solo tentativo.

```bash
python3 DriveHub/docs/evidence/r11-integrated/run-ui.py AgreementUiCheck
python3 DriveHub/docs/evidence/r11-integrated/run-ui.py SaleOutcomeUiCheck
python3 DriveHub/docs/evidence/r11-integrated/run-ui.py DestinationUiCheck
python3 DriveHub/docs/evidence/r11-integrated/run-ui.py IdentityUiCheck
python3 DriveHub/docs/evidence/r11-integrated/run-ui.py TableLifecycleUiCheck
python3 DriveHub/docs/evidence/r11-integrated/run-ui.py SlowModelsUiCheck
python3 DriveHub/docs/evidence/r11-integrated/run-ui.py AppStartupUiCheck
```

Eseguiti **in sequenza**, dopo Maven. Il launcher compila con javac21 e
JavaFX21.0.2/JDBC42.7.3 già in Maven; comandi completi nei log dei singoli harness.
Seed/check sono JVM distinte quando previsti. Sette esiti finali riusciti,
log e screenshot salvati prima dell'interruzione. Le copie degli harness
variano solo nei punti descritti dalle patch `*-harness.patch` e dagli hash
`*-inputs.json`:

- Percorsi fissi output05/06 sostituiti con directory18.
- Primo `SaleOutcomeUiCheck` exit2 nel loop modale JavaFX; non contato riuscito.
  Aggiunta stampa dell'errore originale prima del finally, assert invariati;
  riesecuzione riuscita. Il primo errore non è stato riprodotto.
- Primo `DestinationUiCheck` exit2: ClassCastException dimostra cast Vehicle
  obsoleto dopo14. Solo nella copia18, tabella tipizzata VehicleRow e letture
  ID tramite `vehicle()`, stessi assert e gesti: riesecuzione riuscita.
  L'harness storico12 rimane byte per byte invariato.

Render indipendente18: JAR PlantUML1.2026.2 già presente, SHA256 verificato da
renderer.json17; copia dei18puml in `/tmp/drivehub-ticket18-session/render`, poi
Java21 `-XX:+PerfDisableSharedMem -Djava.awt.headless=true -jar <jar> -charset
UTF-8 -failfast2 -tsvg <18puml>`. Comando completo in `render.log`: exit0,
18/18SVG identici ai consegnati (`render-results.json`). Nessun originale storico
o render nel progetto modificato.

## Ripresa dopo perdita di /tmp — fase2

Dopo l'interruzione richiesta durante la conversazione, env.json/strumenti/
cluster non erano più presenti. Tentativi di ricontrollo/avvio finale fermati
con FileNotFoundError prima di Maven; **nessuna suite eseguita** in quel tentativo.
Sentinella finale della fase1 non verificabile: limite in `resume-preservation.json`.
Le prove fase1 conservate non vengono rinominate come esecuzioni fase2.

Ricreati strumenti senza installazioni di sistema:

```bash
apt download openjdk-21-jdk-headless openjdk-21-jre-headless libpq5 libunwind8
# Download URL PostgreSQL16 e dipendenze: phase2-downloads.json
dpkg-deb -x <ciascun.deb> /tmp/drivehub-ticket16-tools/root
```

Versioni/hash effettivi in `phase2-tool-archives.json`; Java21 e PG16 come fase1.
Link del solo JDK estratto da `/etc/java-21-openjdk/*` reindirizzati ai file
estratti. `JAVA_HOME` e PATH al JDK21/PG16; LD_LIBRARY_PATH alle librerie estratte.
Xvfb non ricreato: tutte le prove GUI18 erano già concluse e conservate.

Gli1796file iniziali esistono ancora: fotografia iniziale ricostruita dai file
immutati e dall'inverso dei soli due aggiornamenti della sessione (piano/ticket18),
poi **tutti1796SHA256 confrontati con il manifest iniziale** prima di usarla per
il diff. Esito in `baseline-restored.json`. Non è una nuova baseline da HEAD.

Nuova copia del worktree, esclusi `.git`/target/relazione; solo config privata
modificata nella copia. Nuove password casuali e cluster inizializzato su porta
libera55436, stesso protocollo initdb UTF8/trust locale/SCRAM host e socket privato.
Nuovi ruoli, schema/demo/sentinella con gli script sopra. Gli output precedenti
di setup/versioni/sentinella/server sono `phase1-*`; quelli non prefissati
riguardano soltanto il nuovo cluster fase2.

```bash
python3 DriveHub/docs/evidence/r11-integrated/setup-db.py
python3 DriveHub/docs/evidence/r11-integrated/check-db.py before-suite
python3 DriveHub/docs/evidence/r11-integrated/run-maven.py full -Ppostgres-tests clean test
python3 DriveHub/docs/evidence/r11-integrated/check-db.py after-suite
pg_ctl -D /tmp/drivehub-ticket18-session/pgdata -m fast -w stop
```

`run-maven.py` registra input SHA256, log, exit e riepilogo XML reali; negli XML
conservati rimuove soltanto properties ambientali, mantiene testcase/esiti/output.
Il `clean` finale impedisce di sommare report mirati precedenti. Sentinella e
hash13tabelle/7sequenze riletti READ ONLY nello stesso cluster fase2 prima/dopo.
Arresto e assenza listener in `shutdown.txt`. La scomparsa del cluster fase1
non viene presentata come un arresto controllato.
