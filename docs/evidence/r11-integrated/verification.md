# Ticket18 — verifica integrata e tracciabilità

Sessione del3novembre2026, soltanto18/R11. Letti AGENTS, piano, specifica,
glossario, ADR0001, testing, installation, architecture ed evidenze16–17.
[01–17 verificati chiusi](prerequisites.json), senza criteri aperti.
`Relazione_SWE` esclusa da letture, ricerche, copia, generazioni e verifiche.
Nessuno staging, commit, push o pubblicazione.

La [matrice AC01–AC20](acceptance-matrix.md) collega ogni rilievo a ticket,
comportamento, percorso principale/alternativo/errore e prove **nominate**.
Il [catalogo dei394casi eseguiti](executed-tests.json) e gli XML permettono di
risalire anche ai parametri effettivi. Login/caricamenti di FullWorkflowIntegrationTest
sono distinti dai flussi economici completi esercitati negli altri test.
Non è una percentuale di copertura, né una promessa su ogni gara o gesto UI.

## Versione, input e conservazione

HEAD iniziale `c2a81122ee6be44760ded4160fde229bd33e837c` **non identifica da solo
la versione verificata**. [Fotografia iniziale](baseline-sha256.json):1796file
di progetto, tracker e piano, inclusi modificati/non tracciati, target e326
ignorati pertinenti; `.git` e relazione esclusi. [Stato](initial-status.txt),
[ignorati](initial-ignored.txt), [HEAD](initial-head.txt), [indice](initial-index.sha256).
Copia privata iniziale in `/tmp/drivehub-ticket18-session/before`.

L'interruzione ha cancellato `/tmp`. Prima del diff, fotografia ricostruita
dai file immutati e dall'inverso dei soli due aggiornamenti di sessione già
eseguiti a piano/ticket18: **1796/1796SHA256 nuovamente identici all'iniziale**,
[riscontro](baseline-restored.json). Nessuna sostituzione con un checkoutHEAD.
La copia privata e gli strumenti sono temporanei; i manifest/prove persistono qui.

Entrambe le copie runtime derivano dal worktree locale, con sola risorsa
`application.properties` privata diversa. [Identità fase1](runtime-identity.json),
[identità finale e confronto fra fasi](final-runtime-identity.json):93input
produttivi, nessun cambiamento a Java/POM/test/schema/demo fra mirati, GUI e
suite finale; unica differenza nei manifest di esecuzione è la configurazione
privata dei nuovi DB. Questi input sono identici anche al17, ma ciò **non**
trasforma i suoi risultati in esecuzioni18.

Nessuna regressione produttiva dimostrata, quindi nessuna modifica a sorgenti,
testJUnit, POM, schema/demo o diagrammi. Aggiornati matrice/prove18, indice delle
evidenze, rinvio in testing, piano e ticket18. Nessun test artificiale del testo.
Il delta è rispetto alla fotografia locale: [patch della sessione](session.patch),
[hash file](session-files.sha256.json), [conservazione](preservation.json).

## Ambiente ed esiti effettivi18

[Comandi e tentativi](commands.md). Java/javac**21.0.12.1**, Maven**3.9.16** sullo
stesso JDK, PostgreSQL server/client**16.15**, nuovo cluster solo**127.0.0.1:55436**,
ruoli non amministrativi `drivehub_test` e `drivehub` su DB omonimi distinti.
Nessun accesso a un DB applicativo preesistente. Configurazioni private0600,
directory sessione0700, nessun segreto pubblicato.

**Fase1:** strumenti temporanei16 presenti e ricontrollati, Xvfb isolato**:90**,
1440×1000, socket astratto locale e niente TCP. [Hash binari](tools-rechecked.json),
[versioni](phase1-versions.txt), [comandoXvfb](xvfb-command.txt).
**Fase2:** dopo perdita di /tmp, JDK/PG e dipendenze nuovamente scaricati/estratti,
nessuna installazione di sistema; [pacchetti/hash](phase2-tool-archives.json),
[URL download](phase2-downloads.json), [versioni](versions.txt).
Nuovo cluster, nuove credenziali e nuova sentinella. Nessun Xvfb o nuovo giro GUI
in fase2; gli input Java/POM/test sono rimasti identici alle prove GUI fase1.

