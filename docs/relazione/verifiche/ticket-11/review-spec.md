# Review Spec — ticket 11

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
