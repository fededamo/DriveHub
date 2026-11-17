# Perimetro operativo ricevuto — ticket 12

17 novembre 2026. Solo `docs/relazione/tracker/issues/12-riallineamento-testing.md`.
Obiettivo: rifare il capitolo conservando contenuti e scansione della relazione
originale, adattati esattamente al codice corrente e riusando il testo valido.
Coincidenza dei titoli insufficiente. Cinque sezioni e quindici voci della matrice,
con sei argomenti Business Logic e due sottosezioni DAO/schema.

Il messaggio operativo dell'utente precisa questi criteri aggiuntivi:

- Prima delle modifiche: working tree e baseline di materiali, non tracciati,
  PDF e storici. Leggere docs/AGENTS e docs/agents, piano/indice/matrice
  (4, frammenti, diagrammi), spec/ticket, glossario/ADR, istruzioni TeX,
  verifiche 06 e11, README/testing/installation/architecture/POM; confrontare
  originale, corrente, C, capitoli2/3 e A/B, codice/test/supporti/fixture/schema,
  fonti storiche di ogni esito usato.
- Strategia: rischi economici, livelli, funzionale/strutturale, oracoli/limiti.
  POM/JUnit/Surefire/Maven/profilo postgres-tests/TestDatabase effettivi.
  Nome IntegrationTest non crea ciclo Maven; ConfigurationTest può rifiutare
  prima di connessioni. Separare senzaDB, PostgreSQL e harness JavaFX esterni.
  Non ripristinare H2/Mockito/JaCoCo/Failsafe assenti.
- Ogni argomento Business Logic mantiene motivazione, preparazione, azione,
  oracolo, esempio significativo e limite: invarianti/stato dopo rifiuto,
  conto/sconti/anni/date incluse/fascia test drive; autenticazione senza vecchio
  unitario, login/email non provano registrazione pubblica atomica cliente/conto;
  prezzi base manuale/automatica, massimo, deprezzamento, geometrica/centesimi;
  PBKDF2 con prove effettive e lacune; RequestAgreement/SaleType in memoria,
  accordo/esiti/scritture PostgreSQL; rifiuto rimborso100%, scadenza penale20%
  dell'acconto, zero limitato; noleggio locale distinto da addebiti/conflitti/
  rimborsi/destinazione PostgreSQL; vecchio TransactionAndObserver assente,
  contratto reale/rollback dopo scritture, nessuna prova commit/rollback mock
  dedotta da erroreSQL.
- Persistenza: cosa salvato, confermato e riletto; DAO/contesto/Connection;
  oggetti Java distinti da stato persistito; tipi/enum/NULL/BigDecimal/date/
  agreed_total, non tutti i DAO per selezione; fixture/reset13 tabelle/demo/
  conteggi/sequenze senza bootstrap Java inventato; rollback/gare/isolamento,
  sincronizzazione/PID/oracoli/limiti, sentinella parziale non tutti i dati,
  gare selezionate non assenza universale di deadlock.
- Funzionale/UI: accesso/registrazione, catalogo/sconti, flussi commerciali,
  GUI/tabelle, lacune; FullWorkflow non tutti i workflow completi; main,
  Platform/Robot/check propri distinti da JUnit; provenienza/incompatibilità
  degli harness intatti; AppStartup solo ingresso, non tutte le dashboard.
- Esecuzione: comandi documentati distinti dagli eseguiti; fonte/versione o
  manifest/ambiente/data/comando/esito/limiti per ogni risultato; log/XML,
  invocazioni parametrizzate/classi/scenari distinti, niente somma o coverage.
  Conservare S0/E1/E2/E3, assenza manifest runtime/XML metodo E1, inputE2
  differenti, tentativi falliti, incompatibilità. C aggiornata solo quanto
  necessario per coerenza con2.2/A e metodi/assert/fonti/limiti; esempi nel corpo.
- Nove funzioni originali da censire, incluso comando shell: duplicato/effetti
  esclusi; tariffa; penale; addebito/Rent; transazione causa/soppressa/ordine;
  roundtrip nuova lettura; schema/demo/conteggi; BLOCKED/SOLD; Maven/esito.
  Conservare tre estratti validi; ogni estratto verificato letteralmente con
  file/intervallo/funzione/omissioni. Leggere originali dei sorgenti per fixture,
  chiamata/assert/ordine/rilettura/confine, non nomi o riassunti compressi.
- Tavole valide e ID conservati; qualsiasi intervento richiederebbe drawio,
  nativi/export/originali e verifica PDF. Nessun intervento se non necessario.
- Compilare tutto, controllare indice/rinvii/log e osservare tutte le pagine4/C
  e accessorie modificate. Documentare cinque messaggi TeX preesistenti inC:
  quali persistono/cambiano/scompaiono con log e PDF, senza attribuzione falsa.
- Conservare capitoli1–3, altre parti, diagrammi/autori/storici; nuove prove
  solo ticket 12, nessun vecchio manifest o checker storico contro documento nuovo.
- Review finale Standards/Spec con due subagenti a contesto iniziale separato;
  punto fisso baseline locale inclusi non tracciati, rapporti separati fino
  chiusura; risolvere rilievi e ricontrollare correzioni nel PDF conclusivo.
- Aggiornare ticket e soli stati pertinenti; done solo dopo tutti i criteri;
  link/impronte finali e conservazione. Chiusura tecnica diversa da autori.

Divieti: nessuna modifica a software/test/harness/schema/dati; nessuna build
Java, suite JUnit, harness, avvioUI o DB anche dedicato; nessun commit/invio/
pubblicazione. Ammessi controlli documentali, TeX e renderingPDF. Difetti
separati senza patch. Nessun pacchetto finale o integrazione del ticket 13.
Toolchain11 temporanea da verificare, non presunta disponibile.
