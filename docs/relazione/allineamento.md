# Allineamento alla relazione di riferimento

Data: 16 novembre 2026. Stato: matrice completa; ticket 09 concluso tecnicamente sul capitolo 1, con riordino dei due ultimi punti di 1.3 e PDF ricompilato. [Confronto, fonti ed evidenze 09](verifiche/ticket-09/README.md). Concluso tecnicamente anche il ticket 10 sul capitolo 2 e A–B, con tutte le 47 voci e undici tavole verificate: [confronto, fonti e review 10](verifiche/ticket-10/README.md). Il ticket 11 è concluso tecnicamente il 17 novembre sul capitolo 3: tutte le
35 voci, quattordici corrispondenze di frammenti e 27 estratti esatti,
con [confronto e review finali](verifiche/ticket-11/README.md). Il ticket 12 è concluso tecnicamente sul testing: quindici voci,
nove funzioni originali, quindici blocchi censiti e tre estratti iniziali
identici; [confronto, fonti, PDF e review](verifiche/ticket-12/README.md).
Il ticket 13 ha integrato e compilato il documento, osservato tutte le
151 pagine e verificato lo ZIP mediante ricompilazione da estrazione;
review finali Standards/Spec chiuse senza rilievi: ticket **done**.
[Evidenze integrate](verifiche/ticket-13/README.md),
[102 corrispondenze con intervalli](verifiche/ticket-13/corrispondenza-102.md)
e [materiali censiti](verifiche/ticket-13/corrispondenza-materiali.md).
La matrice conserva sotto il trattamento concordato il 16 novembre;
la corrispondenza 13 ne attesta le destinazioni effettive e i limiti.
Nessuna approvazione degli autori presunta.

Gli autori chiedono di rifare la relazione vecchia mantenendone tutti i contenuti, la struttura e l'ordine, riadattati esattamente all'implementazione attuale. La relazione di riferimento è quella effettivamente composta da [Relazione_SWE/main.tex](../../Relazione_SWE/main.tex). Ogni spiegazione, scheda, esempio, frammento, figura e tabella deve avere un corrispondente attuale: il riallineamento non si esaurisce nella coincidenza dei titoli o in una sintesi degli argomenti. Il riferimento tecnico resta il codice corrente.

## Metodo e perimetro

- Seguiti ricorsivamente gli input del documento originale: **24 file attivi e 102 voci**, dai capitoli alle sottosottosezioni. Inclusa la sottosezione non numerata del Package Diagram, indicata qui come `2.5.a` per tracciarla senza trasformarla in una voce numerata.
- Esclusi `2c_architettura.tex` e `2d_dati_flussi.tex`: sono nella cartella originale ma non sono inclusi dal documento principale.
- La matrice registra dove è trattato oggi un argomento e quale intervento serve. L’assenza di un titolo autonomo non viene presentata come assenza di ogni contenuto sul tema.
- Recuperare struttura, spiegazioni, esempi, frammenti e figure significative; le immagini o il codice storici non diventano fonti dell’implementazione corrente.
- Le osservazioni tecniche sono riscontri documentali e letture mirate dei sorgenti, non nuove prove applicative. I ticket richiedono il controllo puntuale dei frammenti e delle affermazioni prima della riscrittura.

## Criteri del riallineamento

1. Conservare i quattro capitoli, le sezioni e l’ordine di tutti gli argomenti interni. Aggiornare un titolo quando descrive un componente, una tecnologia o una prova rimossi.
2. Riportare nel corpo le sei schede complete dei casi principali e i quattro diagrammi di classe corrispondenti a quelli originali; le appendici completano la lettura senza sottrarre contenuti al percorso originale.
3. Conservare gli identificativi UC attuali: la cessione UC-05 torna prima dell’autorizzazione UC-04 nell’esposizione, come nell’originale, senza scambiare i loro significati.
4. Ogni argomento originale riceve una trattazione attuale oppure una spiegazione esplicita della sua assenza. Nessuna nuova schermata o architettura viene inventata per far coincidere i titoli.
5. La profondità si valuta sul contenuto: responsabilità, regola, esempio o frammento significativo, conseguenze e limiti. Non si impongono lo stesso numero di pagine o gli stessi listati.
6. Il capitolo 5 sull’AI resta. Le nuove verifiche documentali non diventano esecuzioni della suite applicativa e non riscrivono le evidenze storiche.

## Diagrammi: vincoli operativi

Questi vincoli si applicano a **tutti i diagrammi**, compresi quelli già
esistenti, non soltanto a eventuali nuove tavole.

1. **Usare sempre la skill `drawio:drawio`** per creare, rifare, correggere,
   adattare ed esportare i diagrammi. Conservare sia i sorgenti nativi
   `.drawio` modificabili sia le esportazioni aggiornate impiegate nel PDF.
2. **Partire dai disegni degli autori.** Per ER, casi d'uso e navigazione
   usare i tre originali indicati sotto, conservandone stile, forme,
   notazione e struttura riconoscibile. Le copie corrette già realizzate
   sono una base di lavoro da confrontare con gli originali e con il codice;
   la loro sola presenza non chiude il lavoro. Anche il package diagram
   riparte dal relativo disegno degli autori.
3. **Rifare e riadattare tutti gli undici diagrammi dell'originale.** Ogni
   tavola deve avere un corrispondente completo, riconoscibile e coerente
   con il progetto corrente nella medesima sezione. Le viste di dettaglio
   della prima riscrittura possono fornire materiale e approfondimenti,
   ma non sostituiscono le tavole originali con un'organizzazione diversa.
4. **Riscontrare i contenuti.** Aggiornare classi, attributi, relazioni,
   cardinalità, dipendenze, flussi e transazioni sui sorgenti e sullo schema
   correnti. Quando manca un sorgente modificabile del vecchio disegno,
   ricostruirlo con la skill dal riferimento visivo disponibile.
5. **Verificare sorgente ed esportazione.** Aprire o renderizzare ogni
   `.drawio`, controllare semanticamente il diagramma, esportarlo con
   draw.io e ispezionarlo nel nuovo PDF. XML valido o esportazione riuscita
   non bastano a dimostrare correttezza e leggibilità. Registrare per ogni
   tavola origine, adattamenti, sorgente finale, esportazione e verifica.

### Basi degli autori