| Prova nuova18 | Risultato effettivo |
|---|---|
| Mirati, primo tentativo fase1 |363casi,0failure/**4errori**/0salti; timeout fixture/connessione, non riuscito. [Log](targeted-attempt1.log), [XML](targeted-attempt1-surefire/), [summary](targeted-attempt1-summary.json). Causa non dimostrata, nessuna correzione produttiva dedotta. |
| Mirati identici, seconda esecuzione fase1 |**363/363**,0failure/errori/salti, exit0; [log](targeted.log), [XML](targeted-surefire/), [summary](targeted-summary.json), [input](targeted-inputs.sha256.json). Gare reali e rollback inclusi. |
| JavaFX accordo/acquisto |`AgreementUiCheck` **UI CHECK SUCCESS**; seed/check in JVM distinte, approvazione e saldo20000/4000/16000, nuovo accordo distinto; [log](AgreementUiCheck.log). |
| JavaFX cessione/registro |`SaleOutcomeUiCheck` **UI CHECK SUCCESS**, accredito singolo, vista obsoleta, rifiuti con/senza storico, cliente e registro manager con tipi/segni; [log](SaleOutcomeUiCheck.log). Primo tentativo exit2 nel loop modale conservato separato e non contato. |
| JavaFX destinazione/rimborsi |`DestinationUiCheck` **UI CHECK SUCCESS**, futuro multi-cliente, noleggio iniziato ieri, BLOCKED obsoleto, cambio opposto, conto mancante/rollback; [log](DestinationUiCheck.log). Primo tentativo ClassCastException dell'harness12 conservato separatamente. |
| JavaFX identità |`IdentityUiCheck` **UI CHECK SUCCESS**, password/richieste/storico, annullamento, tre ruoli senza vincoli, callback solo su successo; [log](IdentityUiCheck.log). |
| JavaFX tabelle |`TableLifecycleUiCheck` **UI CHECK SUCCESS**, tre dashboard, basi/sconti/prezzi, lock reale/PID e tastiera reattiva, errore/riprova, obsoleti, celle/scorrimento senza query; [log](TableLifecycleUiCheck.log). |
| JavaFX modelli lenti |`SlowModelsUiCheck` **UI CHECK SUCCESS**, lettura realmente bloccata, selezione ritorna sul threadFX e vecchia lettura non ripopola modelli dopo clear; [log](SlowModelsUiCheck.log). |
| Vero ingresso applicativo |`AppStartupUiCheck` **APP STARTUP SUCCESS**, LoginView, tema e Tab; [log](AppStartupUiCheck.log), [schermata](AppStartupUiCheck-startup-login.png). Non è un nuovo `mvn javafx:run`. |
| Diagrammi correnti |18SVG renderizzati con PlantUML1.2026.2, exit0 e18/18identici byte per byte ai consegnati; [log](render.log), [hash](render-results.json). |
| Suite completa finale, fase2 |`mvn -B -Ppostgres-tests clean test` **394/394**,**47senzaDB/347PostgreSQL**,0failure/errori/salti, exit0; [log](full.log), [XML](full-surefire/), [summary](full-summary.json), [gruppi](full-groups.json), [input](full-inputs.sha256.json). |
| Sentinella finale fase2 |**READ ONLY**,13tabelle/7sequenze identiche prima/dopo suite, marchio e identità/privilegi verificati; [prima](before-suite-application.sha256.json), [dopo](after-suite-application.sha256.json), [esito](after-suite-environment.txt). |
| Arresto fase2 |PostgreSQL arrestato, pg_ctl status3 e listener55436 assente; [esito](shutdown.txt). |

Il394/394del16 e quello17 sono **storici**, non nuove esecuzioni18.
Tutti gli esiti nuovi sopra hanno input/log propri. Il tentativo di avvio finale
subito dopo l'interruzione si è fermato su env.json assente, prima di Maven:
non è contato come suite eseguita. La suite finale riuscita è soltanto quella
fase2 con XML completi; nessun harness era contemporaneamente attivo sul DB.

I log delle gare18 conservano PID e blocker osservati su connessioni PostgreSQL
distinte, non attese arbitrarie. I trigger diagnostici provocano errori dopo
scritture reali: addebiti/accrediti, richieste/stati, vendite/acquisizioni,
cancellazioni e rimborsi multipli. Le riletture verificano commit e rollback.
La suite comprende configurazioni sicure, isolamento, fixture/demo e tutte le
regressioni precedenti. Non usa DAO simulati per certificare effetti PostgreSQL.

