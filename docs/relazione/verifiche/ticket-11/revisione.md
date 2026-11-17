# Review finale indipendente — ticket 11

17 novembre 2026. Skill `mattpocock-skills:code-review`, due revisori con contesti iniziali nuovi e separati. Punto fisso: working tree iniziale in baseline.json e baseline-editoriale.tar.gz, inclusi non tracciati, PDF e storici. HEAD iniziale `61ce18ae1d4e0458b63e970b793b0b3a634a3adf`; diff/log Git vuoti, confronto effettivo tramite delta-documentale.diff e delta-files.json. I tentativi del 16 novembre interrotti per crediti del workspace non sono esiti di review.

**Esito conclusivo: Standards conforme, zero aperti; Spec PASS, zero aperti.** Due correzioni documentali uniche: descrizione di AbstractUser astratta e recupero della spiegazione SQLSTATE23503. La Spec conferma anche la prima, attribuendone la scoperta alla segnalazione del coordinamento. I due assi restano distinti e non vengono riclassificati in un unico rapporto.

PDF finale SHA-256 `3e08ead9770f414bdf7ed988725bb28d0eae813a79a50491225cdb2572490195`; estratto `acb349fa9682f8b326cfff2a0060867585097938d599c7d4a6a322f920f0432d`. Dopo le correzioni: tre compilazioni-review riuscite e checker documentale PASS. I revisori hanno verificato gli artefatti, senza rieseguire compilazione o prove applicative. I rapporti sotto sono riportati integralmente e restano disponibili come file autonomi.

## Standards

### Prima review

[Rapporto autonomo](review-standards.md).


17 novembre 2026. Prima review sul candidato PDF SHA-256
`b87ffa38ce1ffe7a6569ffb7d8e48a10497582e5a617136e3715c096eacd8236`.
Punto fisso: working tree iniziale conservato in `baseline.json` e
`baseline-editoriale.tar.gz`; HEAD `61ce18ae1d4e0458b63e970b793b0b3a634a3adf`.
Diff e log Git vuoti riscontrati; esaminati il delta documentale effettivo
e i nuovi materiali 11. Nessun rapporto dell'asse Spec consultato.

## Violazioni documentate

- **Media — affermazione tecnica errata**, `capitoli/3_implementazione.tex:40`:
  «AbstractUser, nonostante il nome, non è dichiarata astratta» contraddice
  `src/main/java/it/unifi/drivehub/domain/user/AbstractUser.java:11`,
  `public abstract class AbstractUser implements User`.
  Viola `docs/agents/domain.md`, regola «Riscontrare le affermazioni
  sull'implementazione nel codice e nello schema correnti», e i criteri di
  lavoro di `piano.md`. Correggere la descrizione della classe e rigenerare
  il PDF. Rilievo comunicato e accolto; la correzione annunciata richiede
  ancora il ricontrollo del candidato finale.

## Euristiche Fowler

Nessun nuovo smell rilevante nel delta o nello script documentale:
responsabilità unica, helper limitati e controlli coerenti con il compito.
I listati applicativi autentici sono materiale citato; questa review non
propone refactor del software. Le euristiche non sono violazioni normative.

## Verifiche effettive e limiti

Letti istruzioni editoriali, piano, indice, matrice, glossario, README,
istruzioni di consegna e due ADR; nessun CONTRIBUTING/CODING_STANDARDS
individuato nel contesto fornito. Letti capitolo, delta, checker, verbali,
log e sorgenti tecnici mirati.

Controlli indipendenti in sola lettura: 3.277 file iniziali presenti, sole
due modifiche previste; 223 copie archiviate e 72 fonti autentiche; diff
esatto; 27 estratti corrispondenti a sorgenti e PDF; 13 precedenti conservati
(11 identici, due completati dalla graffa). PDF 143 pagine, estratto 23;
78 pagine dei capitoli 1/2 identiche anche nei pixel, altre 38 invariate nel
testo salvo numero finale; 79 rinvii, 224 link e 106 font verificati.
Osservati nove provini, corrispondenti ai 35 rendering del PDF.

Tre log finali letti: cinque messaggi TeX preesistenti in C, senza difetti
visibili o nuovi box segnalati. Nessuna ricompilazione, esecuzione del
checker scrivente, prova applicativa, DB o UI; nessuna approvazione degli
autori presunta. Esito iniziale: **un rilievo Standards aperto**.

### Ricontrollo delle correzioni

[Rapporto autonomo](review-standards-chiusura.md).


17 novembre 2026. Candidato PDF SHA-256
`3e08ead9770f414bdf7ed988725bb28d0eae813a79a50491225cdb2572490195`;
estratto SHA-256
`acb349fa9682f8b326cfff2a0060867585097938d599c7d4a6a322f920f0432d`.
Punto fisso invariato: working tree iniziale del ticket 11, inclusi i
non tracciati; confronto effettivo con baseline, archivio e delta.
Il rapporto iniziale resta conservato senza modifiche.

