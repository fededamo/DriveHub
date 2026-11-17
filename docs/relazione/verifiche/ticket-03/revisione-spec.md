# Revisione indipendente — Spec

**Esito: CONFORME, nessun rilievo aperto.** Verifica del 12 novembre 2026,
asse Spec, svolta separatamente senza consultare il rapporto Standards.
Riferimento: [specifica operativa](specifica-ticket.md) ricevuta in conversazione
e [specifica generale](../../tracker/spec.md), insieme a istruzioni documentali,
piano, indice, glossario, README, architettura e ADR 0001/0002.

HEAD iniziale/attuale: `61ce18ae1d4e0458b63e970b793b0b3a634a3adf`.
Eseguiti `git diff 61ce18ae1d4e0458b63e970b793b0b3a634a3adf...HEAD`
e `git log 61ce18ae1d4e0458b63e970b793b0b3a634a3adf..HEAD --oneline`:
entrambi vuoti. `docs/relazione/` è non tracciata; la revisione riguarda
quindi i materiali correnti, non il solo diff Git.

Riscontri effettivi:

- Conteggio indipendente dei sorgenti e verifica SHA-256: **65 tipi principali,
  11 package**, inventario completo e immutato. Ogni tipo compare nella propria
  tavola; matrice e omissioni motivate comprendono record annidati, callback,
  implementazioni anonime e membri selezionati. Import ricalcolati coerenti
  con `dipendenze-import.json`.
- XML delle 13 tavole confrontati con dichiarazioni, campi e collaborazioni
  pertinenti nei sorgenti. Corretti generalizzazione/realizzazione, riferimenti
  per ID, conto opzionale, molteplicità al bersaglio e assenza di composizioni.
  Confermati navigator anonimo creato da App, connessione JDBC condivisa,
  Observer sincrono prima del commit e confine password distinto
  dall'autorizzazione generale. Nessun conflitto con gli ADR.
- Letti LaTeX, integrazioni, matrice, relazioni, log finale e
  [controlli riusciti](controlli-documento.txt). Aperto con PyMuPDF il PDF finale
  e osservate tutte le nuove tavole nelle pagine fisiche 26 e 51–62,
  le riprese 28/30, indice, didascalie e legenda B: intere e leggibili.
  Verificati indipendentemente **63 pagine, 95 link interni validi,
  45 font incorporati**, assenza di `??`, ordine 2.6 e appendice B.
- Ricalcolate tutte le **2046 impronte protette**: nessuna modifica.
  Testo del capitolo 1 identico; confronto delle 32 pagine di 2.1–2.4/A
  identico, esclusi avviso di stato e numeri. Confermati provvisori
  2.7–2.8, 3–5 e C; nessuna approvazione degli autori attribuita.

PDF esaminato: SHA-256
`568d76ead85fa06d6d3942c25ab0505722e10b1b4365aa5455fbbfa1a18e2d95`.
Nessun requisito mancante/parziale, comportamento documentale scorretto
o ampliamento del perimetro rilevato. 2.5–2.6/B sono proponibili agli autori.

Limiti: revisione statica e documentale; compilazione attestata dai log,
non rieseguita dal revisore. Nessun test applicativo, accesso DB/UI, commit,
certificazione generale del software o verifica di una seconda distribuzione LaTeX.
