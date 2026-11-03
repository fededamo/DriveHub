# Comandi della verifica16

Eseguiti il3novembre2026, dalla copia
`/tmp/drivehub-ticket16-session/install/DriveHub`. Questa pagina registra
l'ambiente della sessione; la procedura generale è [installation.md](../../installation.md).

## Preparazione isolata

`/tmp` esisteva ma gli strumenti13–15 erano assenti. Nuovi pacchetti Ubuntu
scaricati ed estratti con `dpkg-deb -x`, senza installazioni o servizi di sistema.
[Versioni e SHA256 dei pacchetti](tool-archives.json).

- `apt download openjdk-21-jdk-headless openjdk-21-jre-headless xvfb xserver-common libxfont2 x11-xkb-utils libpq5`;
  poi `apt download libunwind8` e `apt download libfontenc1`.
- PostgreSQL16 e client16 da
  `https://archive.ubuntu.com/ubuntu/pool/main/p/postgresql-16/`,
  versione16.15-0ubuntu0.24.04.1.
- Dipendenze aggiuntive dall'archivio Ubuntu:
  `pool/main/i/icu/libicu74_74.2-1ubuntu3.1_amd64.deb`,
  `pool/main/l/llvm-toolchain-17/libllvm17t64_17.0.6-9ubuntu1_amd64.deb`,
  `pool/main/libx/libxml2/libxml2_2.9.14+dfsg-1.3ubuntu3.10_amd64.deb`.

Le prime richieste apt che includevano `libicu74` e `libxml2-2` non erano
risolvibili nei metadati della distribuzione corrente; nessuna build/test
fallita viene attribuita al prodotto. Le dipendenze sono state estratte dai
pacchetti indicati; `ldd` ha guidato l'aggiunta di libxml2/icu/unwind/fontenc.
La libpq18 scaricata dai metadati correnti è una libreria client; il server e
il client `psql` eseguiti sono16.15, verificati anche tramite SQL sul server.

Solo nella copia estratta del JDK, symlink da `/etc/java-21-openjdk/*`
reindirizzate ai corrispondenti file estratti. Solo nel binario Xvfb estratto,
la stringa `/usr/bin` del compilatore tastiera è stata sostituita con
`/tmp/x16`, contenente un link all'xkbcomp estratto. Nessun binario di sistema
modificato. `LD_LIBRARY_PATH` punta alle librerie estratte, `JAVA_HOME` al JDK21.

[snapshot.py](snapshot.py) ha salvato la copia iniziale e controllato i15ticket.
[prepare.py](prepare.py) ha copiato questa fotografia, generato credenziali
casuali private0600 e configurato un nuovo cluster con `initdb`: UTF8,
socket privato, host SCRAM, solo127.0.0.1:55434. Il file privato `env.json`
non è un'evidenza pubblicata e non va copiato nel repository. La directory
temporanea della sessione è stata ristretta a0700 al controllo finale.

```bash
pg_ctl -D /tmp/drivehub-ticket16-session/pgdata \
  -l /home/federico/dev/Drivehub/DriveHub/docs/evidence/r09-instructions/postgres.log -w start
# Comando completo registrato in xvfb-command.txt, su display92 libero
python3 /tmp/drivehub-ticket16-session/setup-db.py
```

[setup-db.py](setup-db.py) crea esclusivamente nuovi ruoli/DB dedicati;
inizializza lo schema applicativo vuoto con `psql -X -v ON_ERROR_STOP=1
--single-transaction -f .../schema.sql`, ne controlla client=0 e btree_gist,
poi carica demo con gli stessi flag. Aggiunge la sentinella soltanto nella
copia sacrificabile, distinta dal DB di test. Non usa DB sulla porta5432.

## Build, avvio e suite

Il [launcher Maven](run-maven.py) legge l'ambiente privato, esegue comandi
Maven reali nella copia, salva log/input e riepiloga XML. Non modifica src/POM;
nei soli XML conservati omette il blocco properties ambientale, mantenendo
testcase, esiti e output. Nessun nuovo test nella suite.

```bash
python3 /tmp/drivehub-ticket16-session/run-maven.py build -DskipTests clean package
python3 /tmp/drivehub-ticket16-session/run-maven.py without-db test
python3 /tmp/drivehub-ticket16-session/check-startup.py
python3 /tmp/drivehub-ticket16-session/check-db.py before-suite
python3 /tmp/drivehub-ticket16-session/run-maven.py full -Ppostgres-tests clean test
python3 /tmp/drivehub-ticket16-session/check-db.py after-suite
pg_ctl -D /tmp/drivehub-ticket16-session/pgdata -m fast -w stop
```

[check-startup.py](check-startup.py) compila gli harness15/14/05 già presenti
e il solo probe d'installazione [InstallationDbCheck.java](InstallationDbCheck.java).
Quest'ultimo apre la configurazione applicativa della copia, controlla DB/ruolo,
versione server e sentinella, poi autentica le tre identità demo tramite
AuthService. Non scrive o resetta il DB; non è un test sulla forma del testo.
AppStartupUiCheck invoca App.start, verifica tema/schede/Tab e salva screenshot.
Infine il launcher esegue due volte `mvn -B javafx:run`, attende la finestra
con titolo/classe App su Xvfb e registra PID tramite xprop. Arresta ciascun
gruppo di processi con SIGTERM: i goal sono interrotti dopo l'osservazione,
non dichiarati terminati spontaneamente con BUILD SUCCESS.

[check-db.py](check-db.py) apre connessioni READ ONLY alla copia applicativa,
controlla sentinella/identità e confronta gli hash delle righe ordinate di
tutte le13tabelle e lo stato delle7sequenze prima/dopo la suite. Privilegi e
proprietari vengono letti sul nuovo cluster. Il controllo di isolamento nella
suite tenta anche configurazioni di reset non sicure e verifica il loro rifiuto.

A fine prova SIGTERM al solo PID Xvfb92 registrato; `pg_ctl status`=3.
Controllo successivo di assenza dei listener55434/X92 e del processo display.
Rieseguire questi script richiede di predisporre nuovamente l'ambiente privato;
i percorsi temporanei non sono un requisito per usare DriveHub.
