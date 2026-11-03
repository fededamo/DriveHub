# Ticket 05 / R03 — prezzo consigliato e base manuale

Verifica del 31 ottobre 2026. Ticket 01–04 controllati `closed`; lavorato solo il
[ticket 05](../../../../TRACKER_DRIVEHUB/revisioni/issues/05-prezzo-consigliato-e-base-manuale.md).
**6/6 criteri soddisfatti**. Nessun commit: HEAD
`c2a81122ee6be44760ded4160fde229bd33e837c` invariato, indice senza staging.
Relazione_SWE esclusa da letture, ricerche, modifiche e verifiche.

## Perimetro e versione

Baseline dei file di lavoro acquisita prima delle modifiche, comprendente i ticket
precedenti: [hash iniziali](baseline-sha256.json). Il confronto riguarda questa
baseline, non `git diff HEAD`, che includerebbe il lavoro preesistente.
[Patch della sola sessione](session.patch), [16 file revisionati](files.txt),
[hash finali](hashes.json), [conservazione delle modifiche precedenti](preservation.json).
Dei **62 file locali preesistenti**, 57 sono identici; cambi intenzionali soltanto
in Vehicle, SaleToDealershipService, README, GLOSSARY e docs/testing.md.
Gli altri file dei ticket 01–04, compreso il protocollo dei conti, sono invariati.

Chiamanti verificati con ricerche mirate in `src`, prima degli interventi:

- Dominio: factory `Vehicle.create`, `getPrice`, `setPrice`; nuova modalità
  `PriceMode.AUTOMATIC/MANUAL` e ritorno `useRecommendedPrice`.
- Persistenza: tutti i SELECT, INSERT e UPDATE di PostgresVehicleDao, restituzione
  da save, VehicleDao, schema e demo. `updatePricing` modifica solo base/modalità,
  evitando di sovrascrivere stato o caratteristiche durante una scelta commerciale.
- CatalogService: creazione, selezione della base, consigliato, effettivo e modifica
  persistita. La base modello mancante non viene presentata come un prezzo finale.
- PriceCalculatorService: un solo algoritmo di sconti/svalutazioni, riusato da
  consigliato, effettivo e stima iniziale della cessione.
- PurchaseService: acconto e saldo invocano il calcolo condiviso; nessuna modifica
  al protocollo del conto o congelamento del totale.
- RentalService: preventivo e prenotazione invocano il calcolo condiviso; geometrica
  e date inclusive preservate. Anche cambio destinazione/rimborsi verificati nella suite.
- SaleToDealershipService: stima conservata nel deposito della richiesta, senza
  trasformarla in base già svalutata. Nuovi veicoli automatici; veicoli esistenti
  conservano la scelta commerciale del manager. Nessuna correzione di titolarità,
  richieste concorrenti o segno contabile dei ticket successivi.
- ManagerDashboardView: base del modello, modalità, base applicata, consigliato,
  effettivo; scelta per creazione e modifica, ritorno all'automatico.
- ClientDashboardView: base, consigliato, effettivo e tariffa iniziale distinti;
  conferma acquisto etichettata come prezzo effettivo attuale. SalesmanDashboardView
  non modifica né ricalcola prezzi: nessun adeguamento necessario.

`vehicle.price` conserva una base; `price_mode` è esplicita, con default AUTOMATIC
ed elenco di valori ammessi nel DB. In automatico conta la base corrente del modello,
non il valore memorizzato sul veicolo. Il manuale resta tale anche se le basi sono
uguali. Le basi manuali sono arrotondate a centesimi HALF_UP nel dominio, dopo aver
rifiutato gli input negativi; factory manuale e setter condividono questa regola.
La demo seleziona esplicitamente l'automatico e riallinea le basi ai modelli.

## Ambiente e isolamento

[Versioni](environment.txt): OpenJDK Linux 21.0.12.1, Maven 3.9.16, PostgreSQL 16.15,
JavaFX 21.0.2. Target POM 21 invariato. Riutilizzato l'ambiente R00 descritto in
[testing.md](../../testing.md), con variabili private caricate dal launcher locale.
Solo cluster sacrificabile `127.0.0.1:55432`, DB/ruolo `drivehub_test` e copia
applicativa distinta con sentinella. Nessun contatto al DB applicativo preesistente.
Reset, fixture e confronto sentinella sono passati nella suite finale.

Avvio e arresto con `pg_ctl` documentato, esecuzioni DB con escalation autorizzata.
Nessun mock o Singleton applicativo usato per certificare la persistenza. Le scritture
SQL nei nuovi test preparano fixture; gli esiti sono osservati tramite dominio e
servizi pubblici. Suite e controllo GUI eseguiti in sequenza sullo stesso DB dedicato.
Alla fine PostgreSQL e Xvfb sono stati arrestati.

