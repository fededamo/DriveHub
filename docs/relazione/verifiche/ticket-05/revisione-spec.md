# Revisione indipendente Spec — ticket 05

**13 novembre 2026 — CONFORME: un rilievo minore risolto, zero aperti.**
Revisione in contesto separato, senza leggere il rapporto Standards corrente.
Riferimento: [specifica operativa](specifica-ticket.md), punti 1–11, e
[specifica generale](../../tracker/spec.md); letti istruzioni richiamate,
piano, indice, glossario, README, architettura, ADR 0001–0002 e riscontri 03–04.

Punto fisso e HEAD: `61ce18ae1d4e0458b63e970b793b0b3a634a3adf`, risolto con
`git rev-parse`. Eseguiti i comandi fissati
`git diff 61ce18ae1d4e0458b63e970b793b0b3a634a3adf...HEAD` e
`git log 61ce18ae1d4e0458b63e970b793b0b3a634a3adf..HEAD --oneline`:
vuoti, senza nuovi commit. La relazione era già non tracciata: esaminati
delta documentale, copie `prima/` e nuovi materiali 05; il diff Git vuoto
non prova assenza di lavoro.

**Rilievo risolto — punto 3**, «Riscontrare sui sorgenti correnti le affermazioni»:
D1 della matrice chiamava privata la factory `Vehicle.create`, che è pubblica;
è privato il costruttore. Segnalato al principale, corretto in «costruttore
privato e factory pubblica» e ricontrollato. LaTeX e PDF erano già corretti.

Non emergono requisiti mancanti/parziali, implementazioni documentali errate
residue o ampliamenti non richiesti:

- Punti 2–6: cinque titoli e ordine conservati. Confrontati direttamente
  Java/DDL e matrice: invarianti, gerarchie, mutabilità/ID, prezzi/accordo,
  economia/identità/conto, connessioni effettivamente condivise o autonome,
  lock, rollback/errori e Observer sincrono; navigazione, Task/AsyncLoad,
  refresh/lifecycle, JDBC/null e quattro enum. Classificazioni GoF,
  factory statiche, DAO, DI, Singleton e Visitor motivate con limiti.
- Punti 3/7/8: verificati autonomamente 55 hash delle fonti e 13 estratti
  esatti Java/LaTeX, da 3–19 righe, spiegati nel contesto. Diagrammi riusati;
  nessuna duplicazione superflua del capitolo 2 individuata.
- Punti 10–11: ricalcolati 2.199 hash protetti, tutti intatti; confrontate
  77 pagine pregresse di capitoli 1–2/A–B, equivalenti esclusi piè di pagina,
  spazi e sillabazione. 4–5/C restano provvisori; nessuna approvazione presunta.

PDF aperto autonomamente con PyMuPDF: **93 pagine, 133 link interni risolti,
81 font incorporati**, identico a `build/main.pdf`, senza `??`.
Osservate con view_image tutte le pagine del capitolo 3 (54–63), indice/frontespizio
1–3 e confini 53/64/65/93: frammenti, didascalie e riferimenti leggibili.
SHA-256: `2713ae306c1bc3f748ecddbfbdc0771e0b401119a8cd41af005ebd7856b742bb`.

Limiti: verifica statica/documentale; letti log, senza ricompilare o osservare
manualmente tutte le 93 pagine. Nessun Java, test applicativo, accesso DB/UI
o commit; nessuna certificazione generale del software o approvazione degli autori.