## Rilievo iniziale e correzione

**Chiuso** il rilievo medio sulla descrizione di AbstractUser.
`capitoli/3_implementazione.tex:40` dichiara ora classe astratta e
costruttore protetto, coerentemente con
`src/main/java/it/unifi/drivehub/domain/user/AbstractUser.java:11,21`.
La frase è presente anche nella pagina fisica 83 del PDF.
È così rispettata la regola di `docs/agents/domain.md:17` sul riscontro
delle affermazioni nel codice corrente.

Verificata anche la precisazione in `3_implementazione.tex:1003–1006`:
SQLSTATE 23503 sulla cancellazione diventa IllegalStateException con causa;
gli altri errori mantengono la causa SQL, come in
`PostgresUserDao.java:236–240`. La descrizione non attribuisce una tassonomia
uniforme assente. Pagina fisica 99 riaperta e leggibile.

## Verifiche del candidato corretto

Controlli indipendenti in sola lettura confermano hash di candidato,
sorgente ed estratto, delta esatto, 3.277 file iniziali presenti e sole
modifiche a capitolo 3/PDF. Riconfrontati i 27 estratti con sorgenti e PDF;
estratto di 23 pagine coerente con le pagine 83–105 del documento di 143.

Confrontate tutte le 143 pagine con la copia del candidato precedente
identificata dall'impronta del rapporto iniziale: cambiano testo e pixel
soltanto delle pagine 83 e 99; le altre 141 sono identiche.
Nuovamente confermate 78 pagine dei capitoli 1/2 identiche alla baseline
anche nei pixel e 38 pagine successive invariate nel testo salvo numero.
I rendering riaperti corrispondono al nuovo PDF.

Letti i tre log `compilazione-review-*`: documento di 143 pagine, cinque
messaggi TeX preesistenti in C, nessun nuovo box o rinvio irrisolto.
Nessuna nuova violazione documentata o euristica Fowler rilevante.

**Esito Standards: conforme, zero rilievi aperti.** Nessuna ricompilazione,
esecuzione del checker scrivente, prova applicativa, accesso DB/UI o modifica
agli altri materiali effettuata dal reviewer. Restano i limiti applicativi
dichiarati; questo esito non presume l'approvazione degli autori.

## Spec

### Prima review

[Rapporto autonomo](review-spec.md).


17 novembre 2026. Review indipendente dalla Standards, secondo
`mattpocock-skills:code-review`. Punto fisso: working tree iniziale di
`baseline.json` e archivio editoriale; HEAD
`61ce18ae1d4e0458b63e970b793b0b3a634a3adf`. Diff a tre punti e log Git
verificati vuoti: il confronto effettivo usa baseline, delta documentale
e nuove evidenze. PDF esaminato: SHA-256
`b87ffa38ce1ffe7a6569ffb7d8e48a10497582e5a617136e3715c096eacd8236`.

Due rilievi documentali; verifica delle correzioni nel nuovo PDF pendente.

- **Conservazione parziale del testo valido.** Il ticket, righe 12–13,
  richiede «Riorganizzare il testo valido e recuperare spiegazioni».
  Rispetto al capitolo iniziale nel tar mancava la spiegazione specifica
  della traduzione SQLSTATE 23503 durante la cancellazione dell'identità
  e dell'assenza di una tassonomia uniforme degli errori DAO. La frase
  generica sulla conservazione della causa non recuperava questa funzione.
  Il comportamento è confermato da
  `src/main/java/it/unifi/drivehub/dao/postgres/PostgresUserDao.java:236`.
  Ripristino ora presente in `docs/relazione/capitoli/3_implementazione.tex:1003`;
  il PDF esaminato precede questa correzione.
- **Descrizione errata della gerarchia.** Dopo la segnalazione del
  coordinamento, verificato direttamente `domain/user/AbstractUser.java:11`:
  la classe è `abstract`, con costruttore `protected` alla riga 21.
  Il candidato affermava invece «non è dichiarata astratta» nel capitolo
  corrente, riga 40. Contraddice la richiesta del ticket, righe 12–13,
  di spiegazioni «verificate sul codice corrente». Correzione in corso.

Non emergono altri requisiti mancanti, errori semantici o ampliamenti
estranei al ticket. Letti gli otto originali attivi, il capitolo iniziale
archiviato e quello corrente, confrontandoli con ticket, specifica,
indice, matrice, piano, glossario e ADR. Le 35 voci conservano progressione
e funzione; cinque gruppi dominio, otto servizi e quattro flussi spiegano
controlli, alternative, scritture ed esiti. Verificati sui sorgenti ordine
dei passi, destinatari, connessioni, callback, formule, accordo persistito,
Observer, UI/AsyncLoad, mapping, vincoli e sei temi dei pattern.