## TDD e verifiche

Comandi sotto eseguiti dalla radice del progetto. Il launcher temporaneo usa JDK 21
e le variabili DB da `/tmp/drivehub-r00-env.json` senza stampare password.

| Comando (`python3 /tmp/drivehub-r00-maven.py <etichetta> ...`) | Esito | Evidenza |
|---|---|---|
| `ticket05-red -Dtest=PriceCalculatorServiceTest#manualBaseReceivesPromotionOnce test` | 1 failure attesa: 18000 invece di 9000 | [RED dominio](red.log) |
| `ticket05-green` con stessi argomenti | 1/1 | [GREEN dominio](green.log) |
| `ticket05-persistence-red -Ppostgres-tests -Dtest=PriceSelectionIntegrationTest test` | 1 failure attesa dopo salvataggio/rilettura: 18000 invece di 9000 | [RED catalogo](persistence-red.log) |
| `ticket05-persistence-green` con stessi argomenti | 1/1 | [GREEN catalogo](persistence-green.log) |
| `ticket05-cession-red -Ppostgres-tests -Dtest=PriceSelectionIntegrationTest#existingCessionKeepsManagerBaseSeparateFromEstimate test` | 1 failure attesa: base commerciale sovrascritta dalla stima | [RED cessione](cession-red.log) |
| `ticket05-targeted-final -Ppostgres-tests -Dtest=PriceCalculatorServiceTest,PriceSelectionIntegrationTest test` | 19/19 | [Mirati prima della review](targeted-final.log) |
| `ticket05-targeted-v3` con stessi argomenti | 19/19 dopo aggiornamento limitato ai campi prezzo | [Mirati DAO](targeted-v3.log) |
| `ticket05-full -Ppostgres-tests clean test` | 82/82, stato precedente al rilievo sui centesimi | [Prima suite completa](full.log) |
| `ticket05-rounding-red -Ppostgres-tests -Dtest=PriceCalculatorServiceTest#fractionalCentManualBaseIsRoundedBeforeApplyingFormula,PriceSelectionIntegrationTest#fractionalCentBaseHasSameEffectivePriceBeforeAndAfterReload test` | 2 failure attese: 9000.00 invece di 9000.01 | [RED centesimi](rounding-red.log) |
| `ticket05-rounding-green -Ppostgres-tests -Dtest=PriceCalculatorServiceTest,PriceSelectionIntegrationTest test` | 21/21 | [GREEN mirati finali](rounding-green.log) |
| `ticket05-final-full -Ppostgres-tests clean test` | **84/84**, zero failure/errori/saltati | [Suite finale](final-full.log), [XML Surefire](surefire/), [riepilogo](summary.json) |

I primi ampliamenti `ticket05-targeted` e `ticket05-targeted-v2` avevano 1 failure
nell'aspettativa del test: il gestore transazionale esistente avvolge
IllegalArgumentException in RuntimeException. Adeguata la prova al contratto
esistente, verificando anche la causa e la scelta persistita invariata. Nessuna
modifica a DatabaseManager. Log [primo](targeted.log), [secondo](targeted-v2.log).

La suite finale ricompila **63 sorgenti principali e 11 sorgenti test** con target
21. Comprende **33 prove senza DB e 51 PostgreSQL**, incluse le **16 prove di
concorrenza dei conti**, isolamento/sentinella, fixture e flussi preesistenti.
XML controllati: [84, 0, 0, 0] per test/failure/errori/saltati.

Casi verificati: 10000 e 10% → 9000, massimo fra sconti e tutte le svalutazioni,
manuale uguale alla base modello seguito da cambio del modello, ritorno al
consigliato riletto, zero e negativi (incluso -0.001) senza mutazioni, frazioni di
centesimo su setter/factory/creazione/modifica/rilettura, acquisto in entrambe le
modalità senza successive variazioni, noleggio a 1 e 3 giorni inclusivi, stima della
cessione indipendente dalla scelta commerciale prima e dopo l'approvazione.

## Controllo JavaFX e tastiera

[Sorgente eseguibile](PriceUiCheck.java), [launcher](run-ui.py), [log finale](ui.log).
Controllo rieseguito dopo l'ultima modifica al dominio: **UI CHECK SUCCESS**.
Costruttori pubblici delle viste, servizi reali e PostgreSQL dedicato; nessun mock.

