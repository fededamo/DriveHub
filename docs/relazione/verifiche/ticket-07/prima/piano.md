# Riscrittura della relazione DriveHub

## Decisioni confermate

- La relazione viene riscritta da zero per descrivere l'implementazione attuale.
- Tutti i nuovi materiali della relazione risiedono in `docs/relazione/`.
- Il formato sorgente è LaTeX; il documento finale è un PDF.
- Tutti i nuovi diagrammi sono realizzati con draw.io, conservando i sorgenti
  modificabili `.drawio` e le esportazioni usate nella relazione.
- Il lavoro riguarda la documentazione. Eventuali difetti del software vengono
  segnalati per una correzione separata.
- Si definiscono prima un indice commentato e l'elenco dei diagrammi; si procede
  poi con la revisione di un capitolo alla volta.
- Il progetto viene presentato secondo il Mode #2.
- Il testo principale sarà sintetico e argomentato; le schede complete dei casi
  d'uso e le tabelle di verifica più estese saranno collocate in appendice.
- Si adotta il punto di vista della concessionaria: vendita al cliente e
  acquisto dal cliente, detto anche cessione del cliente. Le definizioni sono
  registrate in [GLOSSARY.md](GLOSSARY.md).
- Il frontespizio mantiene Federico D'Amodio e Matteo Orlandi come autori,
  Ingegneria del Software come corso, il prof. Enrico Vicario come docente
  e l'anno accademico 2025/2026.
- L'indice mantiene i primi quattro capitoli della relazione attuale, con la stessa
  struttura delle sezioni e lo stesso ordine. I contenuti vengono riscritti;
  i titoli delle sottosezioni legati a elementi rimossi vengono corretti quando
  necessario.
- Si aggiunge il capitolo 5, Uso dell'AI, dedicato alla fase manuale, alla
  revisione con Codex, ai controlli e ai limiti delle tracce disponibili.

## Origine del lavoro e uso dell'AI

L'utente dichiara che la progettazione, la struttura iniziale del software e
le prime classi sono state realizzate manualmente, mentre tutta la revisione
è stata svolta nelle sessioni con Codex. Questo è il significato di "scheletro"
confermato durante l'intervista.

Gli interventi e le verifiche della revisione saranno ricostruiti dai registri,
dalle evidenze e dalla cronologia Git disponibili. Queste fonti non vengono
presentate come una trascrizione completa di ogni conversazione o come una
nuova esecuzione dei controlli. L'attribuzione delle attività manuali proviene
dalla dichiarazione dell'utente.

Le fonti disponibili consentono di ricostruire queste categorie di revisione:

| Attività | Tracce conservate |
| --- | --- |
| Audit iniziale, pianificazione e diciotto ticket di revisione | [Fotografia dei registri](../evidence/revision-records.zip), che conserva piano, audit e tracker con manifest delle impronte. |
| Correzioni delle regole di dominio, operazioni economiche, identità e persistenza | Ticket e relative evidenze sotto `docs/evidence/r*/`. |
| Tabelle JavaFX asincrone, gestione degli errori e pulizia mirata | [Tabelle](../evidence/r08-tables/verification.md) e [pulizia](../evidence/r10-cleanup/verification.md). |
| Test di regressione, integrazione PostgreSQL, concorrenza e controlli JavaFX | [Verifica integrata](../evidence/r11-integrated/verification.md) e relativa [matrice](../evidence/r11-integrated/acceptance-matrix.md). |
| Documentazione tecnica, istruzioni e revisione dei diagrammi precedenti | [Istruzioni](../evidence/r09-instructions/verification.md) e [diagrammi](../evidence/r09-diagrams/verification.md). |
| Avvio Windows e istruzioni di esecuzione | Sezione sull'uso dell'AI del [README](../../README.md), che nomina esplicitamente Codex. |
| Semantica delle richieste e del registro, conversione e consolidamento dati | [Richieste](../evidence/request-semantics-01/verification.md), [registro](../evidence/sale-semantics-02/verification.md), [conversione](../evidence/data-conversion-03/verification.md) e [consolidamento](../evidence/db-consolidation-2026-11-09/verification.md). |
| Enumerazioni PostgreSQL e riesame delle modifiche | [Verifica enum](../evidence/postgres-enums-2026-11-09/verification.md) e [review](../evidence/postgres-enums-2026-11-09/review.md). |

Queste tracce riscontrano interventi e controlli; l'attribuzione complessiva
della revisione a Codex deriva anche dalla dichiarazione dell'utente. Non
dimostrano autonomamente l'origine di ogni riga o di ogni diagramma. Nelle fonti
lette non risultano prompt completi, versioni del modello, tempi complessivi o
percentuali di codice generato: questi dati non saranno inventati.

I diversi conteggi dei test appartengono a sessioni e insiemi di prove diversi.
In particolare il risultato indicizzato del 9 novembre è successivo al
consolidamento del 3 novembre; nessuno dei due costituisce una nuova verifica
eseguita durante questa intervista.

## Fonte della consegna

`Slides and notes - academic year 202526-20261020/NotesOnTheProjectWork_Jan2025.pdf`,
nella directory che contiene il progetto `DriveHub/`.