## Adattazioni degli harness e coerenza

Originali storici immutati. Il [launcher18](run-ui.py) usa copie e conserva
patch/hash originali/eseguiti per ciascun giro. Percorsi output05/06 adattati;
solo nella copia di SaleOutcome stampa dell'errore prima del finally che può
mascherarlo; nella copia Destination il cast Vehicle è aggiornato a VehicleRow
con accesso `vehicle().getId()`, necessario dopo14. Nessun assert rimosso o
indebolito. Prima/seconda esecuzione fallita/riuscita distinte nei relativi log.
Il vecchio main PriceUiCheck non è eseguito: ne sono riusati gli helper,
mentre prezzi e ritorno al consigliato sono esercitati da TableLifecycle.

Ispezionate sette nuove schermate: conferma saldo, registro manager, conferma
rimborsi futuri, rifiuto storico venditore, caricamento, riprova e prezzi manager.
Importi/controlli/messaggi leggibili nelle viste osservate. Etichetta consigliato
e tipi lunghi possono apparire abbreviati a1280px, limite già presente.
Le altre schermate sono salvate ma non dichiarate tutte ispezionate visivamente.

[Riscontro documentale18](document-consistency.md): istruzioni/configurazione,
teoria e18diagrammi confrontati coi componenti/transazioni/stati correnti.
La leggibilità dei render17 resta un'ispezione storica;18 ne verifica nuova
generazione, identità e coerenza sullo stesso codice. Nessuna discrepanza che
richieda modifiche a installazione/architettura/diagrammi. Nessun grafo Graphify
esistente, nessuna generazione necessaria.

## Review, stato e limiti

Review indipendenti [Standards](standards-review.md) **0violazioni/0smell
azionabili** e [Spec](spec-review.md) **0rilievi** sul solo delta rispetto alla
fotografia iniziale, non HEAD. [Patch congelata](review.patch), SHA256
`d8c1281839f3221e1e4b1c3d341c15120caf7555974b2fb806eaea301d34450b`,
[hash input review](review-files.sha256.json). Ricontrollo indipendente delle
bozze di chiusura: [Standards](standards-closing-review.md),
[Spec](spec-closing-review.md); identità dei byte poi applicati verificata.
I documenti di chiusura e i rapporti non costituiscono nuove prove runtime.

[Conservazione finale](preservation.json): **1796file iniziali presenti,
1792identici e4aggiornamenti documentali intenzionali**, zeroeliminazioni;
codice/POM/test/schema/demo, evidenze01–17 e originali storici immutati,
HEAD/indice invariati. [Diff finale](session.patch) distinto dalla review
congelata; gli artefatti del diff/manifest escludono se stessi, come dichiarato
nel rapporto di conservazione, per evitare inventari ricorsivi.

**Ticket18 chiuso,6/6criteri verificati; R11 e AC12/AC13 completati,
20/20rilievi chiusi nei confini delle prove e dei limiti dichiarati.**
Nessun residuo funzionale dimostrato18. Le prove supplementari fuori perimetro
elencate sotto non vengono dichiarate eseguite; non sono una certificazione
universale del prodotto. Nessuno staging/commit/push/pubblicazione.

Limite di isolamento **esplicito**: la sentinella fase1 ha fingerprint iniziale
e prova JUnit mirata riuscita, ma nessun confronto esterno dopo tutti gli harness,
perché l'ambiente è scomparso all'interruzione. Non è conteggiato come riuscito.
La nuova sentinella fase2 ha invece confronto completo prima/dopo394test riuscito.
Non si afferma un arresto controllato di PostgreSQL/Xvfb fase1; quei temporanei
sono scomparsi. [Ripresa e conservazione](resume-preservation.json).

Rimangono fuori dalle prove: screen reader/accessibilità certificata, altri OS
o display, ogni gesto/dialogo JavaFX, ogni interleaving possibile, benchmark,
installer autonomo e migrazione completa di DB popolati. Nessun nuovo goal
javafx:run18; i due avvii16 sono storici. Questi limiti non diventano successi.
Temporanei da ricontrollare o ricreare; nessuna garanzia globale di assenza di
deadlock o snapshot unico del listino durante aggiornamenti concorrenti.
