# Ticket 03 — Convertire i dati esistenti conservando pratiche, storico e saldi

Baseline iniziale pulita: `f4a7f0e`, con ticket 01 (`a260598`) e 02 già completati.
Fonte: **sezione 03** di `TRACKER_DRIVEHUB/semantica-operazioni/proposta-ticket.md`
e relativa `spec.md`, nel tracker esterno. Nessun file individuale del ticket è
stato presunto o creato. Esecuzioni: 8–9 novembre 2026, fuso Europe/Rome.

## Contratto sorgente e implementazione

Il contratto è stato ricavato dalla storia Git, direttamente da `a260598^` =
`841d4bce1dc8b5217f6f2e2a39fc42b09b5da044`. La fotografia dello schema nei test è
identica al blob originale `2d37366f0805b21020a7f14152d8043b15e21aca`, verificato
con `git hash-object` e `git rev-parse`. [Provenienza e SHA256](source-contract.json).
Controllati anche dominio, factory, servizio di cessione e query dell'ultimo
compratore nella stessa revisione: non si è dedotta la vecchia semantica dai
nomi o dal solo segno delle righe.

La vecchia richiesta `PURCHASE` conservava acconto/totale dell'acquisto del
cliente; la factory `SALE` salvava la stima non positiva. Il vecchio registro
usava `PURCHASE` per il compratore, `ACQUISITION` non positiva per la cessione e
`PENALTY` non negativa. Il vecchio servizio accreditava il valore assoluto della
stima; la ricerca del titolare selezionava il vecchio `PURCHASE`.

[Script consegnato](../../../src/main/resources/db/convert-operation-semantics.sql):
riconoscimento rigoroso di colonne, precisione, enum e vincoli, controlli di
quiescenza/proprietà, lock, trasformazione dal tipo originario e sostituzione dei
tre CHECK nella stessa transazione. Il contratto finale riconosciuto è una
seconda esecuzione senza scritture; i due contratti intermedi sono rifiutati.
Trigger/regole personalizzati e sicurezza per riga sulle tabelle coinvolte
sono rifiutati, per evitare effetti laterali non presenti nella versione sorgente.
Nessun aggiornamento di `bank_account`, movimento o chiamata a servizi applicativi.
Nessuna modifica ai contratti applicativi già implementati, nuove dipendenze,
colonne, tabelle di controllo o framework di migrazione.

## Prove PostgreSQL e servizi pubblici

`OperationSemanticsConversionIntegrationTest`: **25/25** nella suite finale.
La fixture ha sei richieste (entrambi i tipi, approvate/non approvate, futura e
scaduta, acconto/stima zero), otto operazioni (tutti i tipi anche a zero), due
conti e vendite storiche con un'acquisizione più recente di un altro cliente.
Include noleggio e test drive estranei alla conversione, FK opzionali nulle e
sequenze con valori distinti dagli ID massimi, compresa una con `is_called=false`.

| Requisito | Evidenza osservata |
|---|---|
| Conservazione | Confronto completo di tutte le tabelle, esclusi soltanto i campi trasformati, poi confrontati con valori letterali; conteggi, associazioni, date, approvazioni, scadenze, accordi e saldi identici; proprietà e stato delle sequenze identici |
| Zero esplicito | Entrambi i tipi di richiesta e tutti i tipi di registro riclassificati dal valore originario, anche a zero |
| Rollback dopo modifiche | Iniezione SQL prima del vero `COMMIT`: osserva richieste e registro già convertiti e CHECK finale già presente, poi solleva `conversion_after_real_writes`; confronto integrale dimostra ripristino di dati e vecchi vincoli, quindi riesecuzione riuscita |
| Ripetizione | Seconda esecuzione senza differenze; anche lo schema finale appena inizializzato viene riconosciuto senza modifiche |
| Origine sconosciuta | Rifiuto senza differenze per vincoli mancanti/aggiuntivi, precisione diversa, enum esteso, vecchia stima positiva, NaN, entrambi i contratti intermedi effettivi e trigger personalizzato |
| Applicazione ferma | Una connessione applicativa aperta impedisce la conversione senza modifiche |
| Accordo convertito | Totale 20.000/acconto 4.000, cambio di base a 35.000 e listino a 30.000: saldo ancora 16.000, nuova vendita `SALE` da 20.000, conto da 26.000 a 10.000; ripetere il saldo fallisce |
| Cessione convertita | Stima 16.000, un solo `PURCHASE` da 16.000, conto da 26.000 a 42.000; seconda approvazione rifiutata senza doppio accredito |
| Scadenza convertita | Un rimborso di 3.200 e una sola `PENALTY` da 800; conto da 26.000 a 29.200 |
| Ultimo compratore | Vecchio compratore corretto dopo la conversione; vendita più vecchia e acquisto della concessionaria più recente ignorati; vendita a zero riconosciuta |
| Nuovi input | CHECK finali respingono sette aggiornamenti non validi, compresi importi negativi per ciascun tipo del registro; dominio/factory restano coperti dalla suite esistente |