Ricalcolate le 3.277 impronte iniziali: cambiano soltanto capitolo 3 e PDF;
verificate le 223 copie nel tar. Tutti i 13 estratti precedenti sono
conservati; i 27 correnti coincidono con sorgenti, intervalli e LaTeX.
Riscontrate le 14 corrispondenze motivate. Osservati i nove provini:
indice 2–4, tutte le pagine 83–105 e C 135–143; formule, tabelle e
listati leggibili. Esaminati log di compilazione ed evidenze, con i
cinque messaggi TeX storici dichiarati.

Non eseguiti compilazione, checker scrivente o prove applicative/DB/UI.
Serve ricontrollare il PDF rigenerato; nessuna approvazione degli autori
o chiusura del ticket è attribuita da questa review.

### Ricontrollo delle correzioni

[Rapporto autonomo](review-spec-chiusura.md).


17 novembre 2026. **PASS: zero rilievi residui nel candidato esaminato.**
Rapporto separato dalla [prima review](review-spec.md), senza lettura del
rapporto Standards. Resta il punto fisso working tree iniziale, non il
solo diff Git vuoto.

Impronte SHA-256 ricalcolate e coincidenti con `candidato-review.json`:

- PDF completo: `3e08ead9770f414bdf7ed988725bb28d0eae813a79a50491225cdb2572490195`.
- Capitolo sorgente: `6b6a947f0aa4c2581c44aa44baacedea8b52f8321c030f54c28fedeecd072302`.
- Estratto: `acb349fa9682f8b326cfff2a0060867585097938d599c7d4a6a322f920f0432d`.

Entrambi i rilievi sono risolti nel sorgente e nel PDF. A pagina fisica
83, `AbstractUser` è descritta come astratta con costruttore protetto,
in accordo con `AbstractUser.java:11` e `:21`. A pagina 99 è recuperata
la spiegazione della traduzione SQLSTATE 23503, della conservazione della
causa SQL e del limite della tassonomia DAO, confermata da
`PostgresUserDao.java:236`. Aperte entrambe le pagine rigenerate: testo
completo e leggibile, senza tagli o sovrapposizioni.

Resta soddisfatta la richiesta del ticket di conservare contenuti e
funzione espositiva: 35 voci, cinque gruppi dominio, otto servizi,
quattro flussi completi, formule ed esempi, tre argomenti UI, cinque DAO
e sei temi dei pattern. Le 14 corrispondenze originali hanno esempi
correnti o adattamenti motivati; nessuna Strategy, FXML o UnitOfWork
inventata. Il riscontro semantico della prima review resta valido.

Ricontrollati in memoria tutti i 27 estratti rispetto a intervalli dei
sorgenti e testo PDF; le 23 pagine dell'estratto coincidono con 83–105
del completo. Ricalcolati i sette risultati monetari, inclusi 25.333,87
e 57,05 euro. Verificati 79 rinvii risolti e assenza di `??`.
Letti i tre log `compilazione-review-*`: 143 pagine, soli cinque messaggi
TeX storici in C; nessun Overfull/Underfull o rinvio indefinito finale.

Ricontrollate 3.277 impronte: cambiano soltanto capitolo 3 e PDF.
Confrontate direttamente col PDF archiviato le 78 pagine dei capitoli
1/2, identiche per testo e pixel, e le 38 successive di capitoli 4/5 e
appendici, identiche nel testo tolto il numero finale. Nessuna anticipazione
dei ticket 12/13 o approvazione degli autori.

Non eseguiti compilazione, checker scrivente, test applicativi, UI o DB.
Stati e riepiloghi amministrativi finali restano da aggiornare; il PASS
riguarda il candidato identificato sopra e non chiude il ticket.

## Chiusura e limiti

Gli stati documentali vengono aggiornati dopo i due PASS. Nessuna nuova modifica narrativa al capitolo o al PDF dopo i ricontrolli. I cinque messaggi TeX preesistenti in C restano dichiarati e osservati; nessun test applicativo, build Java, avvio UI, DB, commit, invio o pubblicazione. La revisione degli autori e i ticket 12/13 restano distinti.

Rilievi per asse: **Standards: uno iniziale risolto, zero aperti. Spec: due iniziali risolti (uno condiviso), zero aperti.** Non si attribuisce approvazione degli autori.

## Controllo finale degli stati

Confermati dopo l'aggiornamento del ticket e dei riepiloghi:
[controllo amministrativo Standards](review-standards-stati.md) e
[controllo amministrativo Spec](review-spec-stati.md), entrambi senza
rilievi aperti. Ticket11 done con nove criteri verificati; ticket12/13
immutati, tre impronte degli artefatti identiche a quelle dei ricontrolli.
Le nuove impronte dei due rapporti sono incluse nel manifest conclusivo.

Le review tecniche e i due ricontrolli erano conclusi prima di comporre
questo aggregato. Durante il solo controllo amministrativo, il revisore
Standards ha letto anche l'aggregato, segnalando la circostanza nel proprio
rapporto. Tale fase non è presentata come una nuova review tecnica a
contesto separato; i precedenti quattro rapporti indipendenti restano
immutati. La Spec amministrativa non ha letto l'altro asse.
