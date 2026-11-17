# Specifica della riscrittura della relazione DriveHub

Status: done

Revisione del 16 novembre 2026: riallineamento della prima stesura completa
alla struttura interna di `Relazione_SWE`. La [matrice](../allineamento.md)
e l'[indice dettagliato](../indice.md) precisano il contenuto delle voci
originali; i ticket 01–08 e le loro verifiche restano storici.

## Problem Statement

La prima riscrittura descrive l'implementazione attuale e conserva i quattro
capitoli e le sezioni principali della relazione precedente. Gli autori
chiedono però maggiore fedeltà anche nella struttura dei contenuti: diverse
sottosezioni sono state accorpate, i flussi implementativi hanno perso la
scansione originale e le sei schede complete dei casi principali sono state
spostate in appendice.

La relazione di riferimento contiene anche componenti, comportamenti e prove
superati. Recuperarne il percorso espositivo richiede di riadattare il
contenuto al progetto corrente, senza ripristinare FXML, H2, UnitOfWork,
strategie o classi di test non più presenti. I contenuti validi della nuova
stesura devono essere conservati e ricollocati dove pertinenti.

## Solution

Riallineare la relazione in italiano con sorgenti LaTeX e PDF finale,
conservando i primi quattro capitoli, le sezioni, le sottosezioni e l'ordine
degli argomenti del documento originale effettivamente compilato:
Introduzione; Analisi dei requisiti e progettazione; Implementazione; Testing.
Mantenere il capitolo 5, Uso dell'AI.