La prova di rollback inserisce l'errore nel comando SQL di test prima del commit,
senza introdurre un punto di iniezione nell'applicazione o nello script consegnato.
Il loader della fotografia sorgente riusa le verifiche di destinazione/privilegi
di `TestDatabase`, senza fallback alla configurazione applicativa.

## Ambiente, comandi ed esiti

OpenJDK 21.0.12.1, Maven 3.9.16, PostgreSQL 16.15, JavaFX 21.0.2, Linux/WSL.
Riutilizzati i binari estratti sotto `/tmp/drivehub-ticket01/tools`; cluster
temporaneo dedicato `127.0.0.1:55437`, ruolo/database `drivehub_test` e distinta
copia sacrificabile `drivehub_sentinel`. Variabili private come in
[testing.md](../../testing.md); nessuna configurazione applicativa modificata.
Il sandbox blocca le connessioni locali: le prove DB/UI sono state autorizzate
fuori dal sandbox esclusivamente per queste destinazioni dedicate.

```bash
git status --short
git log -4 --oneline
git show a260598^:src/main/resources/db/schema.sql
git diff a260598^..f4a7f0e -- src/main/resources/db/schema.sql
git hash-object src/test/resources/db/pre-semantics-841d4bc-schema.sql
git rev-parse a260598^:src/main/resources/db/schema.sql
mvn -o -DskipTests test-compile
mvn -o -Ppostgres-tests -Dtest=OperationSemanticsConversionIntegrationTest test
DISPLAY=:98 DRIVEHUB_UI_DIR=/tmp/drivehub-ticket03/ui-complete python3 docs/evidence/data-conversion-03/run-ui.py
mvn -o -B -Ppostgres-tests clean test
git diff --check f4a7f0e -- . ':!src/test/resources/db/pre-semantics-841d4bc-schema.sql'
```

Il primo tentativo DB nel sandbox fallisce per connessione impedita; rieseguito
nel cluster dedicato il [test red](conversion-red.txt) raggiunge la conversione
mancante. Il [primo green](conversion-green.txt) passa 1/1. Le
[prove estese](conversion-expanded.txt) rilevano una connessione JDBC appena
chiusa il cui backend non era ancora uscito; il coordinamento della fixture
attende la quiescenza nella stessa connessione che eseguirà lo script, mantenendo
il controllo rigoroso nello script. Seguono [23/23 green](conversion-final.txt);
aggiunti poi riconoscimento dell'installazione finale e rifiuto NaN nel registro.

Risultato integrato definitivo: **444/444, zero fallimenti, errori o saltati,
23 classi**, in 1 minuto e 19 secondi. [Log](full-suite.txt),
[conteggi letti dagli XML Surefire](summary.json). Comprende dominio, flussi,
concorrenza, vincoli, demo e isolamento con sentinella. Compilazione Java 21
con `-Xlint:all`, senza avvisi di compilazione; Maven segnala sei problemi del
modello della dipendenza JavaFX durante la risoluzione. Nei log archiviati sono rimossi
soltanto gli spazi a fine riga e le righe vuote finali dei due output CLI.
Il controllo whitespace esclude soltanto la fotografia dello schema sorgente:
la sua riga vuota finale è conservata per mantenere l'identità del blob storico.

Provato anche il comando operativo `psql -X -v ON_ERROR_STOP=1 -f
src/main/resources/db/convert-operation-semantics.sql` sul solo `drivehub_test`,
dopo la suite. La fotografia sorgente è stata preparata con i due SQL sotto
`src/test/resources/db/`, in transazione; **non** con schema/demo correnti come
strumento di aggiornamento. [Preparazione](cli-source.txt),
[due esecuzioni, identità, versione e righe finali](cli-conversion.txt): prima
conversione riuscita, seconda senza modifiche, conti 26.000 e 1.000 prima/dopo.

## JavaFX e limiti

[Harness](ConvertedDataUiCheck.java) e [launcher](run-ui.py) riusano Robot,
schermate e utility delle prove precedenti, senza modificarle. La fase seed
carica la fotografia sorgente nel solo DB dedicato, converte due volte e cambia
il listino. Una seconda JVM usa dashboard e controlli reali.
Display Xvfb isolato `:98`, 1440×1000, senza listener TCP/socket su filesystem,
rendering software. Copia temporanea del binario con `/tmp/xkb` per xkbcomp;
il processo del display è arrestato alla fine della prova.