| Disegno originale | Base di lavoro già disponibile | Uso richiesto |
| --- | --- | --- |
| [SWE ER.drawio](<../SWE ER.drawio>) | [ER corretto](diagrammi/database-er-autori.drawio) | Schema ER in 2.8.1, con stile e notazione degli autori. |
| [Users SWE.drawio](<../Users SWE.drawio>) | [Casi d'uso corretti](diagrammi/casi-uso-autori.drawio) | Tavola d'insieme dei casi d'uso in 2.1. |
| [Page Navigation Diagram.drawio](<../Page Navigation Diagram.drawio>) | [Navigazione corretta](diagrammi/navigazione-autori.drawio) | Navigazione in 2.4, mantenendo le tre diramazioni per ruolo. |
| [Package diagram.drawio](<../Package diagram.drawio>) | [Vista corrente dei package](diagrammi/package-architettura.drawio), da confrontare con l'originale | Rifacimento del package diagram in 2.5. |

Il file di navigazione si chiama `Page Navigation Diagram.drawio` nel
repository; `Package diagram.drawio` è un file distinto. Entrambi rientrano
nel lavoro. Gli originali degli autori e quelli della vecchia relazione
restano conservati; le copie aggiornate sono nell'area della nuova relazione.

### Diagrammi originali da rifare e riadattare

| Sezione | Tavola originale | Base richiesta o riferimento per il rifacimento |
| --- | --- | --- |
| 2.1 | [Casi d'uso](../../Relazione_SWE/images/users-swe-current.png) | `Users SWE.drawio`. |
| 2.4 | [Navigazione](../../Relazione_SWE/images/page-navigation-original.pdf) | `Page Navigation Diagram.drawio`. |
| 2.5 | [Package](../../Relazione_SWE/images/package-diagram-original.pdf) | `Package diagram.drawio`. |
| 2.6.1 | [Domain Model](../../Relazione_SWE/images/class-domain-drivehub.pdf) | Stessa funzione e organizzazione della tavola originale, con entità e relazioni attuali. |
| 2.6.2 | [Business Layer](../../Relazione_SWE/images/class-business-drivehub.pdf) | Servizi, composizione e collaborazioni attuali nello stesso gruppo espositivo. |
| 2.6.3 | [DAO](../../Relazione_SWE/images/class-dao-drivehub.pdf) | Contratti, implementazioni e coordinamento JDBC attuali. |
| 2.6.4 | [Presentation Layer](../../Relazione_SWE/images/class-presentation-drivehub.pdf) | Viste, navigazione e collaboratori attuali al posto dei controller rimossi. |
| 2.7.1 | [Acquisto del cliente](../../Relazione_SWE/images/sequence-purchase-drivehub.pdf) | Vendita al cliente: acconto, autorizzazione e saldo attuali. |
| 2.7.2 | [Noleggio](../../Relazione_SWE/images/sequence-rental-drivehub.pdf) | Prenotazione, prezzo, conto, conflitti e transazione attuali. |
| 2.7.3 | [Acquisizione dalla clientela](../../Relazione_SWE/images/sequence-acquisition-drivehub.pdf) | Proposta ed esito della cessione, registrazione e accredito attuali. |
| 2.8.1 | [Schema ER](../../Relazione_SWE/images/swe-er-original.pdf) | `SWE ER.drawio`, aggiornato allo schema e ai vincoli correnti. |

Queste undici corrispondenze sono obbligatorie. Le eventuali suddivisioni
per leggibilità mantengono una tavola d'insieme e la stessa collocazione;
le viste aggiuntive coprono i dettagli dell'implementazione attuale.

## Fonti della stesura attuale

| Sigla | Sorgente |
| --- | --- |
| F1 | [Introduzione](capitoli/1_introduzione.tex) |
| F2 | [Casi d’uso, interfaccia e navigazione](capitoli/2_funzioni.tex) |
| AR | [Package e classi](capitoli/2_architettura.tex) |
| SQ | [Sequenze](capitoli/2_sequenze.tex) |
| DB | [Database](capitoli/2_database.tex) |
| IM | [Implementazione](capitoli/3_implementazione.tex) |
| TE | [Testing](capitoli/4_testing.tex) |
| A | [Schede complete attuali](appendici/a_casi_uso.tex) |
| B | [Tavole delle classi](appendici/b_classi.tex) |
| C | [Matrice delle verifiche](appendici/c_verifiche.tex) |

Le sigle e i numeri di sezione nella colonna «Ora» si riferiscono alla stesura prima di questo riallineamento; i numeri di destinazione coincidono con quelli originali, salvo la sottosezione non numerata già indicata.

## Riscontri tecnici mirati

Le seguenti fonti sostengono gli adattamenti proposti; la verifica riga per riga
degli estratti resta nei ticket di stesura:

- Prezzi e totale concordato: [calcolatore](../../src/main/java/it/unifi/drivehub/service/PriceCalculatorService.java), [ADR 0001](../adr/0001-base-manuale-e-prezzo-concordato.md), [test del calcolatore](../../src/test/java/it/unifi/drivehub/PriceCalculatorServiceTest.java) e [accordo nel dominio](../../src/test/java/it/unifi/drivehub/RequestAgreementTest.java).
- Coordinamento JDBC ed eventi: [DatabaseManager](../../src/main/java/it/unifi/drivehub/dao/DatabaseManager.java), [publisher](../../src/main/java/it/unifi/drivehub/event/VehicleEventPublisher.java) e [prove di cancellazione e rollback](../../src/test/java/it/unifi/drivehub/TestDriveCancellationIntegrationTest.java).
- Navigazione e presentazione: [App](../../src/main/java/it/unifi/drivehub/App.java), [contratto del navigator](../../src/main/java/it/unifi/drivehub/domain/user/UserDashboardNavigator.java), [viste JavaFX](../../src/main/java/it/unifi/drivehub/presentation/view/) e trattazione corrente AR/IM.
- Persistenza e tipi: [schema](../../src/main/resources/db/schema.sql), [ADR 0002](../adr/0002-enumerazioni-native-postgresql.md) e [tipi del registro](../../src/test/java/it/unifi/drivehub/SaleTypeTest.java).
- Classificazione delle prove: [autenticazione con PostgreSQL](../../src/test/java/it/unifi/drivehub/AuthServiceIntegrationTest.java), [ricaricamento demo](../../src/test/java/it/unifi/drivehub/DemoSqlValidationTest.java), [isolamento](../../src/test/java/it/unifi/drivehub/DatabaseIsolationIntegrationTest.java), [invarianti](../../src/test/java/it/unifi/drivehub/DomainInvariantsTest.java), [regole temporali](../../src/test/java/it/unifi/drivehub/BookingRulesTest.java) e capitolo TE/appendice C.

L'esistenza e la lettura di un test non attestano una sua nuova esecuzione.
Per gli argomenti oggi privi di test isolato, la matrice richiede una
dichiarazione del limite e il rinvio alla prova pertinente.

## Capitolo 1

| Origine | Ora | Destinazione proposta | Intervento e contenuto atteso |
| --- | --- | --- | --- |
| [1 Introduzione](../../Relazione_SWE/capitoli/1_introduzione.tex#L1) | F1 | 1 Introduzione | **Mantenere.** Contesto, obiettivi e tre ruoli; introduzione autonoma, senza anticipare la discussione tecnica dei capitoli successivi. |
| [1.1 Descrizione](../../Relazione_SWE/capitoli/1_introduzione.tex#L3) | F1 §1.1 | 1.1 Descrizione | **Mantenere.** Catalogo comune, responsabilità di cliente, venditore e manager, distinzione fra richieste e operazioni concluse. |
| [1.2 Stack tecnologico](../../Relazione_SWE/capitoli/1_introduzione.tex#L15) | F1 §1.2 | 1.2 Stack tecnologico | **Adattare.** Java 21, JavaFX costruito in Java, CSS, JDBC, PostgreSQL e Maven; strumenti di verifica reali, senza FXML, H2, Mockito o JaCoCo non più presenti. |
| [1.3 Semplificazioni adottate](../../Relazione_SWE/capitoli/1_introduzione.tex#L22) | F1 §1.3 | 1.3 Semplificazioni adottate | **Adattare.** Conto simulato, registrazione dei soli clienti, personale predisposto, processi aziendali ridotti e inizializzazione esterna del DB; preservare i limiti attuali. |

## Capitolo 2

Le colonne «Ora» e «Intervento» conservano il confronto della pianificazione.
L'esecuzione del ticket 10 e le 47 corrispondenze verificate sono documentate
nel [confronto finale](verifiche/ticket-10/confronto.md); le sei schede sono
ora nel corpo e A conserva soltanto le nove aggiuntive.

| Origine | Ora | Destinazione proposta | Intervento e contenuto atteso |
| --- | --- | --- | --- |
| [2 Analisi dei requisiti e progettazione](../../Relazione_SWE/capitoli/2_progettazione.tex#L1) | F2, AR, SQ, DB e A | 2 Analisi dei requisiti e progettazione | **Riorganizzare.** Stesse otto sezioni e stesso percorso dall'obiettivo utente a interfaccia, progetto e dati; riportare nel corpo il contenuto principale oggi in appendice. |
| [2.1 Diagramma dei casi d'uso](../../Relazione_SWE/capitoli/2a_casi_uso.tex#L1) | F2 §2.1 | 2.1 Diagramma dei casi d'uso | **Mantenere.** Tavola degli autori corretta e dettagli per ruolo; attori, obiettivi e supporto, registrazione del solo cliente e assenza della panoramica prenotazioni del venditore. |
| [2.2 Template dei casi principali](../../Relazione_SWE/capitoli/2a_casi_uso.tex#L10) | F2 §2.2 e A, UC-01–06 | 2.2 Template dei casi principali | **Ripristinare.** Sei sottosezioni con schede complete nel corpo, nello stesso ordine originale; campi, flusso numerato, alternative e riscontri delle prove. Le schede aggiuntive restano in A. |
| [2.2.1 Prenotare un test drive](../../Relazione_SWE/templates/test_drive.tex#L1) | F2 sintesi UC-01; A scheda UC-01 | 2.2.1 Prenotare un test drive | **Ricollocare.** Scheda completa del test drive: attore, condizioni, fascia oraria effettiva, limite di tre prenotazioni, conflitti, cancellazione e prove pertinenti. |
| [2.2.2 Noleggiare un veicolo](../../Relazione_SWE/templates/noleggio.tex#L1) | F2 sintesi UC-02; A scheda UC-02 | 2.2.2 Noleggiare un veicolo | **Ricollocare.** Scheda completa del noleggio: estremi inclusi, costo ricalcolato, saldo, conflitti, annullamento dei test drive futuri e rollback. |
| [2.2.3 Richiedere e completare un acquisto](../../Relazione_SWE/templates/acquisto.tex#L1) | F2 sintesi UC-03; A scheda UC-03 | 2.2.3 Richiedere e completare una vendita al cliente | **Ricollocare.** UC-03: acconto del 20%, totale fissato, autorizzazione, saldo, rifiuto e scadenza con penale; sostituire le vecchie anomalie di prezzo con il comportamento corretto attuale. |
| [2.2.4 Proporre un veicolo al concessionario](../../Relazione_SWE/templates/cessione.tex#L1) | F2 sintesi UC-05; A scheda UC-05 | 2.2.4 Proporre un veicolo alla concessionaria | **Ricollocare.** UC-05 prima di UC-04 nell'esposizione, conservando gli identificativi: proposta, stima persistita, esito del venditore, storico e limite dell'accredito zero. |
| [2.2.5 Autorizzare una richiesta di acquisto](../../Relazione_SWE/templates/autorizzazione_acquisto.tex#L1) | F2 sintesi UC-04; A scheda UC-04 | 2.2.5 Autorizzare una richiesta di vendita al cliente | **Ricollocare.** UC-04: approvazione o rifiuto del venditore, scadenza e rimborso; distinguere autorizzazione e saldo senza rinumerare il caso d'uso. |
| [2.2.6 Gestire lo sconto su un veicolo](../../Relazione_SWE/templates/sconto_veicolo.tex#L1) | F2 sintesi UC-06; A scheda UC-06 | 2.2.6 Gestire lo sconto su un veicolo | **Ricollocare.** Scheda UC-06: sconti su modello e veicolo, intervallo 0–50, massimo non cumulato, inserimento/rimozione e accordi già fissati. |
| [2.3 Mockups](../../Relazione_SWE/capitoli/2b_interfaccia.tex#L2) | F2 §2.3 | 2.3 Mockups | **Ripristinare.** Stessi quattro gruppi e dettagli per funzione; screenshot correnti con spiegazione di input, azione, risultato e limiti, evitando immagini storiche presentate come attuali. |
| [2.3.1 Autenticazione](../../Relazione_SWE/capitoli/2b_interfaccia.tex#L18) | F2 §2.3.1 | 2.3.1 Autenticazione | **Riorganizzare.** Distinguere ingresso, accesso e registrazione pur condividendo LoginView; riusare le immagini 01–02 senza duplicazioni inutili. |
| [2.3.1.1 Schermata iniziale](../../Relazione_SWE/capitoli/2b_interfaccia.tex#L21) | F2 immagine 01 e §2.4 | 2.3.1.1 Schermata iniziale | **Adattare.** La vista iniziale è LoginView; dichiarare l'assenza di una Welcome Page separata e richiamare lo screenshot dell'accesso. |
| [2.3.1.2 Accesso](../../Relazione_SWE/capitoli/2b_interfaccia.tex#L27) | F2 immagine 01 | 2.3.1.2 Accesso | **Ripristinare.** Email, password, esito ed ingresso nella dashboard del ruolo; screenshot e gestione dell'errore, senza scelta pubblica del ruolo. |
| [2.3.1.3 Registrazione](../../Relazione_SWE/capitoli/2b_interfaccia.tex#L33) | F2 immagine 02 | 2.3.1.3 Registrazione | **Ripristinare.** Anagrafica e credenziali del solo cliente, creazione del conto e risultato; personale già nel DB, senza simulare un form staff. |
| [2.3.2 Area Customer](../../Relazione_SWE/capitoli/2b_interfaccia.tex#L39) | F2 §2.3.2, immagini 03–09 | 2.3.2 Area Customer | **Riorganizzare.** Separare le sei funzioni originali, descrivendo tab e dialoghi attuali; la stessa schermata può essere richiamata da più argomenti. |
| [2.3.2.1 Catalogo veicoli](../../Relazione_SWE/capitoli/2b_interfaccia.tex#L42) | F2 immagine 03 e dialogo 06 | 2.3.2.1 Catalogo veicoli | **Ripristinare.** Cataloghi vendita/noleggio, dati mostrati, prezzi, selezione del veicolo e comandi disponibili. |
| [2.3.2.2 Acquisti e richieste](../../Relazione_SWE/capitoli/2b_interfaccia.tex#L48) | F2 immagine 08 | 2.3.2.2 Vendite al cliente e richieste | **Ripristinare.** Richieste aperte, totale/acconto/residuo, saldo dopo autorizzazione e consultazione delle proprie operazioni; distinguere il registro economico. |
| [2.3.2.3 Noleggi e test drive](../../Relazione_SWE/capitoli/2b_interfaccia.tex#L54) | F2 immagini 04, 06 e 08 | 2.3.2.3 Noleggi e test drive | **Ripristinare.** Dialoghi di test drive e noleggio, data/ora o intervallo, costo, prenotazioni del cliente e annullamento del test drive. |
| [2.3.2.4 Proposta di vendita del proprio veicolo](../../Relazione_SWE/capitoli/2b_interfaccia.tex#L60) | F2 immagine 07 | 2.3.2.4 Proposta di cessione del proprio veicolo | **Ripristinare.** Form, valutazione e invio della proposta; la concessionaria acquista dal cliente e il venditore decide l'esito. |
| [2.3.2.5 Conto dimostrativo](../../Relazione_SWE/capitoli/2b_interfaccia.tex#L66) | F2 immagine 09 e §2.4 | 2.3.2.5 Conto dimostrativo | **Adattare.** Saldo e ricarica demo dal comando disponibile; non descrivere una scheda bancaria o un prelievo autonomo ereditati dalla vecchia UI. |
| [2.3.2.6 Profilo](../../Relazione_SWE/capitoli/2b_interfaccia.tex#L72) | F2 immagine 09 | 2.3.2.6 Profilo | **Adattare.** Gestione Account: email, password ed eliminazione con conferma; dialogo comune invece di una scheda Profilo separata. |
| [2.3.3 Area Salesman](../../Relazione_SWE/capitoli/2b_interfaccia.tex#L78) | F2 §2.3.3, immagini 10–11 | 2.3.3 Area Salesman | **Riorganizzare.** Riprendere le cinque funzioni originali distinguendo richieste, inventario, gestione account e consultazione non implementata. |
| [2.3.3.1 Veicoli in attesa](../../Relazione_SWE/capitoli/2b_interfaccia.tex#L81) | F2 immagine 10 | 2.3.3.1 Proposte di cessione in attesa | **Adattare.** Le richieste PURCHASE sono nella stessa tabella delle SALE; mostrare filtro/tipologia e approvazione/rifiuto senza inventare una tab separata. |
| [2.3.3.2 Richieste di acquisto](../../Relazione_SWE/capitoli/2b_interfaccia.tex#L87) | F2 immagine 10 | 2.3.3.2 Richieste di vendita al cliente | **Adattare.** Autorizzazione/rifiuto delle SALE, dati economici e successivo saldo; richiamare la stessa figura delle richieste. |
| [2.3.3.3 Stato dei veicoli](../../Relazione_SWE/capitoli/2b_interfaccia.tex#L93) | F2 immagine 11 | 2.3.3.3 Stato dei veicoli | **Ripristinare.** Cambio SALE/RENT, rifiuto con noleggio in corso e rimborsi dei futuri; distinguere la destinazione dalle altre transizioni di stato. |
| [2.3.3.4 Panoramica prenotazioni](../../Relazione_SWE/capitoli/2b_interfaccia.tex#L99) | F2 §2.1 e §2.4: funzione dichiarata assente | 2.3.3.4 Panoramica prenotazioni: limite attuale | **Esplicitare limite.** Conservare il punto del percorso originale spiegando che il venditore non dispone della vecchia panoramica; niente screenshot storico o nuova funzionalità. |
| [2.3.3.5 Profilo](../../Relazione_SWE/capitoli/2b_interfaccia.tex#L105) | F2 §2.4: Gestione Account comune | 2.3.3.5 Profilo | **Adattare.** Descrivere il dialogo comune per il venditore e i vincoli dell'identità; riscontrare l'apertura dalla sua dashboard, senza presumere uno screenshot dedicato già disponibile. |
| [2.3.4 Area Manager](../../Relazione_SWE/capitoli/2b_interfaccia.tex#L111) | F2 §2.3.4, immagini 12–16 | 2.3.4 Area Manager | **Riorganizzare.** Riprendere parco veicoli, marche/modelli, registro e profilo; gli sconti della UI attuale sono una scheda distinta ma restano spiegati presso le rispettive funzioni. |
| [2.3.4.1 Parco veicoli](../../Relazione_SWE/capitoli/2b_interfaccia.tex#L114) | F2 immagini 14–15 | 2.3.4.1 Parco veicoli | **Ripristinare.** Veicoli, base automatica/manuale, prezzi, inserimento/rimozione e sconto individuale con rinvio alla scheda Sconti. |
| [2.3.4.2 Marche e modelli](../../Relazione_SWE/capitoli/2b_interfaccia.tex#L120) | F2 immagini 12–13 e 15 | 2.3.4.2 Marche e modelli | **Ripristinare.** Due tab attuali per marchi e modelli; base modello, anno, manutenzione e sconto modello, senza confonderli con lo sconto sul veicolo. |
| [2.3.4.3 Registro delle operazioni](../../Relazione_SWE/capitoli/2b_interfaccia.tex#L126) | F2 immagine 16 | 2.3.4.3 Registro delle operazioni | **Ripristinare.** SALE/PURCHASE/PENALTY con importi non negativi e punto di vista della concessionaria; i noleggi non sono automaticamente righe di Sale. |
| [2.3.4.4 Profilo](../../Relazione_SWE/capitoli/2b_interfaccia.tex#L132) | F2 §2.4: Gestione Account comune | 2.3.4.4 Profilo | **Adattare.** Email, password ed eliminazione del manager nel dialogo condiviso; verificare la vista corrente e riusare le figure solo se rappresentative. |
| [2.4 Page Navigation Diagram](../../Relazione_SWE/capitoli/2b_interfaccia.tex#L138) | F2 §2.4 | 2.4 Page Navigation Diagram | **Mantenere.** Tavola degli autori corretta e percorsi dei tre ruoli; distinguere Scene, tab, dialoghi, annullamento, logout e rientro dopo eliminazione dell'identità. |
| [2.5 Package Diagram](../../Relazione_SWE/capitoli/2c_package_diagram.tex#L2) | AR §2.5 | 2.5 Package Diagram | **Mantenere.** Undici package di produzione, composizione e dipendenze reali; motivazione dei confini e limiti della separazione, con tavola PKG nel corpo. |
| [2.5.a Il dominio non dipende dagli altri livelli](../../Relazione_SWE/capitoli/2c_package_diagram.tex#L12) | AR §2.5, paragrafi sul dominio | 2.5.a Il dominio non dipende dagli altri livelli | **Ripristinare.** Riprendere la sottosezione non numerata: nessun import verso gli altri livelli applicativi, ma contratto di navigazione degli utenti; non attribuire indipendenza concettuale assoluta. |
| [2.6 Class Diagram](../../Relazione_SWE/capitoli/2c_class_diagram.tex#L2) | AR §2.6 e B | 2.6 Class Diagram | **Riorganizzare.** Mantenere i quattro gruppi originali; i quattro diagrammi originali rifatti/riadattati nel corpo, uno per gruppo e tutte le tavole di dettaglio in B, coprendo gli undici package. |
| [2.6.1 Domain Model](../../Relazione_SWE/capitoli/2c_class_diagram.tex#L6) | AR §2.6.1 e B, CL-02–05 | 2.6.1 Domain Model | **Riorganizzare.** Gerarchia utenti, conto, catalogo, sconti e operazioni; relazioni per ID distinte da riferimenti reali e invarianti spiegate. |
| [2.6.2 Business Layer](../../Relazione_SWE/capitoli/2c_class_diagram.tex#L17) | AR §2.6.2 e B, CL-01/06/07/11 | 2.6.2 Business Layer | **Riorganizzare.** Servizi, composizione, prezzi, sicurezza ed eventi; rifare/riadattare la tavola originale dei servizi nel corpo oltre ai dettagli dell'Observer. |
| [2.6.3 DAO](../../Relazione_SWE/capitoli/2c_class_diagram.tex#L28) | AR §2.6.3 e B, CL-08a/08b/09 | 2.6.3 DAO | **Ricollocare.** Contratti DAO, implementazioni e DatabaseManager; tavola originale rifatta/riadattata nel corpo, connessione esplicita e assenza delle vecchie factory/UnitOfWork. |
| [2.6.4 Presentation Layer](../../Relazione_SWE/capitoli/2c_class_diagram.tex#L39) | AR §2.6.4 e B, CL-10 | 2.6.4 Presentation Layer | **Ricollocare.** Viste JavaFX, dialoghi, AsyncLoad e collegamento con App; tavola corrente nel corpo al posto dei controller FXML. |
| [2.7 Sequence Diagram](../../Relazione_SWE/capitoli/2c_sequence_diagram.tex#L1) | SQ §2.7 | 2.7 Sequence Diagram | **Mantenere.** Stessi tre flussi originali; rifare/riadattare le tre sequenze originali con la skill draw.io, usando i dettagli correnti come approfondimento, spiegando transazioni e alternative senza moltiplicare i casi principali. |
| [2.7.1 Acquisto di un veicolo](../../Relazione_SWE/capitoli/2c_sequence_diagram.tex#L5) | SQ §2.7.1, SQ-01a–d | 2.7.1 Vendita di un veicolo al cliente | **Adattare.** Acconto, esito, saldo e scadenza; totale persistito, ruoli e lock attuali, senza ripetere le vecchie anomalie come comportamento corrente. |
| [2.7.2 Prenotazione di un noleggio](../../Relazione_SWE/capitoli/2c_sequence_diagram.tex#L16) | SQ §2.7.2, SQ-03 | 2.7.2 Prenotazione di un noleggio | **Mantenere.** Disponibilità, periodo incluso, costo, saldo, cancellazione Observer e confine transazionale del noleggio. |
| [2.7.3 Acquisizione di un veicolo del Customer](../../Relazione_SWE/capitoli/2c_sequence_diagram.tex#L27) | SQ §2.7.3, SQ-02a–b | 2.7.3 Acquisto di un veicolo dal cliente | **Adattare.** Proposta e decisione del venditore, stima persistita e storico, attribuzione corretta senza vecchia UnitOfWork annidata o venditore costante. |
| [2.8 Database](../../Relazione_SWE/capitoli/2c_database.tex#L2) | DB §2.8 | 2.8 Database | **Mantenere.** Conservare ER, modello relazionale e utenti/vincoli; mantenere distinta la spiegazione del dato dal mapping implementativo di 3.4. |
| [2.8.1 Schema ER](../../Relazione_SWE/capitoli/2c_database.tex#L4) | DB §2.8.1, tavola autori ed ER-01/02 | 2.8.1 Schema ER | **Mantenere.** Tavola degli autori corretta, entità e cardinalità nei due versi, generalizzazione concettuale User distinta dal DDL. |
| [2.8.2 Modello relazionale](../../Relazione_SWE/capitoli/2c_database.tex#L17) | DB §2.8.2, REL-01–03 | 2.8.2 Modello relazionale | **Riorganizzare.** Tredici relazioni e tutti gli attributi, PK/FK, unicità, nullabilità e legenda; recuperare una lettura testuale dello schema accanto alle tavole correnti. |
| [2.8.3 Utenti, vincoli e semplificazioni](../../Relazione_SWE/capitoli/2c_database.tex#L96) | DB §2.8.3 e §2.8.2 | 2.8.3 Utenti, vincoli e semplificazioni | **Adattare.** Tre tabelle e vista user, sequenza condivisa con PK locali, enum nativi, vincoli temporali/economici, limiti email e predisposizione separata del DB. |

## Capitolo 3

Le colonne «Ora» e «Intervento» conservano il confronto della pianificazione.
Il ticket 11 ne ha verificato l'esecuzione nelle 35 voci:
[confronto originale/nuovo](verifiche/ticket-11/confronto.md), comprese
le quattordici corrispondenze dei frammenti e gli adattamenti motivati.

| Origine | Ora | Destinazione proposta | Intervento e contenuto atteso |
| --- | --- | --- | --- |
| [3 Implementazione](../../Relazione_SWE/capitoli/3_implementazione.tex#L1) | IM §3 e frammenti; AR/SQ/DB come supporto | 3 Implementazione | **Riorganizzare.** Stesse cinque sezioni e stessa progressione interna; riusare gli estratti validi e recuperare spiegazioni e frammenti significativi oggi accorpati. |
| [3.1 Domain Model](../../Relazione_SWE/capitoli/3a_domain_model.tex#L1) | IM §3.1 | 3.1 Domain Model | **Ripristinare.** Passare dall'organizzazione per invarianti/gerarchie ai cinque argomenti originali; invarianti e identità si spiegano nelle entità cui appartengono. |
| [3.1.1 Users](../../Relazione_SWE/capitoli/3a_domain_model.tex#L13) | IM §3.1.2, §3.2.4 e AR §2.6.1 | 3.1.1 Users | **Ripristinare.** User, AbstractUser, Client/Salesman/Manager e BankAccount: gerarchia reale, conto opzionalmente caricato, invarianti e identità per ID/ruolo. |
| [3.1.2 Vehicles](../../Relazione_SWE/capitoli/3a_domain_model.tex#L20) | IM §3.1.1–2 e §3.2.1 | 3.1.2 Vehicles | **Ripristinare.** Brand, Model, Vehicle, PriceMode/VehicleState e gerarchia Discount; validazione prima della mutazione, ID e dati modificabili, stato locale distinto dai controlli concorrenti. |
| [3.1.3 Rentals](../../Relazione_SWE/capitoli/3a_domain_model.tex#L43) | IM §3.1.1–2 e TE §4.2 | 3.1.3 Rentals | **Ripristinare.** Rent e TestDrive nel package operation: date, importi e fascia oraria effettiva; nessuna pretesa che le sole entità impediscano conflitti persistenti. |
| [3.1.4 Sales](../../Relazione_SWE/capitoli/3a_domain_model.tex#L48) | IM §3.1.1–2 e §3.2.2 | 3.1.4 Sales | **Ripristinare.** Request/RequestType e Sale/SaleType: accordo totale/acconto, stima, attribuzioni opzionali e importi non negativi; le tipologie non dipendono più dal segno. |
| [3.1.5 Observer](../../Relazione_SWE/capitoli/3a_domain_model.tex#L74) | IM §3.2.3 e AR §2.6.2 | 3.1.5 Eventi e contratto Observer | **Ricollocare.** Presentare evento, observer e publisher nel package event, senza dichiararli parte di domain; l'effetto concreto e la transazione si approfondiscono in 3.2.6. |
| [3.2 Business Layer](../../Relazione_SWE/capitoli/3b_business_layer.tex#L1) | IM §3.2 | 3.2 Business Layer | **Ripristinare.** Ordine originale: coordinamento transazionale, responsabilità, quattro flussi, prezzi, identità/conto e Observer; estratti legati al caso spiegato. |
| [3.2.1 Core: unità di lavoro e transazioni](../../Relazione_SWE/capitoli/3b_business_layer.tex#L14) | IM §3.2.2, lst:transazione | 3.2.1 Coordinamento e transazioni | **Adattare.** DatabaseManager e callback Connection: commit, rollback, causa ed eccezioni soppresse; niente TransactionRunner o contratto UnitOfWork assenti. |
| [3.2.2 Services: responsabilità](../../Relazione_SWE/capitoli/3b_business_layer.tex#L36) | IM apertura §3.2.1, AR §2.6.2 e AppContext | 3.2.2 Services: responsabilità | **Ripristinare.** Responsabilità di Auth, Catalog, OperationQuery, PriceCalculator, Purchase, Rental, SaleToDealership e TestDrive; composizione esplicita e dipendenze reali. |
| [3.2.3 Services: flussi applicativi](../../Relazione_SWE/capitoli/3b_flussi.tex#L1) | IM §3.2.1–4, SQ e F2 | 3.2.3 Services: flussi applicativi | **Ripristinare.** Quattro sottosottosezioni con sequenza applicativa, controlli, scritture, alternative ed esito; i rinvii ai diagrammi completano la spiegazione e non la sostituiscono. |
| [3.2.3.1 Catalogo e test drive](../../Relazione_SWE/capitoli/3b_flussi.tex#L4) | F2 UC-01/07, IM §3.3.2 e SQ | 3.2.3.1 Catalogo e test drive | **Ripristinare.** Letture catalogo e dati preparati; prenotazione test drive con fascia reale, limite attivi, idoneità e conflitti. Recuperare un frammento corrente del controllo significativo. |
| [3.2.3.2 Noleggio con saldo del conto](../../Relazione_SWE/capitoli/3b_flussi.tex#L23) | IM §3.2.2–3 e SQ-03 | 3.2.3.2 Noleggio con saldo del conto | **Ripristinare.** Costo geometrico sui giorni inclusi, lock veicolo/cliente/conto, conflitti, addebito, salvataggio e Observer; distinguere prenotazione e cambio destinazione con rimborsi. |
| [3.2.3.3 Richiesta di acquisto, saldo e penale](../../Relazione_SWE/capitoli/3b_flussi.tex#L35) | IM §3.2.1–2 e SQ-01 | 3.2.3.3 Richiesta di vendita al cliente, saldo e penale | **Ripristinare.** Acconto del 20%, totale fissato, approvazione, saldo, rifiuto con rimborso e scadenza con penale del 20% dell'acconto; controlli nel servizio e limiti delle letture. |
| [3.2.3.4 Proposta di cessione del veicolo](../../Relazione_SWE/capitoli/3b_flussi.tex#L42) | IM §3.2.2 e SQ-02 | 3.2.3.4 Proposta di cessione del veicolo | **Ripristinare.** Proposta, preventivo/stima salvata, verifica VIN e ultima vendita al proponente, accettazione o rifiuto, accredito e storico; dichiarare il limite degli importi zero. |
| [3.2.4 Strategies: prezzi e sconti](../../Relazione_SWE/capitoli/3c_prezzi_sicurezza.tex#L1) | IM §3.2.1 e ADR 0001 | 3.2.4 Calcolo dei prezzi e sconti | **Adattare.** PriceCalculatorService concreto; base automatica/manuale, massimo sconti, deprezzamenti, acconto, noleggio e stima con formule/esempi attuali. Nessuna strategia intercambiabile inventata. |
| [3.2.5 Security e Banking: autenticazione e conto](../../Relazione_SWE/capitoli/3c_prezzi_sicurezza.tex#L33) | IM §3.2.4 | 3.2.5 Autenticazione e conto dimostrativo | **Adattare.** AuthService e PasswordHasher/PBKDF2, registrazione del cliente e conto, ricarica e gestione dell'identità; saldo protetto e limiti dei controlli autorizzativi. |
| [3.2.6 Observer e gestione dell'inventario](../../Relazione_SWE/capitoli/3c_prezzi_sicurezza.tex#L46) | IM §3.2.3, lst:observer e SQ-OB | 3.2.6 Observer e gestione dell'inventario | **Ripristinare.** TestDriveCancellationObserver sulla stessa Connection prima del commit: cancella tutti i test drive futuri, propaga errori e può causare rollback; differenza dal vecchio listener che ignorava eccezioni. |
| [3.3 Presentation Layer](../../Relazione_SWE/capitoli/3a_presentazione_dominio.tex#L1) | IM §3.3 | 3.3 Presentation Layer | **Riorganizzare.** Stesse tre responsabilità narrative, aggiornate dalle classi FXML/gateway a viste Java, servizi diretti e AsyncLoad. |
| [3.3.1 Controller e aree applicative](../../Relazione_SWE/capitoli/3a_presentazione_dominio.tex#L4) | IM §3.3.1 e F2 §2.3 | 3.3.1 Viste e aree applicative | **Adattare.** LoginView e tre dashboard, AccountManagementDialog e UiUtils; raccolta input, controlli e comandi per ruolo, senza AbstractController o FXML. |
| [3.3.2 Creazione dei controller e navigazione](../../Relazione_SWE/capitoli/3a_presentazione_dominio.tex#L16) | IM §3.3.1, lst:navigazione | 3.3.2 Creazione delle viste e navigazione | **Adattare.** App e AppContext passano collaboratori e logout; openDashboard/UserDashboardNavigator selezionano il ruolo con double dispatch, senza Route o sessione globale. |
| [3.3.3 Gateway, dati della UI e aggiornamento](../../Relazione_SWE/capitoli/3a_presentazione_dominio.tex#L30) | IM §3.3.2–3 | 3.3.3 Servizi, dati della UI e aggiornamento | **Adattare.** Invocazioni dirette, VehicleRow e letture collettive; AsyncLoad, risposta superata, uscita dalla Scene, errore/Riprova, scritture ancora sincrone e refresh dopo commit. |
| [3.4 Persistenza: livello DAO e mapping oggetti/tabelle](../../Relazione_SWE/capitoli/3d_dao.tex#L1) | IM §3.4 e DB | 3.4 Persistenza: livello DAO e mapping oggetti/tabelle | **Ripristinare.** Recuperare cinque sottosezioni: porte, utenti, operazioni, vincoli e connessione; riusare gli estratti JDBC e aggiungere spiegazioni mirate dove mancanti. |
| [3.4.1 Porte DAO e implementazioni JDBC](../../Relazione_SWE/capitoli/3d_dao.tex#L4) | IM §3.4.1 e AR §2.6.3 | 3.4.1 Porte DAO e implementazioni JDBC | **Ripristinare.** Dieci contratti DAO e Postgres*Dao, PreparedStatement, overload con/senza Connection, ritorni e numero di righe modificate; esempio attuale di contratto. |
| [3.4.2 Mapping degli utenti e vista unificata](../../Relazione_SWE/capitoli/3d_dao.tex#L17) | IM §3.4.2 e DB §2.8.3 | 3.4.2 Mapping degli utenti e vista unificata | **Ripristinare.** Vista UNION ALL, ricostruzione del sottotipo, selezione chiusa della tabella di ruolo, ID condivisi per sequenza e limiti delle PK/email. |
| [3.4.3 Mapping delle operazioni e valori opzionali](../../Relazione_SWE/capitoli/3d_dao.tex#L34) | IM §3.4.2, lst:mapping | 3.4.3 Mapping delle operazioni e valori opzionali | **Ripristinare.** Request e Sale: tipi espliciti, agreed_total, BigDecimal, date/timestamp e FK nullable; letture e scritture simmetriche, senza codifica economica tramite segno. |
| [3.4.4 Vincoli SQL e controlli dei servizi](../../Relazione_SWE/capitoli/3d_dao.tex#L44) | DB §2.8.2–3 e IM §3.4.1 | 3.4.4 Vincoli SQL e controlli dei servizi | **Ricollocare.** Estratti SQL attuali di unicità, CHECK, EXCLUDE e enum/cast; distinguere regole SQL da stato, titolarità, accordo e autorizzazione nei servizi. |
| [3.4.5 Connessione condivisa e Unit of Work](../../Relazione_SWE/capitoli/3d_dao.tex#L55) | IM §3.4.1 e §3.2.2 | 3.4.5 Connessione condivisa e confine transazionale | **Adattare.** Overload che usano realmente Connection, proprietà di commit/close e letture autonome; nessuna identity map o UnitOfWork del vecchio progetto. |
| [3.5 Idiomi e Design Pattern](../../Relazione_SWE/capitoli/3e_pattern.tex#L1) | IM §3.5, paragrafi non numerati | 3.5 Idiomi e Design Pattern | **Ripristinare.** Sei sottosezioni nello stesso ordine, con problema, collaboratori, frammento/rinvio significativo e limite; distinguere GoF, idiomi e tecniche. |
| [3.5.1 Observer](../../Relazione_SWE/capitoli/3e_pattern.tex#L4) | IM §3.5 paragrafo Observer | 3.5.1 Observer | **Ripristinare.** Publisher, contratto e observer concreto; notifica sincrona e condivisione della Connection, errori e assenza di consegna persistente. |
| [3.5.2 Strategy](../../Relazione_SWE/capitoli/3e_pattern.tex#L16) | IM §3.2.1; nessuna sottosezione Strategy | 3.5.2 Calcolo centralizzato e assenza di Strategy | **Adattare.** Preservare la discussione della variabilità degli algoritmi: oggi un calcolatore concreto, senza interfaccia PricingStrategy né selezione intercambiabile. Non attribuire GoF Strategy. |
| [3.5.3 Metodi statici di creazione](../../Relazione_SWE/capitoli/3e_pattern.tex#L32) | IM §3.5 paragrafo factory statiche | 3.5.3 Metodi statici di creazione | **Ripristinare.** Factory di Vehicle, Request e Sale, validazione e ricostruzione; costruttori effettivi e distinzione da Factory Method GoF. |
| [3.5.4 Istanza centrale per la navigazione](../../Relazione_SWE/capitoli/3e_pattern.tex#L46) | IM §3.5 paragrafi navigazione e Singleton | 3.5.4 Navigazione e contesto applicativo | **Adattare.** Double dispatch nello stile Visitor per il ruolo e Singleton predefinito AppContext con costruttore alternativo; non ripristinare NavigationManager globale. |
| [3.5.5 DAO e Unit of Work](../../Relazione_SWE/capitoli/3e_pattern.tex#L59) | IM §3.5 paragrafo DAO/DI e §3.4 | 3.5.5 DAO e coordinamento transazionale | **Adattare.** Porte SQL e callback DatabaseManager sulla stessa connessione, DI manuale e dipendenze JDBC; spiegare che il vecchio contratto UnitOfWork non c'è più. |
| [3.5.6 Idiomi Java usati nei confini tra livelli](../../Relazione_SWE/capitoli/3e_pattern.tex#L64) | IM §3.5 factory/idiomi e §3.3.2 | 3.5.6 Idiomi Java usati nei confini tra livelli | **Ripristinare.** Optional dove realmente usato, record di lettura, enum, campi finali, lambda/callback, try-with-resources e Task; niente UiModels o RegistrationRequest rimossi. |

## Capitolo 4

| Origine | Ora | Destinazione proposta | Intervento e contenuto atteso |
| --- | --- | --- | --- |
| [4 Testing](../../Relazione_SWE/capitoli/4_testing.tex#L1) | TE e C | 4 Testing | **Riorganizzare.** Stesse cinque sezioni e stessi argomenti interni, con classi e prove attuali; motivazione, preparazione, comando, oracolo, estratto e limite per i casi significativi. |
| [4.1 Strategia di verifica](../../Relazione_SWE/capitoli/4_testing.tex#L3) | TE §4.1 | 4.1 Strategia di verifica | **Mantenere.** Rischi economici, livelli, test funzionali e guidati dalla struttura, oracoli e attribuzione dei risultati; separare test, harness e controlli documentali. |
| [4.1.1 Tecnologie e strumenti](../../Relazione_SWE/capitoli/4_testing.tex#L13) | TE §4.1, tecnologie nel testo | 4.1.1 Tecnologie e strumenti | **Ripristinare.** JUnit, Surefire, Maven, PostgreSQL dedicato e supporto TestDatabase; profilo postgres-tests, harness JavaFX esterni alla suite, niente H2/Mockito/JaCoCo presunti. |
| [4.2 Livello 1: Business Logic](../../Relazione_SWE/capitoli/4_testing.tex#L21) | TE §4.2 e rinvii a §4.3 | 4.2 Livello 1: Business Logic | **Riorganizzare.** Recuperare i sei argomenti originali senza classificare come unitari i test PostgreSQL; dove manca un test isolato, indicarlo e rinviare alla prova di integrazione. |
| [4.2.1 Invarianti del dominio](../../Relazione_SWE/capitoli/4_testing.tex#L23) | TE §4.2 | 4.2.1 Invarianti del dominio | **Ripristinare.** DomainInvariantsTest e BookingRulesTest: confini, stato invariato dopo input rifiutato, conto, sconti, anni, date e fascia oraria. |
| [4.2.2 AuthServiceTest](../../Relazione_SWE/capitoli/4_testing.tex#L26) | TE §4.3–4.4 e C: AuthServiceIntegrationTest | 4.2.2 Autenticazione e identità | **Adattare.** Conservare l'argomento della vecchia AuthServiceTest; non esiste un equivalente unitario dedicato. Spiegare il confine e rinviare ai test PostgreSQL di email, credenziali e cancellazione. |
| [4.2.3 PricingAndSecurityTest](../../Relazione_SWE/capitoli/4_testing.tex#L35) | TE §4.2 e C: PriceCalculatorServiceTest | 4.2.3 Prezzi e sicurezza | **Adattare.** Estratti discriminanti su formula/base/sconti/centesimi; per password e identità indicare le prove effettive e l'eventuale assenza di un test isolato PBKDF2, senza ricreare PricingAndSecurityTest. |
| [4.2.4 SalesServiceTest](../../Relazione_SWE/capitoli/4_testing.tex#L47) | TE §4.2, §4.3 e C | 4.2.4 Richieste, vendite e accordo economico | **Adattare.** RequestAgreementTest e SaleTypeTest per il dominio; PurchaseAgreement/SaleOutcome/SaleProposalIntegrationTest per scritture ed esiti, chiaramente classificati nel livello successivo. |
| [4.2.5 RentalServiceTest e BankAccountServiceTest](../../Relazione_SWE/capitoli/4_testing.tex#L61) | TE §4.2–4.3 e C | 4.2.5 Noleggio e conto dimostrativo | **Adattare.** BookingRulesTest e DomainInvariantsTest per regole locali; BookingConcurrency, BalanceConcurrency e VehicleDestinationIntegrationTest per integrazione, conflitti e rimborsi. |
| [4.2.6 TransactionAndObserverTest](../../Relazione_SWE/capitoli/4_testing.tex#L73) | TE §4.3 rollback e C | 4.2.6 Transazioni e Observer | **Adattare.** Dichiarare l'assenza della vecchia TransactionAndObserverTest; mostrare il contratto e rinviare alle riletture PostgreSQL di TestDriveCancellationIntegrationTest e ai guasti dopo scritture effettive. |
| [4.3 Livello 2: Persistence Layer (DAO)](../../Relazione_SWE/capitoli/4_testing.tex#L86) | TE §4.3 e C | 4.3 Livello 2: Persistence Layer (DAO) | **Riorganizzare.** Restituire visibilità a mapping/DAO e preparazione schema/demo, mantenendo il contenuto aggiuntivo su rollback, concorrenza e isolamento del DB di test. |
| [4.3.1 PostgresDaoIntegrationTest](../../Relazione_SWE/capitoli/4_testing.tex#L89) | TE §4.3 mapping/enum e C | 4.3.1 DAO e mapping su PostgreSQL | **Adattare.** PriceSelection, PurchaseAgreement, SaleOutcome e PostgresEnumIntegrationTest: salvataggio e rilettura, tipi e null; non descrivere la vecchia PostgresDaoIntegrationTest su H2. |
| [4.3.2 DatabaseBootstrapIntegrationTest](../../Relazione_SWE/capitoli/4_testing.tex#L102) | TE §4.3 TestDatabase/DemoSqlValidationTest e C | 4.3.2 Schema e dati dimostrativi | **Adattare.** DemoSqlValidationTest e TestDatabase: tredici tabelle, righe e sequenze dopo ricaricamenti; DatabaseIsolationIntegrationTest e configurazione dedicata. Nessun bootstrap automatico dell'applicazione. |
| [4.4 Verifiche funzionali e della presentazione](../../Relazione_SWE/capitoli/4_testing.tex#L114) | TE §4.4 e C | 4.4 Verifiche funzionali e della presentazione | **Riorganizzare.** Esplicitare scenari di registrazione/accesso, catalogo/sconti, flussi commerciali, GUI e tabelle; estratto funzionale attuale e fonti degli harness. Non rinominare FullWorkflow come prova di ogni workflow. |
| [4.5 Esecuzione della suite e portata dei risultati](../../Relazione_SWE/capitoli/4_testing.tex#L129) | TE §4.5 e C | 4.5 Esecuzione della suite e portata dei risultati | **Mantenere.** Comandi e ambienti correnti, risultati storici con versione/data, suite standard distinta dal profilo PostgreSQL e dagli harness; nessuna percentuale di coverage o nuova esecuzione dedotta dalla riscrittura. |

## Frammenti ed esempi da conservare per funzione

I **23 blocchi `lstlisting`** originali sono censiti qui. I frammenti correnti validi si riusano; dove manca un esempio equivalente si seleziona un estratto reale durante il ticket. Un rinvio è sufficiente per evitare duplicazioni, purché la spiegazione rimanga nella sottosezione prevista.

| Originale | Destinazione | Esempio attuale o adattamento richiesto |
| --- | --- | --- |
| [Transizioni di Vehicle](../../Relazione_SWE/capitoli/3a_domain_model.tex#L24) | 3.1.2 | Invarianti locali di Vehicle (`lst:vehicle-invariante`) e transizione condizionale del DAO (`lst:update-condizionale`), distinguendo i due confini. |
| [Factory dei due tipi di Request](../../Relazione_SWE/capitoli/3a_domain_model.tex#L50) | 3.1.4 | Accordo e precisione (`lst:request-accordo`), factory attuali e RequestType; niente stima codificata con il segno. |
| [TransactionRunner.execute](../../Relazione_SWE/capitoli/3b_business_layer.tex#L17) | 3.2.1 | DatabaseManager.executeInTransactionWithResult (`lst:transazione`), causa e rollback; sostituire il vecchio contratto UnitOfWork. |
| [Controlli di TestDriveService.book](../../Relazione_SWE/capitoli/3b_flussi.tex#L10) | 3.2.3.1 | Selezionare i controlli correnti di TestDriveService: fascia e conflitti reali, senza riproporre l’arrotondamento storico dell’ora. |
| [Predicato SQL di sovrapposizione dei noleggi](../../Relazione_SWE/capitoli/3b_flussi.tex#L27) | 3.2.3.2 e 3.4.4 | Query corrente di PostgresRentDao e vincolo EXCLUDE di schema.sql; lettura e lock coordinati dal servizio. |
| [Passaggio ApplicationContext dopo FXMLLoader](../../Relazione_SWE/capitoli/3a_presentazione_dominio.tex#L19) | 3.3.2 | Costruzione delle viste in App e dispatch (`lst:navigazione`); FXML e ContextAware non esistono più. |
| [Interfaccia UserDao](../../Relazione_SWE/capitoli/3d_dao.tex#L7) | 3.4.1 | Selezionare poche firme del contratto corrente, comprese identità/ruolo dove necessari; non copiare firme rimosse. |
| [Ricostruzione di User dalla vista SQL](../../Relazione_SWE/capitoli/3d_dao.tex#L20) | 3.4.2 | Selezionare il mapper di PostgresUserDao che ricostruisce i sottotipi dalla vista e spiegare i limiti degli ID. |
| [Chiavi opzionali del mapping di Sale](../../Relazione_SWE/capitoli/3d_dao.tex#L37) | 3.4.3 | Conservare l’esempio di NULL per Sale e quello corrente di Request (`lst:mapping`); getLong da solo non distingue NULL da zero. |
| [Vincoli di init.sql](../../Relazione_SWE/capitoli/3d_dao.tex#L47) | 3.4.4 | Estratti di schema.sql: unicità, saldo, date ed esclusione dei noleggi; enum nativi e cast separati dai CHECK economici. |
| [Contratto InventoryObserver](../../Relazione_SWE/capitoli/3e_pattern.tex#L7) | 3.5.1 | VehicleCommitmentObserver e publisher (`lst:observer`), inclusa la Connection condivisa e la propagazione dell’errore. |
| [Iniezione di PricingStrategy](../../Relazione_SWE/capitoli/3e_pattern.tex#L19) | 3.5.2 | Esempio di PriceCalculatorService e selezione della base (`lst:base`); dichiarare l’assenza di una Strategy intercambiabile. |
| [Vehicle.create con ID ancora da assegnare](../../Relazione_SWE/capitoli/3e_pattern.tex#L35) | 3.5.3 | Factory corrente, ID finale e ricostruzione dopo INSERT; non ripristinare BaseEntity.assignId. |
| [NavigationManager.initialize](../../Relazione_SWE/capitoli/3e_pattern.tex#L49) | 3.5.4 | App/UserDashboardNavigator e contesto predefinito (`lst:singleton`), distinguendo dispatch e accesso globale. |
| [Mockito: nessun hash/save per email duplicata](../../Relazione_SWE/capitoli/4_testing.tex#L29) | 4.2.2 con prova in 4.3/4.4 | AuthServiceIntegrationTest: identità e login conservati; l’oracolo attuale osserva PostgreSQL, non le interazioni con mock. |
| [Assert sulla tariffa di noleggio](../../Relazione_SWE/capitoli/4_testing.tex#L38) | 4.2.3 | PriceCalculatorServiceTest: formula geometrica, estremi e centesimi reali; valori da ricavare dalla prova corrente. |
| [Assert di annullamento con penale](../../Relazione_SWE/capitoli/4_testing.tex#L50) | 4.2.4 con prova in 4.3/4.4 | SaleOutcomeIntegrationTest e PurchaseAgreementIntegrationTest: distinguere rifiuto con rimborso e scadenza con penale. |
| [Addebito e registrazione del noleggio](../../Relazione_SWE/capitoli/4_testing.tex#L64) | 4.2.5 con prova in 4.3/4.4 | Selezionare un caso corrente con rilettura di saldo e Rent, distinguendo regole in memoria e integrazione. |
| [Ordine commit, rollback e close su mock](../../Relazione_SWE/capitoli/4_testing.tex#L76) | 4.2.6 e 4.3 | Usare il codice transazionale e le prove di rollback dopo scritture/Observer. Non dichiarare una prova dedicata a guasto del commit/rollback se non esiste. |
| [Roundtrip DAO in nuova UnitOfWork](../../Relazione_SWE/capitoli/4_testing.tex#L92) | 4.3.1 | PriceSelectionIntegrationTest o PurchaseAgreementIntegrationTest: nuova lettura/contesto dopo il commit, senza classe UnitOfWork. |
| [Conteggi dopo DatabaseBootstrap/demo](../../Relazione_SWE/capitoli/4_testing.tex#L105) | 4.3.2 | DemoSqlValidationTest confronta tipi del registro, righe e sequenze dopo ricaricamenti e reset; assente il bootstrap automatico Java. |
| [Stati BLOCKED e SOLD del checkout funzionale](../../Relazione_SWE/capitoli/4_testing.tex#L117) | 4.4 | Selezionare un workflow effettivo in PurchaseAgreementIntegrationTest; la sola FullWorkflowIntegrationTest non percorre tutti i casi commerciali. |
| [Comando Maven ed esito storico](../../Relazione_SWE/capitoli/4_testing.tex#L131) | 4.5 | Comandi correnti per suite standard, profilo postgres-tests e harness separati; risultati storici datati, non i 57 test della vecchia versione. |

## Figure, schede e tabelle

| Materiale originale | Corrispondenza prevista |
| --- | --- |
| Tavola casi d’uso | Rifare/riadattare con la skill draw.io partendo da Users SWE, mantenendo la tavola degli autori in 2.1; dettagli per ruolo come approfondimento. |
| Sei template completi | Riportare UC-01, UC-02, UC-03, UC-05, UC-04, UC-06 in 2.2; mantenere campi, condizioni, flussi, alternative e riscontro dei test. In A restano UC-07–11 e UC-S01–04, senza duplicare le sei schede. |
| Diciotto schermate originali | Tutte tracciate nelle diciotto voci di 2.3: riusare screenshot attuali; ingresso e login condividono una vista, le richieste del venditore una tabella, Profilo è un dialogo. La panoramica prenotazioni del venditore non ha un equivalente corrente. Nessuna nuova schermata inventata. |
| Diagramma di navigazione | Rifare/riadattare con la skill draw.io da Page Navigation Diagram, mantenendo la tavola degli autori in 2.4; dettagliare tab, scene e dialoghi attuali. |
| Package diagram | Rifare/riadattare con la skill draw.io da Package diagram in 2.5, con dipendenze attuali e sottosezione non numerata sul dominio. |
| Quattro class diagram | Rifare/riadattare con la skill draw.io le quattro tavole originali di dominio, business, DAO e presentazione, nello stesso ordine in 2.6. Le viste correnti alimentano il lavoro; B completa la copertura degli undici package. |
| Tre sequenze | Rifare/riadattare con la skill draw.io le tre sequenze originali in 2.7; mantenere tavole d'insieme per vendita, noleggio e cessione, con approfondimenti su alternative e transazioni. |
| ER e schema relazionale testuale con legenda | Rifare/riadattare con la skill draw.io da SWE ER in 2.8; ER-01/02 e REL-01–03 completano la tavola degli autori. Affiancare le tredici relazioni con chiavi, nullabilità e legenda. |
| Tabella Origine degli importi | Riprenderne la funzione in 3.2.4: consigliato/effettivo, base manuale, acconto, saldo, noleggio e stima, con formula e fonte attuali. Il listino non modifica il totale già concordato. |
| Tabella Garanzie transazionali e vincoli | Riprenderne la funzione in 3.4.4–5: per ogni regola, meccanismo SQL/applicativo e limite. Includere connessioni autonome, Observer, vincoli temporali e identità senza vecchie lacune già corrette. |

Le undici figure di diagramma, le diciotto schermate, le sei tabelle dei template e le due tabelle argomentative originali hanno così una destinazione esplicita. Le suddivisioni in più tavole attuali non richiedono di duplicare l’argomento.

## Aggiunte attuali da conservare

- Capitolo 5 e relative quattro sezioni sull’AI, correggendo solo eventuali riferimenti coinvolti nel riallineamento e documentando anche questa attività.
- Garanzie e limiti introdotti dall’implementazione corrente: accordo economico, tipi espliciti, enum SQL, lock e concorrenza, identità, Observer transazionale e ciclo di vita delle tabelle.
- Tavole degli autori corrette, diagrammi attuali, screenshot con provenienza e prove storiche attribuite. Nuove acquisizioni solo quando il materiale riusato non basta a spiegare una funzione presente.
- Appendice A come schede aggiuntive; B come dettagli dei package; C come matrice estesa e provenienza delle verifiche. Il corpo deve restare comprensibile senza aprirle per ogni argomento principale.

## Esecuzione e verifica

L’[indice dettagliato](indice.md) applica questa matrice. La [specifica aggiornata](tracker/spec.md) e i ticket [09](tracker/issues/09-riallineamento-introduzione.md), [10](tracker/issues/10-riallineamento-progettazione.md), [11](tracker/issues/11-riallineamento-implementazione.md), [12](tracker/issues/12-riallineamento-testing.md), [13](tracker/issues/13-verifica-riallineamento.md) definiscono il lavoro successivo.

Il confronto struttura/contenuti, i rinvii e il PDF compilato sono il confine di verifica concordato. Ogni scostamento residuo richiede una motivazione collegata alla voce originale. I ticket dei capitoli sono indipendenti nei prerequisiti, ma si eseguono nell’ordine 1–4 per la revisione progressiva; il ticket finale dipende da tutti e quattro.

La [verifica di questa pianificazione](verifiche/allineamento-2026-11-16/verifica.md) attesta copertura e conservazione dei file, non il completamento della riscrittura.

La successiva [verifica dei vincoli sui diagrammi](verifiche/allineamento-2026-11-16/verifica-vincoli-diagrammi.md) riguarda la precisazione degli autori sull'uso obbligatorio della skill, sulle basi originali e sul rifacimento completo.
