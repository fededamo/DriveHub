# Riscrittura della relazione DriveHub

## Decisioni confermate

- La prima riscrittura descrive l'implementazione attuale. La fase corrente
  riallinea struttura interna e contenuti alla relazione di riferimento,
  riusando il materiale valido già prodotto.
- Tutti i nuovi materiali della relazione risiedono in `docs/relazione/`.
- Il formato sorgente è LaTeX; il documento finale è un PDF.
- Tutti i diagrammi, compresi rifacimenti, adattamenti ed esportazioni di
  tavole esistenti, usano sempre la skill `drawio:drawio`. Si rifanno/riadattano
  tutti gli undici diagrammi originali partendo dai disegni degli autori,
  secondo i [vincoli della matrice](allineamento.md#diagrammi-vincoli-operativi),
  conservando sorgenti `.drawio` modificabili ed esportazioni verificate.
- Il lavoro riguarda la documentazione. Eventuali difetti del software vengono
  segnalati per una correzione separata.
- Si definiscono prima un indice commentato e l'elenco dei diagrammi; si procede
  poi con la revisione di un capitolo alla volta.
- Il progetto viene presentato secondo il Mode #2.
- Il testo principale sarà argomentato e seguirà gli stessi argomenti e la
  stessa profondità della relazione di riferimento. Le sei schede principali
  saranno complete nel corpo; schede aggiuntive, dettagli delle classi e
  matrice estesa delle verifiche resteranno in appendice.
- Si adotta il punto di vista della concessionaria: vendita al cliente e
  acquisto dal cliente, detto anche cessione del cliente. Le definizioni sono
  registrate in [GLOSSARY.md](GLOSSARY.md).
- Il frontespizio mantiene Federico D'Amodio e Matteo Orlandi come autori,
  Ingegneria del Software come corso, il prof. Enrico Vicario come docente
  e l'anno accademico 2025/2026.
- L'indice mantiene i primi quattro capitoli della relazione di riferimento,
  con la stessa gerarchia e ordine di sezioni, sottosezioni e argomenti. I
  titoli legati a elementi rimossi vengono adattati, motivando lo scostamento;
  non si ripristinano componenti o funzioni assenti dal software attuale.
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

- L'[indice dettagliato](indice.md) e la [matrice](allineamento.md) precisano
  struttura e contenuti dei primi quattro capitoli e conservano il 5 sull'AI.
  Le sei schede principali tornano nel corpo, i dettagli ulteriori in appendice.
- La configurazione approvata è installata in [AGENTS](../AGENTS.md), con
  [tracker locale](../agents/issue-tracker.md), etichette predefinite e riuso
  del glossario e degli ADR esistenti.
- La [specifica](tracker/spec.md) è pubblicata nel tracker locale con stato
  `ready-for-agent`.

## Avanzamento documentale — 13 novembre 2026

I ticket documentali 01–06 sono conclusi; capitoli 1–4 e appendici A–C sono
completi e proposti agli autori, non approvati. Il ticket 07 completa il
capitolo 5 nelle quattro sezioni dell'indice, sulla richiesta operativa
ricevuta: [specifica e verifiche](verifiche/ticket-07/README.md).
La dichiarazione sull'origine manuale e sulla revisione con Codex resta
quella registrata sopra; la nuova ricostruzione non attribuisce le singole righe.
Non sono stati eseguiti test applicativi, accessi DB, harness o commit.

## Sequenza del riallineamento — stato alla chiusura del ticket 12

Riallineamento secondo la matrice e l'indice dettagliato del 16 novembre 2026:
ticket 09–12 sui capitoli 1–4, poi ticket 13 di integrazione, compilazione e
verifica completa. Il ticket 09 è concluso tecnicamente, con review Standards e Spec senza rilievi: mantiene il testo del capitolo 1 e
riporta il punto sull’ambiente prima dei processi esclusi, come nell’originale.
[Confronto e nuove verifiche](verifiche/ticket-09/README.md). Il ticket 10 è
concluso tecnicamente sul capitolo 2 e A–B: 47 voci, sei schede nel corpo,
diciotto funzioni UI e undici tavole originali riallineate; PDF completo
ricompilato, estratto e review Standards/Spec senza rilievi aperti.
[Confronto, fonti e verifiche 10](verifiche/ticket-10/README.md).
Il ticket 11 è concluso tecnicamente il 17 novembre: capitolo 3 nelle 35
voci originali, quattordici frammenti originali coperti da 27 estratti
correnti verificati; PDF completo di 143 pagine e capitolo di 23 pagine.
[Confronto, fonti, controlli e review 11](verifiche/ticket-11/README.md).
Le due correzioni documentali delle review sono ricontrollate, senza rilievi
aperti. Capitoli 1/2, altre parti e storici conservati; restano i cinque
messaggi TeX preesistenti in C. La chiusura del ticket 11 non attesta quelle successive.
Il ticket 12 è concluso tecnicamente il 17 novembre: capitolo 4 nelle
quindici voci, nove funzioni originali coperte e tre estratti iniziali
conservati; appendice C coerente con 2.2/A, PDF completo di 151 pagine
ed estratto 4/C di 22. [Fonti, controlli, conservazione e review 12](verifiche/ticket-12/README.md).
Standards e Spec conformi, precisazione heap ricontrollata nel PDF;
cinque messaggi TeX preesistenti in C ancora dichiarati e osservati.
Capitoli 1–3, altre parti, tavole e storici conservati. Alla chiusura del 12,
il ticket 13 restava da eseguire, senza attestare anticipatamente il pacchetto.
La revisione degli autori resta distinta dalla chiusura tecnica e dalle
review assistite; nessun invio al docente o pubblicazione è autorizzato.

## Verifica finale e pacchetto — 14 novembre 2026

Il ticket documentale 08 riscontra il documento unitario rispetto a specifica,
indice e note del corso, conserva la baseline anche dei non tracciati e prepara
PDF e ZIP autonomo per gli autori. [Verifiche e matrice finale](verifiche/ticket-08/README.md).
Le sole correzioni al testo rimuovono un rinvio a un capitolo ancora da redigere
e precisano in UC-05 il limite dell'accredito zero già dichiarato nel corpo;
rimossa anche la macro inutilizzata per i segnaposto. Diagrammi riusati.
La compilazione finale usa XeLaTeX/TeX Live 2025 predisposto in `/tmp`, senza
input editoriali esterni. Vecchia relazione, evidenze e manifest 01–07 restano
intatti. Nessuna compilazione Java, prova applicativa, accesso DB, harness o commit.
Il completamento tecnico, documentato nelle verifiche 08, non chiude la
revisione degli autori: i limiti applicativi e di riproduzione restano espliciti.

15 novembre 2026: ticket 08 concluso tecnicamente, dopo le
[review indipendenti Standards e Spec e i ricontrolli](verifiche/ticket-08/revisione.md),
senza rilievi aperti. Il rilievo Spec sull'omessa valutazione di cinque messaggi
TeX è chiuso con verbale corretto e nuova osservazione delle pagine 101–106:
nessun difetto visivo rilevato; i messaggi del motore restano un
[limite dichiarato](verifiche/ticket-08/valutazione-messaggi-tex.md).
PDF completo e ZIP autonomo sono proposti agli autori, la cui revisione
resta da svolgere. Nessuna approvazione umana, invio o pubblicazione.

## Integrazione delle tavole degli autori — 15 novembre 2026

Su richiesta esplicita degli autori, la relazione integra anche le loro
tre tavole `Page Navigation Diagram.drawio`, `SWE ER.drawio` e
`Users SWE.drawio`, correggendo i contenuti superati con la skill draw.io
e mantenendo stile, struttura, forme e notazione. Le copie corrette sono
in `diagrammi/*-autori.drawio`; gli originali in `docs/` restano conservati.
Le tavole d'insieme A3 affiancano i dettagli A4, senza sostituirli.
L'ER mantiene il supertipo concettuale User, distinguendolo esplicitamente
dal mapping SQL e dai limiti dei vincoli globali.
Il [nuovo pacchetto e le verifiche](verifiche/tavole-autori/README.md)
seguono il ticket 08; le evidenze precedenti non vengono riscritte.

## Riallineamento richiesto dagli autori — 16 novembre 2026

Gli autori giudicano positivamente la nuova relazione ma chiedono stessa
scansione e stessi contenuti interni di `Relazione_SWE`, riadattati al codice
attuale. Autorizzato il percorso proposto: matrice e indice dettagliato,
aggiornamento della specifica, ticket per capitolo e verifica finale.

Il confronto segue gli input del documento originale: 24 file attivi,
102 voci e 23 frammenti, oltre a diagrammi, schermate, schede e tabelle.
I file originali non inclusi da `main.tex` non aggiungono requisiti.
La [matrice](allineamento.md) distingue contenuti da mantenere, ricollocare,
ripristinare nella struttura o adattare per componenti rimossi.

In particolare:

- Le sei schede complete tornano in 2.2 nell'ordine originale; UC-05 precede
  UC-04 nell'esposizione senza rinumerare i casi.
- Il capitolo 2 riprende il dettaglio per funzione della UI e una tavola
  significativa per ognuno dei quattro gruppi di classi nel corpo.
- Il capitolo 3 torna a entità, responsabilità e flussi distinti, mapping e
  pattern; Strategy, controller FXML e UnitOfWork ricevono una spiegazione
  attuale, senza attribuire al software componenti che non ha.
- Il capitolo 4 recupera i singoli argomenti di prova, distinguendo classi
  senza DB, integrazione PostgreSQL e harness JavaFX. Non si inventano test
  isolati in sostituzione delle vecchie classi.
- Capitolo 5, contenuti validi e tracce storiche restano; appendici e rinvii
  saranno adattati per completare il corpo senza duplicare le sei schede.

I ticket dei capitoli non hanno dipendenze tecniche tra loro; si lavorano in
ordine 1–4 come già concordato. Il ticket finale dipende da tutti e quattro.
La [verifica della pianificazione](verifiche/allineamento-2026-11-16/verifica.md)
non attesta ancora un nuovo PDF: i sorgenti LaTeX, il PDF e lo ZIP precedenti
restano la prima stesura completa, proposta agli autori e da riallineare.

Precisazione successiva degli autori, 16 novembre 2026: l'obiettivo è rifare
la vecchia relazione mantenendo tutto, riadattato esattamente al progetto
attuale. La matrice rende ora obbligatori la skill draw.io per ogni
intervento, le tre basi SWE ER, Users SWE e Page Navigation Diagram e anche
il distinto Package diagram per i package. Tutte le undici tavole della
relazione originale vanno rifatte/riadattate; le viste attuali sono basi e
approfondimenti, non sostituti del percorso originale.

## Integrazione del riallineamento — 17 novembre 2026

Il ticket 13 riusa i capitoli conclusi e le undici tavole del 10. Consolida
102 voci e 23 funzioni dei frammenti, diciotto funzioni UI, sei schede e due
tabelle; aggiorna il capitolo 5 sul lavoro documentale 09–13 senza attribuzioni
non dimostrate. [Corrispondenze e fonti](verifiche/ticket-13/README.md).
Il PDF mantiene 151 pagine: solo 119–121 cambiano contenuto; le altre 148
sono identiche a testo e pixel, compreso l’indice. Tutte osservate, A3 incluse.
I cinque messaggi TeX preesistenti in C sono confrontati e valutati, non
rimossi. Lo ZIP aggiornato di 118 membri è estratto e ricompilato realmente:
73 input editoriali interni, tutte le pagine uguali per contenuto e resa.
Le review indipendenti Standards e Spec del nuovo documento sono chiuse
senza rilievi; il ticket 13 è concluso tecnicamente (`done`).
PDF e sorgenti proposti agli autori, senza approvazione
presunta, invio, pubblicazione o commit. Nessuna attività applicativa eseguita.