Esito: **UI CHECK SUCCESS**. [Log completo](ui-converted.txt).
Verificati e ispezionati visivamente:

- [Richieste venditore](seller-converted-requests.png): entrambi i tipi,
  stime 16.000 e zero, saldo 16.000, cessione accettata da tastiera e conto 42.000.
- [Avviso scadenza](client-converted-expiry.png): rimborso 3.200 e conto 45.200.
- [Richieste cliente](client-converted-requests.png) e
  [conferma saldo](client-converted-confirmation.png): filtro `SALE`, accordo
  20.000/4.000/16.000 dopo cambio listino, selezione con Tab/frecce.
- [Acquisto concluso](client-converted-completed.png): conto 29.200 nel flusso
  combinato cessione + scadenza + saldo; [registro](manager-converted-ledger.png)
  con otto righe convertite e tre nuovi esiti, tre tipi anche a zero, importi
  non negativi, navigazione fino all'ultima riga e testo selezionato leggibile.

I tentativi grafici precedenti sono conservati: [display non pronto](ui-display-failed.txt),
[atteso iniziale sul formato del rimborso](ui-first.txt),
[focus dopo dialogo](ui-focus-first.txt). Corretto soltanto l'harness: il messaggio
esistente usa `3200.00`, e il focus viene raggiunto con Tab nella finestra
riattivata. La tabella cliente costruita prima della gestione automatica della
scadenza può mostrare la riga scaduta fino al successivo refresh ordinario;
la pratica è già rimossa dal DB e il saldo successivo aggiorna la vista. Questo
comportamento preesistente non è modificato dal ticket di conversione.

Non applicata alcuna conversione al database dell'utente. Non verificati schemi
personalizzati, altre versioni PostgreSQL, altri sistemi operativi, screen reader
o volumi da produzione. L'aggiornamento richiede una finestra con applicazione
ferma e un backup verificato; dopo un commit il ritorno alla sorgente richiede
il ripristino completo prima di nuovi movimenti. Evidenze storiche e
`Relazione_SWE` conservate integralmente. [Istruzioni operative](../../data-conversion.md).
Al termine sono stati arrestati il cluster PostgreSQL temporaneo e il display
Xvfb avviati per queste prove.

## Revisione indipendente

Skill `mattpocock-skills:code-review`, confronto rispetto a `f4a7f0e`, con due
agenti indipendenti. Nessuna modifica al codice dopo la suite integrata;
l'aggiornamento conclusivo aggiunge soltanto questo resoconto alle evidenze.

### Standards

Nessuna violazione documentata né smell azionabile rilevato.

Il nuovo overload conserva le verifiche di isolamento del loader; conversione
e vincoli restano nella stessa transazione. La fotografia dello schema ha lo
stesso blob Git dell'originale `841d4bc`:
`2d37366f0805b21020a7f14152d8043b15e21aca`.

Le evidenze archiviate riportano 444 test superati e `UI CHECK SUCCESS`,
conservando anche i tentativi falliti e documentando i limiti. Il revisore non
ha eseguito test DB/UI né modificato file; la revisione statica non verifica
nuovamente gli esiti archiviati o gli ambienti esclusi dalle prove.

### Spec

Nessun rilievo azionabile sull'asse Spec per il ticket 03.

Il diff rispetta la conversione diretta richiesta: riconosce il contratto
precedente a entrambi i ticket, riclassifica dal tipo originario anche gli
importi zero e aggiorna dati e vincoli nella stessa transazione. Non modifica
conti, identificativi, associazioni o campi dell'accordo.

Verificata indipendentemente l'identità dello schema sorgente dei test con il
blob Git di `841d4bc`. Factory e servizio storici confermano stime non positive
e accredito dal valore assoluto. Le prove includono rollback dopo modifiche
effettive, ripetizione, contratti intermedi rifiutati, conservazione delle
sequenze, accordo 20.000/4.000/16.000, accredito unico e ultimo compratore.
I log riportano 444 test riusciti, JavaFX completato e doppia esecuzione `psql`
con saldi invariati; documentazione e limiti risultano coerenti. Nessuna
estensione ingiustificata dello scope.

Il revisore non ha eseguito autonomamente test DB/UI né modificato file.
Resta il limite dichiarato: origine `841d4bc`, applicazione ferma e backup
verificato. Totali della revisione: **Standards 0; Spec 0**.