JavaFX Robot su display Xvfb `:95` locale, senza TCP, rendering software. Pacchetti
ufficiali Ubuntu scaricati ed estratti solo in `/tmp/drivehub-ticket05-ui`:
`xvfb`, `xserver-common`, `libunwind8`, `libxfont2`, `libfontenc1`, `x11-xkb-utils`.
Una copia temporanea di Xvfb usa `/tmp/t05/xkbcomp` al posto del percorso hardcoded
`/usr/bin/xkbcomp`; nessuna installazione o modifica sotto `/usr`, nessun uso del
desktop dell'utente. Per ripetere nell'ambiente ancora disponibile: riavviare il DB
R00, riavviare la copia temporanea Xvfb con le sue librerie, copiare il sorgente
nella directory temporanea ed eseguire `python3 /tmp/drivehub-ticket05-ui/run-ui.py`.
Il controllo resetta **solo il DB dedicato** e richiede JDK21 e i JAR Maven in cache.

Verificati con tastiera reale generata da Robot:

- Selezione veicolo con frecce su/giù e focus visibile.
- Tab al checkbox automatico, Spazio per passare al manuale.
- Tab alla base abilitata, Ctrl+A e digitazione di 10000.
- Tab/Spazio sul salvataggio, Invio sul dialogo: rilettura del manuale e risultato 9000.
- Ritorno al checkbox automatico e salvataggio: modalità automatica riletta da PostgreSQL.
- Vista cliente: catalogo noleggio aperto con freccia destra sul TabPane.

Ispezionate visivamente le catture a 1280×760:
[manager automatico](manager-automatic.png), [manager manuale](manager-manual.png),
[cliente vendita](client-sale.png), [cliente noleggio](client-rent.png).
Etichette e colonne distinguono base, consigliato ed effettivo; nel noleggio sono
visibili anche i 9 €/giorno iniziali. Campo con label associata e testo accessibile;
checkbox e pulsanti standard conservano focus/tastiera. Form manager con FlowPane
per distribuire i controlli su più righe.

Il primo tentativo con tutta la demo ha superato il timeout iniziale di 20 secondi
al rendering; lo [stack diagnostico](rendering-threads.txt) mostra nuove connessioni
nelle celle. Il controllo finale usa una fixture mirata di due veicoli. Durante la
messa a punto è stato corretto anche il selettore del test, che individuava la
tabella di un tab non attivo. Questi tentativi non sono conteggiati come successi.
La prova finale certifica funzionalità e tastiera, non prestazioni con la demo
completa. Le query nel rendering e il caricamento asincrono restano al ticket 14.

## Revisione indipendente

Due agenti con contesti nuovi, skill `code-review`, sul diff fissato rispetto alla
baseline della sessione. Nessuna revisione include modifiche preesistenti dei ticket
01–04; nessun test PostgreSQL concorrente avviato dai revisori. Hash 16/16 verificati.

### Standards

**0 violazioni, 0 smell motivati.** Riuso di dominio, servizi, DAO e formula esistenti;
nessuna dipendenza o cambio architetturale. Aggiornamenti commerciali limitati a
base/modalità. Normalizzazione condivisa nel dominio dopo la validazione del segno.
Nessuna modifica al protocollo dei conti o implementazione di ticket successivi.

### Spec

La prima revisione ha trovato il caso delle frazioni di centesimo: 10000.005 veniva
conservato nell'oggetto mentre PostgreSQL lo arrotondava. Riprodotto con due RED,
corretto e coperto da GREEN e nuova suite completa. Revisione finale:
**0 rilievi residui**, nessun requisito funzionale mancante/parziale, implementazione
errata o scope creep. Setter e factory manuale condividono HALF_UP dopo validazione.

## Limiti e prossimo passo

- Schema ricreabile secondo D02; nessuna migrazione dei dati storici. Lo schema
  applicativo esistente deve essere adeguato prima di avviare il codice aggiornato;
  non è stato toccato da queste verifiche.
- Congelamento del totale concordato, transizioni/concorrenza delle richieste,
  titolarità/segno contabile cessione e rendering asincrono restano ai loro ticket.
- Tastiera e schermate controllate su Xvfb, non certificazione completa con screen
  reader o altri sistemi operativi. Prestazioni su catalogo completo non certificate.
- Warning Maven sul modello JavaFX preesistente; Xvfb ha emesso avvisi EGL sulla GPU
  non accessibile, con rendering software riuscito. Ambiente `/tmp` temporaneo.
- Nessun commit, staging o pubblicazione. Prossimo passo del piano: ticket 06 in una
  successiva richiesta; nessun altro ticket avviato in questa sessione.