I riferimenti seguenti indicano le pagine fisiche del PDF, contando da 1.
La numerazione nei piè di pagina non coincide sempre con questo conteggio.
Il documento riporta A.A. 2024/25, versione 1.1 di gennaio 2025. Nella cartella
fornita non sono state individuate note sul progetto più recenti: l'altra
versione è marcata come superata. Non sono stati cercati aggiornamenti online.

## Vincoli ricavati dalla consegna

| Riferimento | Indicazione |
| --- | --- |
| Pagina 4 | Documentare ogni uso dell'AI: parti interessate e processo seguito, compresa l'eventuale integrazione nell'IDE. L'uso dell'AI è accolto ma non obbligatorio. |
| Pagina 4 | Evitare UML generato automaticamente dal codice. I diagrammi devono comunicare il progetto ed essere accompagnati da spiegazioni utili. |
| Pagina 9 | La struttura normalmente attesa comprende problema, architettura d'insieme, tecnologie e strumenti; requisiti e progetto funzionale; progetto; implementazione; test. Non sono imposti titoli letterali dei capitoli. |
| Pagine 9 e 13 | Descrivere requisiti e progetto funzionale con diagrammi dei casi d'uso e template. I template sono obbligatori; mockup e diagramma di navigazione sono raccomandati. |
| Pagine 9 e 35 | Presentare il package diagram e un class diagram per ciascun package, spiegando responsabilità, dipendenze e scelte progettuali. Evitare la semplice parafrasi dei diagrammi. |
| Pagina 9 | Discutere l'implementazione tramite frammenti significativi e commentati, idiomi, pattern e tecnologie. |
| Pagina 9 | Trattare test funzionali e strutturali. |
| Pagine 10, 17–18 e 37 | Frontespizio con corso, anno accademico, autori e docente; indice con titoli comprensibili. |
| Pagine 13, 22 e 44 | Le schede dei casi d'uso riportano identificativo, titolo, descrizione, livello, attori, precondizioni, postcondizioni, flusso principale e alternative riferite ai passi di origine. |
| Pagine 14, 30 e 45 | Documentare la base dati con diagramma ER e modello relazionale. |
| Pagine 26–29 | Motivare i test significativi, collegandoli ai flussi applicativi e alle parti critiche; trattare anche DAO e query pertinenti. Non è indicata una soglia quantitativa di copertura. |
| Pagina 33 | Quando l'interfaccia esiste, gli screenshot possono sostituire i mockup. |
| Pagine 34 e 47 | La navigazione può essere rappresentata separatamente per tipologia di utente. |
| Pagina 2 | Le note richiedono PDF allegato a un'email almeno tre giorni lavorativi prima della discussione, oggetto con `[SWE]` e conservazione del thread. Il collegamento GitHub è suggerito. Nessun invio è previsto in questa attività. |

Il PDF non prescrive limiti di pagine, un numero minimo di casi d'uso o di figure,
né diagrammi di sequenza o di stato. Questi ultimi si includeranno soltanto se
utili a spiegare scelte e flussi significativi.

Per il Mode #2 scelto, le indicazioni sui due o tre pattern descritte per il
Mode #1 non costituiscono un numero minimo obbligatorio.

## Riscontri iniziali sulla documentazione precedente

- La vecchia introduzione e il capitolo database descrivono H2 e bootstrap
  automatico, assenti dall'implementazione attuale basata su PostgreSQL.
- Il vecchio capitolo test cita anche classi non più presenti. I riferimenti
  dovranno essere ricostruiti sui test effettivamente disponibili.
- I draw.io, l'ER, i mockup e gli screenshot esistenti sono dichiarati storici
  dall'indice `docs/diagrams/README.md`. Le viste correnti elencate in quell'indice
  sono PlantUML e SVG: non costituiscono già i nuovi diagrammi draw.io.

## Criteri di lavoro

- Verificare le affermazioni tecniche su codice, schema dati e prove pertinenti
  alla versione descritta.
- Usare la vecchia relazione e le evidenze precedenti come fonti da verificare.
  Un risultato storico non attesta una nuova esecuzione dei test.
- Distinguere funzionalità implementate, limiti e requisiti eventualmente non
  soddisfatti.
- Registrare qui le decisioni editoriali. Il glossario conterrà soltanto termini
  di dominio e relative definizioni; le decisioni architetturali già documentate
  restano in `docs/adr/`.

## Stato della pianificazione

- L'[indice di riferimento](indice.md) conserva la struttura dei primi quattro
  capitoli e aggiunge il capitolo 5 sull'AI. I dettagli estesi sono in appendice.
- La configurazione approvata è installata in [AGENTS](../AGENTS.md), con
  [tracker locale](../agents/issue-tracker.md), etichette predefinite e riuso
  del glossario e degli ADR esistenti.
- La [specifica](tracker/spec.md) è pubblicata nel tracker locale con stato
  `ready-for-agent`.

## Prossimo passaggio

Suddividere la specifica in ticket locali con criteri di accettazione e
dipendenze, quindi procedere con la stesura e la revisione di un capitolo
alla volta.
