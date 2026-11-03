# Ticket 14 — tabelle reattive

Solo ticket14, prerequisito05 e ticket01–13 controllati chiusi prima degli
interventi. Letti AGENTS, piano, glossario, ADR0001, testing, specifica ed
[evidenze13](../r07-identity/verification.md). Confini già approvati: servizi
pubblici con PostgreSQL reale e costruttori/controlli pubblici JavaFX con Robot.
Nessun commit/staging; nessun ticket successivo; `Relazione_SWE` sempre esclusa.

## Baseline e conservazione

Fotografia iniziale **1089 file** in `/tmp/drivehub-ticket14-session/before`,
progetto, piano e tracker esterni, senza `.git`, `target` e `Relazione_SWE`.
[Manifest](baseline-sha256.json), [HEAD](initial-head.txt),
[indice](initial-index.sha256). Il diff da HEAD includerebbe il lavoro01–13:
si confronta soltanto la fotografia della sessione.

Nuova baseline effettiva **390/390**, zero failure/errori/salti,47 senza DB e
343 PostgreSQL, prima delle modifiche ai sorgenti/test; coincide nel conteggio
con il risultato storico13 ma è una nuova esecuzione:
[log](baseline.log), [XML](baseline-surefire/), [input](baseline-files.sha256.json).
Il lavoro precedente e le evidenze storiche non vengono riscritti.

## Perimetro e responsabilità

| Confine/chiamanti | Modifica e verifica |
|---|---|
| `CatalogService.getVehicleRows` | Una lettura collettiva per modelli, sconti modello, sconti veicolo e veicoli: quattro query indipendenti dal numero di righe. Prepara descrizione, base, consigliato, effettivo e tariffa con il calcolatore esistente; nessuna cache persistente |
| `getModelDiscountRows` | Due letture collettive preparano nome e sconto, senza query delle celle |
| `ClientDashboardView` | Due caricamenti indipendenti vendita/noleggio, celle con soli dati preparati. Anche selettori marca/modello asincroni, con invalidazione della marca svuotata. Acquisto, noleggio e test drive continuano a usare i servizi pubblici e il veicolo della riga |
| `ManagerDashboardView` | Tabelle e selettori riusano i dati caricati. Modifica base aggiorna i veicoli; modifica/rimozione sconti aggiorna anche i prezzi. Celle dei modelli scontati leggono la descrizione preparata |
| `SalesmanDashboardView` | Inventario con descrizioni preparate; dopo approvazione/rifiuto e cambio destinazione ricarica il listino. Comandi e filtri SALE/RENT conservati |
| `AsyncLoad` | `Task` JavaFX, worker virtuale21, stato/progresso e Aggiorna/Riprova da tastiera. Solo il task corrente pubblica, sul thread JavaFX. Sostituzione e distacco cancellano il task precedente; dati del controllo disabilitati durante caricamento/errore |
| Persistenza/prezzi | Nessuna modifica a DAO, schema, demo, AppContext, formule o accordi. I metodi di prezzo esistenti rimangono per i comandi. Nessuna dipendenza nuova, nessuna riscrittura generale |

Nessun grafo Graphify presente, nessuna generazione necessaria per queste
relazioni verificate sul codice. Tutti i chiamanti delle tabelle convertite e
le selezioni dei relativi comandi sono stati ricontrollati.

## Ambiente ricontrollato

Il Java predefinito è17: non usato per le prove. Riutilizzati soltanto dopo
verifica i file13 ancora presenti: Java Linux **21.0.12.1**, Maven **3.9.16**,
PostgreSQL **16.15**, binari estratti in `/tmp/drivehub-ticket13-tools/root`.
Solo cluster sacrificabile `/tmp/drivehub-ticket13-pgdata`, **127.0.0.1:55433**,
ruolo/DB `drivehub_test` non amministrativo e copia applicativa sentinella
separata. Sei variabili private copiate in `/tmp/drivehub-ticket14-session/env.json`
0600, mai nelle evidenze. Nessun DB applicativo preesistente contattato.

Xvfb isolato **:94**,1440×1000, rendering software, senza TCP, solo socket locale
astratto. I primi avvii con listener filesystem fallirono; un processo shell
non rimaneva attivo tra comandi. Risolto avviando esplicitamente il processo
distaccato e disabilitando il listener Unix filesystem, senza cambiare socket
altrui. Il primo harness restò nell'uscita JavaFX dopo il fallimento del display;
aggiunto soltanto reporting immediato dell'errore nell'harness. Tentativi separati
conservati, **non sono RED del prodotto**. Warning EGL preesistenti su `/dev/dri`.
L'ambiente è temporaneo: se /tmp scompare occorre ricrearlo secondo testing.