L'obiettivo è rifare l'intera relazione originale mantenendone tutti i
contenuti, spiegazioni, schede, esempi, frammenti, figure e tabelle,
riadattati esattamente alla nuova implementazione. Per tutti i diagrammi
si usa sempre la skill `drawio:drawio`, partendo dai disegni degli autori
per ER, casi d'uso, navigazione e package e rifacendo tutte le tavole della
vecchia relazione secondo i [vincoli operativi](../allineamento.md#diagrammi-vincoli-operativi).

Il corpo riprende spiegazioni, esempi, schede e frammenti significativi con
profondità analoga. Le sei schede principali tornano complete nella sezione
Template e una tavola significativa accompagna ciascuno dei quattro gruppi
di classi. Le appendici raccolgono schede aggiuntive, dettagli delle classi e
matrice estesa delle verifiche. Screenshot ed estratti correnti validi
alimentano la riscrittura; tutti i diagrammi seguono il rifacimento o
adattamento obbligatorio con la skill draw.io. Lavorare e rivedere un capitolo alla volta, fino alla verifica
del documento completo.

## User Stories

1. Come autore, voglio una relazione riscritta per l'implementazione attuale, per eliminare le descrizioni della versione superata.
2. Come autore, voglio conservare struttura e ordine di sezioni, sottosezioni e argomenti dei primi quattro capitoli, aggiornando i titoli obsoleti, per mantenere il percorso della relazione di riferimento.
3. Come autore, voglio un quinto capitolo dedicato all'uso dell'AI, per rendere esplicito il processo di sviluppo e revisione.
4. Come candidato, voglio una relazione coerente con il Mode #2 e con le note sul progetto fornite, per preparare la discussione secondo le indicazioni del corso.
5. Come autore, voglio sorgenti LaTeX e un PDF finale autonomo, per poter modificare e consegnare il documento.
6. Come autore, voglio tutti i materiali della nuova relazione nella cartella documentale concordata, per mantenerli raccolti e riconoscibili.
7. Come autore, voglio il frontespizio con autori, corso, docente e anno accademico confermati, per identificare correttamente la consegna.
8. Come lettore, voglio un indice comprensibile e riferimenti interni risolti, per orientarmi tra capitoli, figure e appendici.
9. Come autore, voglio rivedere un capitolo alla volta, per verificare progressivamente contenuti e scelte editoriali.
10. Come lettore, voglio un'introduzione con contesto, obiettivi e ruoli della concessionaria, per comprendere il problema affrontato.
11. Come lettore, voglio tecnologie e semplificazioni corrispondenti al sistema reale, per conoscere il perimetro effettivo del progetto.
12. Come lettore, voglio distinguere vendita al cliente e acquisto dal cliente, per comprendere sempre il punto di vista della concessionaria.
13. Come lettore, voglio casi d'uso per cliente, venditore e manager, per riconoscere obiettivi e responsabilità dei ruoli.
14. Come lettore, voglio distinguere obiettivi applicativi e funzionalità di supporto, per interpretare correttamente il livello dei casi d'uso.
15. Come lettore, voglio le sei schede complete dei casi principali nel corpo, nello stesso ordine dell'originale, per seguire attori, condizioni, flusso principale, alternative e prove senza rinvii obbligati all'appendice.
16. Come lettore, voglio identificativi e alternative coerenti tra diagrammi e schede, per ricondurre ogni variante al passo che la origina.
17. Come lettore, voglio screenshot dell'interfaccia attuale, per riconoscere informazioni e controlli descritti nei flussi.
18. Come lettore, voglio diagrammi di navigazione per ruolo, per seguire i percorsi tra accesso, dashboard, tab e dialoghi.
19. Come lettore, voglio un package diagram spiegato, per comprendere responsabilità e direzione delle dipendenze.
20. Come lettore, voglio viste delle classi che coprano ciascuno degli undici package di produzione, per comprendere il progetto nel suo insieme.
21. Come lettore, voglio diagrammi selettivi con relazioni effettive e riferimenti per ID espliciti, per comprendere le collaborazioni senza associazioni inventate.
22. Come autore, voglio diagrammi realizzati con draw.io e sorgenti modificabili conservati, per poterli correggere e riutilizzare.
23. Come lettore, voglio ER e modello relazionale coerenti con lo schema corrente, per comprendere persistenza, cardinalità e vincoli.
24. Come lettore, voglio capire base automatica, base manuale e formula dei prezzi, per distinguere la scelta della base dal prezzo risultante.
25. Come lettore, voglio capire come il totale viene fissato all'acconto e usato al saldo, per riconoscere le garanzie dell'accordo economico.
26. Come lettore, voglio richieste e registro spiegati dal punto di vista della concessionaria, per distinguere vendita, acquisto e penale.
27. Come lettore, voglio una sequenza della vendita al cliente, per seguire acconto, esito della richiesta e saldo.
28. Come lettore, voglio una sequenza dell'acquisto dal cliente, per seguire proposta, esito e registrazione dell'operazione.
29. Come lettore, voglio una sequenza del noleggio e delle cancellazioni collegate dei test drive, per comprendere conflitti e atomicità.
30. Come lettore, voglio frammenti di codice significativi e commentati, per collegare le scelte progettuali alla realizzazione.
31. Come lettore, voglio una spiegazione di transazioni, rollback e concorrenza, per comprendere i confini delle garanzie offerte.
32. Come lettore, voglio distinguere pattern, idiomi e meccanismi architetturali effettivamente adottati, per valutarne motivazioni e limiti.
33. Come lettore, voglio capire navigazione e caricamenti asincroni JavaFX, per comprendere aggiornamenti, errori e ciclo di vita della presentazione.
34. Come lettore, voglio capire gestione dell'identità, conto dimostrativo e mapping JDBC, per comprendere i confini tra servizi, dominio e persistenza.
35. Come lettore, voglio una strategia che distingua test unitari, integrazione e verifiche dell'interfaccia, per capire cosa dimostra ogni prova.
36. Come lettore, voglio collegare requisiti e flussi ai test pertinenti, per valutare la copertura funzionale documentata.
37. Come lettore, voglio esempi di verifica delle invarianti, degli errori e dei conflitti concorrenti, per comprendere il comportamento nei casi critici.
38. Come lettore, voglio risultati associati a versione, ambiente e data, per distinguere le prove storiche dalle nuove esecuzioni.
39. Come lettore, voglio limiti e prove non eseguite dichiarati esplicitamente, per evitare conclusioni non sostenute dalle evidenze.
40. Come lettore, voglio la fase manuale distinta dalla revisione assistita da Codex, per comprendere il contributo dichiarato degli autori e dell'AI.
41. Come lettore, voglio attività di revisione, controllo umano e verifiche ricostruiti dalle tracce disponibili, per valutare il processo seguito.
42. Come lettore, voglio documentato anche l'uso dell'AI per relazione e diagrammi, per conoscere tutte le parti interessate dal supporto generativo.
43. Come lettore, voglio limiti della ricostruzione dell'uso AI chiaramente indicati, per non scambiare i registri per una cronologia completa.
44. Come lettore, voglio schede aggiuntive, tavole di dettaglio e matrice estesa in appendice, per approfondire mantenendo nel corpo le sei schede principali e le viste significative dei quattro gruppi di classi.
45. Come autore, voglio una verifica finale di compilazione, leggibilità e coerenza, per consegnare un PDF utilizzabile.
46. Come autore, voglio gli artefatti precedenti distinguibili e conservati come storico, per preservare le fonti senza confonderle con la nuova consegna.
47. Come autore, voglio una corrispondenza esplicita per tutte le voci della relazione originale compilata, per riconoscere contenuti conservati, ricollocati e adattati.
48. Come lettore, voglio le funzioni dell'interfaccia spiegate nello stesso percorso originale, per comprendere schermate, comandi, risultati e funzioni ora assenti.
49. Come lettore, voglio il Domain Model discusso per utenti, veicoli, prenotazioni, operazioni ed eventi, per collegare invarianti e scelte alle entità che le applicano.
50. Come lettore, voglio responsabilità, quattro flussi applicativi, prezzi, identità e Observer distinti nel Business Layer, per seguire la realizzazione senza ricostruirla da paragrafi trasversali.
51. Come lettore, voglio mapping utenti, mapping operazioni, vincoli e connessione trattati separatamente, per capire dove risiedono le garanzie e i loro limiti.
52. Come lettore, voglio i temi dei pattern originali riesaminati sul codice attuale, per distinguere collaborazioni presenti e meccanismi rimossi.
53. Come lettore, voglio sottosezioni di testing dedicate agli stessi argomenti originali con prove e oracoli correnti, per distinguere test in memoria, integrazione PostgreSQL e controlli UI.
54. Come autore, voglio figure, frammenti e tabelle argomentative tracciati insieme alla struttura, per verificare la profondità dei contenuti oltre alla coincidenza dei titoli.

## Implementation Decisions

1. **Perimetro documentale.** Il lavoro modifica relazione, diagrammi, figure,
   appendici e istruzioni documentali pertinenti. Gli eventuali difetti del
   software vengono segnalati per una correzione separata.
2. **Organizzazione degli artefatti.** Tutti i nuovi materiali della relazione
   risiedono nell'area documentale concordata. Il tracker è locale e conserva
   specifica e futuri ticket separati; gli stati triage usano il vocabolario
   predefinito già confermato.
3. **Struttura dei capitoli.** I primi quattro capitoli mantengono gerarchia e
   ordine di tutte le voci effettivamente incluse nel documento precedente,
   comprese sottosezioni, sottosottosezioni e la sottosezione non numerata sul
   dominio. Ogni voce riceve contenuto attuale o una spiegazione della sua
   assenza. I titoli che citano componenti o prove rimossi vengono adattati;
   non si conservano affermazioni obsolete per lasciare immutati i nomi.
4. **Capitolo aggiuntivo.** Il capitolo 5 tratta fase manuale, revisione assistita
   da Codex, controllo umano e documentazione del processo, compresi diagrammi
   e riscrittura della relazione.
5. **Frontespizio.** Autori Federico D'Amodio e Matteo Orlandi; corso Ingegneria
   del Software; docente prof. Enrico Vicario; A.A. 2025/2026. Il contesto
   universitario resta quello della relazione precedente.
6. **Consegna di riferimento.** Si applicano le note sul project work di gennaio
   2025 fornite con i materiali del corso, distinguendo prescrizioni,
   raccomandazioni ed esempi. Il progetto è presentato secondo il Mode #2:
   non si introduce un numero minimo artificiale di pattern.
7. **Base tecnica.** Le affermazioni sono riscontrate su codice, schema e
   configurazione attuali. Si esplicitano le semplificazioni reali e si
   sostituiscono i riferimenti a H2, bootstrap e test non più presenti.
8. **Requisiti e casi d'uso.** I diagrammi coprono i tre ruoli e distinguono le
   funzionalità di supporto. I template completi includono identificativo,
   titolo, descrizione, livello, attori, precondizioni, postcondizioni, flusso
   principale, alternative riconducibili ai passi di origine e riscontri dei
   test. I sei principali sono nel corpo nell'ordine originale: test drive,
   noleggio, vendita al cliente, cessione, autorizzazione e sconti. La cessione
   conserva UC-05 e l'autorizzazione UC-04, anche quando l'ordine espositivo
   non coincide con quello numerico.
9. **Interfaccia.** La sezione Mockups conserva la propria collocazione e usa
   screenshot dell'interfaccia attuale, ammessi dalle note del corso. Le
   schermate storiche non diventano prove della versione corrente. La
   navigazione è rappresentata per ruolo.
   Si ripristina il dettaglio per funzione dell'originale, distinguendo viste
   condivise, tab e dialoghi. La Welcome Page e la panoramica prenotazioni
   del venditore non vengono inventate: si esplicita il comportamento attuale.
10. **Diagrammi.** Usare sempre la skill `drawio:drawio` per rifacimenti,
    adattamenti, correzioni, creazioni ed esportazioni. Rifare/riadattare
    tutti gli undici diagrammi originali nella stessa collocazione,
    partendo dai disegni degli autori e mantenendone stile e struttura.
    Seguire l'elenco delle basi e delle corrispondenze nei vincoli operativi
    della matrice. Le tavole attuali sono materiale di lavoro, non un
    sostituto delle corrispondenze obbligatorie. Conservare sorgenti nativi
    modificabili ed esportazioni, con riscontro sul codice e controllo
    visivo nel PDF; nessuna esportazione UML automatica dal codice.
11. **Copertura strutturale.** Package diagram e viste delle classi coprono gli
    undici package di produzione. Si mantiene l'organizzazione espositiva in
    dominio, business, DAO e presentazione. Ogni package resta identificabile;
    si mostrano membri e relazioni utili a spiegare responsabilità e invarianti.
    I riferimenti per ID non diventano associazioni a oggetti inesistenti.
    I quattro diagrammi di classe originali, rifatti/riadattati, restano nel
    corpo; l'appendice raccoglie i dettagli per la copertura completa dei package.
12. **Flussi dinamici.** La sezione Sequence Diagram conserva tre flussi:
    vendita al cliente; acquisto dal cliente; noleggio con cancellazioni
    collegate dei test drive. Le sequenze spiegano collaborazioni e confini
    transazionali, mantenendo riconoscibile la struttura esistente.
13. **Database.** ER e modello relazionale descrivono lo schema PostgreSQL
    attuale, inclusi vincoli e quattro domini enumerati nativi. La scelta
    dell'ADR 0002 resta valida; non viene ricreato un sistema di migrazioni
    o uno script di conversione ritirato.
14. **Terminologia.** Si usa il glossario concordato: vendita al cliente e
    acquisto dal cliente, detto anche cessione del cliente. Richieste e
    registrazioni economiche vengono distinte; i nomi tecnici non sostituiscono
    una spiegazione dei loro diversi significati nei flussi.
15. **Prezzi e accordo.** Si rispetta l'ADR 0001: la base manuale sostituisce la
    base del modello nella stessa formula e non coincide con un prezzo finale
    imposto. Il totale viene fissato all'acconto; il saldo utilizza tale totale.
16. **Implementazione e pattern.** Si usano frammenti selezionati e commentati
    per spiegare dominio, servizi, presentazione e DAO. Si distinguono factory
    statiche da Factory Method GoF e meccanismi DAO e DI da pattern GoF. Observer
    e navigazione nello stile Visitor vengono descritti con garanzie e limiti
    effettivi; non si attribuiscono al progetto proprietà architetturali ideali.
    Si recupera la scansione originale per entità, servizi e quattro flussi,
    presentazione, cinque argomenti DAO e sei temi dei pattern. Il calcolatore
    concreto non diventa Strategy; il coordinamento JDBC non ripristina il
    vecchio contratto UnitOfWork. Formule, origine degli importi e tabella
    delle garanzie mantengono una funzione esplicativa nel corpo.
17. **Appendici.** Le schede aggiuntive dei casi d'uso, le tavole delle classi
    estese e la matrice requisiti–flussi–test–risultati completano il corpo.
    Le sei schede principali sono presenti una sola volta nella sezione
    Template; i rinvii mantengono gli identificativi. Le istruzioni pertinenti
    alla riproduzione delle prove restano nella sezione finale del Testing,
    coerenti con la documentazione tecnica.
18. **Origine del lavoro.** Progettazione, struttura iniziale del software e
    prime classi manuali sono dichiarazioni confermate dall'utente. La revisione
    con Codex viene ricostruita per attività e verifiche utilizzando audit,
    piano, ticket, cronologia e verbali disponibili.
19. **Documentazione dell'AI.** Il capitolo dedicato distingue decisioni degli
    autori, contributi dell'AI e controlli documentati. Include la produzione
    della nuova relazione e dei diagrammi. Non inventa prompt, versioni dei
    modelli, tempi complessivi, percentuali di codice generato o una cronologia
    completa delle conversazioni.
20. **Conservazione dello storico.** La relazione precedente e le evidenze
    pregresse restano distinguibili dalla nuova consegna. Un eventuale riordino
    preserva fonti, risultati storici e collegamenti pertinenti.
21. **Esecuzione progressiva.** La prima stesura completa è la base del
    riallineamento. Matrice e indice dettagliato precedono le modifiche al
    LaTeX. I capitoli si lavorano e si presentano nell'ordine 1–4; il capitolo
    5 e le appendici sono verificati nell'integrazione finale. La conclusione
    tecnica di un ticket non attribuisce approvazione agli autori.
22. **Testing argomentato.** Si recuperano tecnologie, invarianti, identità,
    prezzi/sicurezza, vendite, noleggio/conto, transazioni/Observer, mapping e
    schema/demo. I nomi delle vecchie classi sono sostituiti dai test reali.
    Se manca una prova unitaria equivalente, lo si dichiara e si rinvia alla
    prova d'integrazione pertinente senza riclassificarla come unitaria.

## Testing Decisions

Il confine principale di verifica è il PDF completo prodotto dai sorgenti
LaTeX: deve essere autonomo, coerente con l'indice e con i contenuti concordati,
leggibile e privo di riferimenti irrisolti. I controlli sulle fonti tecniche e
sugli artefatti supportano questa verifica; non richiedono nuove interfacce nel
software applicativo.

1. **Compilazione e struttura.** Verificare la compilazione reale, gli input,
   le figure, i riferimenti e l'indice. Nella consegna finale non rimangono
   sezioni provvisorie o segnaposto usati durante il lavoro progressivo.
   Confrontare la struttura con tutte le 102 voci originali e verificare
   destinazione e trattamento dei 23 frammenti, delle schermate, delle schede
   e delle tabelle argomentative. La copertura dei titoli da sola non basta:
   ciascun argomento deve conservare una spiegazione di profondità adeguata.
2. **Corrispondenza con la consegna.** Controllare presenza e completezza dei
   template, copertura dei package, modello dei dati, discussione
   dell'implementazione e test, documentazione dell'uso dell'AI.
3. **Diagrammi e figure.** Aprire o renderizzare tutti i sorgenti draw.io e
   controllare visivamente tutte le figure nel documento: testo, dimensioni,
   collegamenti e cardinalità. La validità XML o il successo dell'esportazione
   non dimostrano da soli correttezza semantica e leggibilità.
   Per ognuno degli undici diagrammi originali registrare base, adattamenti,
   sorgente `.drawio`, esportazione e verifica; controllare l'uso della
   skill e dei disegni degli autori anche per tavole preesistenti.
4. **Riscontro tecnico.** Confrontare le affermazioni rilevanti con sorgenti,
   schema, configurazione e ADR pertinenti. Verificare in particolare formule,
   totale concordato, tipi di operazione, transazioni, gestione dell'identità
   e comportamento della presentazione.
5. **Tracciabilità funzionale.** Collegare casi d'uso e alternative ai test
   pertinenti, spiegando ciò che ciascuna prova dimostra. Distinguere test
   unitari, integrazione PostgreSQL e controlli JavaFX; il numero dei test
   riusciti non diventa una percentuale di copertura.
6. **Evidenze riutilizzabili.** Usare come riferimenti i test di dominio,
   servizi e DAO, le prove PostgreSQL isolate, i controlli concorrenti e gli
   harness dell'interfaccia già disponibili. Nuove esecuzioni usano ambienti
   dedicati e registrano versione, data, prerequisiti, risultati e limiti.
7. **Risultati e limiti.** Separare risultati storici, nuove esecuzioni e
   controlli statici. Una prova non eseguita resta dichiarata tale; l'esistenza
   di un test non dimostra automaticamente ogni messaggio di un diagramma.
8. **Dati e sicurezza delle prove.** Riutilizzare l'isolamento già previsto
   per il database di test e dati dimostrativi per le schermate. Le attività
   documentali non reinizializzano archivi da conservare e non pubblicano
   credenziali personali o backup privati.
9. **Revisione umana.** Presentare i capitoli nell'ordine concordato e
   controllare alla fine il percorso di lettura complessivo, oltre alle singole
   parti. Segnalare separatamente eventuali difetti applicativi emersi.

La verifica di completamento richiede sorgenti modificabili ed esportazioni
coerenti, compilazione e controllo visivo effettivi del PDF, copertura dei
contenuti previsti e attribuzione verificabile di ogni risultato dichiarato.
I limiti residui devono essere espliciti; non è richiesto introdurre codice
applicativo o test nuovi soltanto per questa riscrittura.

## Out of Scope

- Correzioni, nuove funzionalità e refactoring del software applicativo.
- Modifiche allo schema o ai dati operativi, ripristino di conversioni ritirate
  e introduzione di nuove migrazioni.
- Cambiamenti all'ordine degli argomenti dei primi quattro capitoli oltre
  agli adattamenti motivati di componenti, funzioni o prove rimossi.
- Ripristino di funzionalità assenti o produzione di nuovi test applicativi
  soltanto per riempire le voci della relazione precedente.
- Imposizione dello stesso numero di pagine, figure o righe di codice
  dell'originale come sostituto della verifica dei contenuti.
- Introduzione di pattern, tecnologie o famiglie di diagrammi per raggiungere
  quantità non richieste.
- Ricostruzione inventata di conversazioni, metriche AI o attribuzioni di codice.
- Pubblicazione su tracker esterni, invio della relazione al docente e
  creazione automatica di commit.

## Further Notes

- L'indice dettagliato del 16 novembre 2026 e la matrice precisano la nuova
  richiesta degli autori; resta il capitolo 5 sull'AI già concordato.
- Il glossario e i due ADR esistenti restano fonti per linguaggio e decisioni
  già accettate.
- Le note del corso riportano A.A. 2024/25 e versione 1.1 di gennaio 2025.
  Sono l'unica fonte specifica non marcata come superata individuata nei
  materiali forniti; non sono stati cercati aggiornamenti online.
- I riferimenti di pagina alle note usano il conteggio fisico del PDF, poiché
  la numerazione nei piè di pagina non coincide sempre.
- Le note richiedono consegna in PDF via email almeno tre giorni lavorativi
  prima della discussione; tale invio resta a cura degli autori.
- Le compilazioni e i pacchetti della prima stesura restano risultati storici.
  Il riallineamento richiederà compilazioni reali e controllo visivo dei nuovi
  PDF; la pianificazione non dichiara nuove compilazioni o esportazioni.
- I ticket 09–12 coprono i quattro capitoli; il 13 integra capitolo 5,
  appendici e pacchetto. I prerequisiti tecnici dei capitoli sono indipendenti;
  l'ordine 1–4 resta quello della lavorazione e della revisione progressiva.

## Comments

16 novembre 2026: dopo la revisione della prima stesura, gli autori chiedono
stessa scansione e stessi argomenti della relazione originale, riadattati
all'implementazione corrente, e autorizzano il percorso proposto. Aggiornati
specifica e indice con una matrice completa come primo risultato da rivedere.
La precedente scelta di sintetizzare nel corpo le sei schede principali è
superata da questa richiesta. Le verifiche e i ticket 01–08 attestano le
versioni precedenti e restano intatti; il nuovo riallineamento è da eseguire.

16 novembre 2026, precisazione successiva degli autori: rifare la vecchia
relazione mantenendo tutto e riadattandola esattamente alla nuova
implementazione. Resi obbligatori uso della skill draw.io per ogni
intervento sui diagrammi, basi degli autori e rifacimento/adattamento di
tutte le tavole originali. Il semplice riuso delle tavole della prima
riscrittura non soddisfa da solo questo requisito.

17 novembre 2026: riallineamento documentale 09–13 concluso tecnicamente.
Il ticket 13 integra il capitolo 5, verifica il PDF completo di 151 pagine,
consolida tutte le 102 voci e i materiali originali e ricompila davvero
lo ZIP aggiornato dopo estrazione. [Fonti, corrispondenze, verbale visivo,
review Standards/Spec separate e impronte](../verifiche/ticket-13/README.md).
Le due review finali sono conformi senza rilievi. Lo stato `done` riguarda
il lavoro tecnico; PDF e sorgenti restano proposti agli autori, senza
approvazione presunta, invio, pubblicazione o modifica al software.