## TDD e mirati

Launcher privato `python3 /tmp/drivehub-ticket14-session/maven.py <etichetta>
<argomenti>`: Java21, variabili private, `mvn -B -o`, log/XML e hash input.
[Comandi portabili](../../testing.md#tabelle-reattive--ticket-14).

| Esecuzione | Risultato |
|---|---|
| baseline `-Ppostgres-tests clean test` |390/390, zero failure/errori/salti |
| render-red-final / render-green |Stesso harness:856 connessioni e query nelle celle/scorrimento →0/0 |
| slow-red / slow-green |Stesso harness: query `model WHERE brand` bloccata, PID/blocker osservati, selezione sul thread grafico in timeout →selezione ritorna e finestra risponde con lock ancora tenuto |
| obsolete-red / obsolete-green-fixed |Stesso harness esteso: la vecchia query ripopola i modelli senza marca →selezione vuota prevale; caricamento in PostgreSQL osservato prima di svuotare |
| listing `-Ppostgres-tests -Dtest=CatalogListingIntegrationTest,PriceCalculatorServiceTest,PriceSelectionIntegrationTest test` |25 casi,1 errore della nuova fixture: veicolo2025 su modello2026. Corretto solo l'anno rilascio del modello della fixture |
| listing-fixed, stesso comando |25/25 |
| targeted, selezione sotto |148/148, zero failure/errori/salti |

Le tre coppie conservano sorgenti identici RED/GREEN; gli ampliamenti successivi
sono verifiche estese, non vengono presentati come RED precedenti. Le copie del
sorgente sono fotografate alla fine di ciascuna coppia. Un tentativo del terzo
GREEN non compilava il nuovo harness esteso per wildcard ComboBox; corretto solo
l'harness, non attribuito al prodotto.

Selezione `targeted`:

```bash
mvn -Ppostgres-tests -Dtest=CatalogListingIntegrationTest,PriceCalculatorServiceTest,PriceSelectionIntegrationTest,PurchaseAgreementIntegrationTest,PurchaseConcurrencyIntegrationTest,BalanceConcurrencyIntegrationTest,VehicleDestinationIntegrationTest,DemoSqlValidationTest,DatabaseIsolationIntegrationTest test
```

I quattro nuovi test PostgreSQL verificano valori letterali: automatico20.000
con10%→18.000; manuale10.000→9.000; sconto veicolo20%→8.000 e consigliato16.000;
rimozione sconti e ritorno all'automatico; manuale10.000,005 arrotondato prima
formula→9.000,01; nuova base modello30.000→consigliato27.000 con manuale invariato;
usato, anno e20.000km→5.776 e tariffa5,78; filtro stato e nuova sessione.
SQL aggiuntivo prepara fixture, nessun DAO simulato certifica la persistenza.

## GUI, caricamenti e tastiera

[Harness rendering/JDBC](ReactiveTablesUiCheck.java),
[harness modelli lenti/obsoleti](SlowModelsUiCheck.java),
[harness ciclo completo](TableLifecycleUiCheck.java), [launcher](run-ui.py).
Ciclo completo iniziale e finale: **UI CHECK SUCCESS**.

Il driver diagnostico delega a PostgreSQL reale e conta sia connessioni sia
esecuzioni; le celle di tutte le tabelle manager, catalogo vendita/noleggio
cliente e inventario venditore sono percorse, rinfrescate e scorse: **0/0**.
Una lettura collettiva di33 veicoli richiede quattro query/connessioni totali.

Il lock esclusivo su `model` trattiene la lettura, `pg_stat_activity` e
`pg_blocking_pids` osservano backend e blocker prima delle asserzioni. Stato
"Caricamento in corso…", frame e spostamento della scheda con frecce funzionano
mentre il lock è ancora tenuto. Nessuno sleep simula i caricamenti.

Errore SQL reale: tabella `vehicle` temporaneamente rinominata nel DB dedicato,
sempre ripristinata in `finally`. Messaggio inline, dati disabilitati e Riprova;
Spazio sul pulsante completa il nuovo caricamento. Per gli obsoleti un gate
trattiene dopo l'esecuzione una vera query JDBC e ignora l'interruzione soltanto
nella prova; la seconda lettura mostra11.700/12.600, poi si rilascia la vecchia
lettura, si osserva la chiusura della sua connessione e si verificano prezzi e
stato invariati, sia per risultato vecchio sia per errore vecchio. Il solo errore
obsoleto è iniettato al confine JDBC dopo la query; non certifica effetti SQL.
Listener pubblici osservano gli aggiornamenti sul thread JavaFX.

Robot05 riusato: frecce per schede/righe, Tab ai controlli, Ctrl+A e cifre per
base manuale, Spazio su applicazione e riprova, Invio sull'alert. Base10.000→12.000
produce10.800 mantenendo consigliato18.000; sconto modello20% aggiorna anche la
tabella veicoli a16.000/11.200. Vendita/noleggio cliente verificano entrambi i
prezzi e tariffa8,00/giorno; nella vista cliente già aperta il ritorno all'automatico
e il successivo cambio base/nome modello vengono riletti correttamente.
Attese di caricamento tramite proprietà/latch, frame tramite AnimationTimer;
le pause ereditate Robot riguardano soltanto gesti e rendering.

Sette schermate ispezionate: manager prezzi, caricamento, errore/riprova,
vendita, noleggio, aggiornamento cliente e inventario venditore. Stato e focus
visibili, importi leggibili. A1280px il titolo manager "Prezzo consigliato" è
abbreviato dalla larghezza della colonna, come prima: testo completo conservato
e colonna ridimensionabile; non si amplia questo ticket in un redesign.
Nessuna certificazione screen reader, altri display/OS o benchmark quantitativo.
La reattività qui è verificata con query bloccata e gesti/frame riusciti.
La cancellazione non promette interruzione immediata di JDBC già avviato;
il risultato è scartato e le risorse chiuse dal worker.

## Verifica finale

`python3 /tmp/drivehub-ticket14-session/maven.py final-full -Ppostgres-tests clean test`
→ **394/394**,47 senzaDB/347 PostgreSQL, zero failure/errori/salti:
[log](final-full.log), [XML](final-full-surefire/), [input](final-full-files.sha256.json).
Include tutte le regressioni01–13, fixture e isolamento/sentinella. Nuovi4/4,
mirati25/25 e148/148; nessun sorgente runtime modificato dopo i mirati.

GUI finale **UI CHECK SUCCESS**:
[log ciclo](final-lifecycle-evidence.log), [input ciclo](final-lifecycle-evidence-files.sha256.json),
[log modelli](final-models.log), [input modelli](final-models-files.sha256.json).
Sette schermate finali ispezionate:
[manager](manager-prices.png), [caricamento](loading.png), [riprova](retry.png),
[vendita](client-sale.png), [noleggio](client-rent.png),
[cliente aggiornato](client-refreshed.png), [inventario](salesman-inventory.png).
[Identità/privilegi dedicati](environment.txt), [versioni](versions.txt),
[sentinella finale in sola lettura](sentinel-final.txt), [arresto](shutdown.txt).
Nessun DB applicativo preesistente contattato; PostgreSQL e Xvfb arrestati.

Review indipendenti [Standards](standards-review.md) **0 violazioni/0 smell
azionabili**, [Spec](spec-review.md) **0 rilievi**, sul diff congelato della sola
sessione: [patch](review.patch), [elenco](review-files.txt),
[hash](review-files.sha256.json),14file, SHA256
`b7cf51719edf2beffd4c2b4e0130e989e31f1a5a928518c313992e8ddd827c5f`.
Nessun codice/test/harness cambiato dopo prove finali e review. Il delta di
chiusura documentale è separato dalle prove runtime:
[patch](closing-docs.patch). Il [diff finale](session.patch) include la chiusura;
[hash finali](session-files.sha256.json), [riepilogo](summary.json),
[regressioni identiche](regression-identity.json), [prerequisiti](prerequisites.json).

[Conservazione](preservation.json):1089file iniziali presenti,1082 identici,
7 aggiornamenti intenzionali,zero eliminazioni. Ticket/evidenze01–13 e successivi
intatti; HEAD/indice invariati. Controllati whitespace del diff della sessione,
link locali, input runtime/GUI, integrità della fotografia e assenza delle
credenziali private nelle prove. Le modifiche documentali non sono nuove prove.

**5/5 criteri verificati, ticket14 chiuso, R08 e AC16/#16 completati,14/20
rilievi chiusi.** AC13 resta trasversale. Nessun ticket successivo,
nessun commit/staging; `Relazione_SWE` sempre esclusa.
