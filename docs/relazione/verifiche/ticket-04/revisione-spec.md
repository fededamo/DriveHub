# Revisione indipendente Spec — ticket 04

**13 novembre 2026 — CONFORME: nessun rilievo Spec residuo.**
Revisione conclusa in contesto separato, senza consultare il rapporto
Standards del ticket 04. Specifica: [ticket operativo](specifica-ticket.md),
requisiti 1–11 e perimetro, e [specifica generale](../../tracker/spec.md).

Riferimento fisso e HEAD: `61ce18ae1d4e0458b63e970b793b0b3a634a3adf`.
Verificati `git diff 61ce18ae1d4e0458b63e970b793b0b3a634a3adf...HEAD`
e `git log 61ce18ae1d4e0458b63e970b793b0b3a634a3adf..HEAD --oneline`:
vuoti, senza nuovi commit. Essendo la documentazione già non tracciata,
esaminato il delta reale: nuove 2.7–2.8, tredici draw.io/esportazioni,
evidenze 04 e integrazione nei cinque file necessari. Confrontate le copie
`prima/` e ricontrollate indipendentemente tutte le 2.116 impronte protette:
nessuna alterazione.

Non emergono requisiti mancanti, parziali o realizzati erroneamente,
né ampliamenti del perimetro. Riscontrati direttamente Java e DDL:

- Requisiti 2–5: tre flussi attuali, handler, ID/oggetti, accordo e importi,
  richieste/registro, connessioni autonome rispetto a quelle riusate,
  lock effettivi, commit/rollback/errori, Observer sincrono e controllo
  scadenze all'apertura. Nessuna garanzia concorrente aggiunta.
- Requisiti 6–7: tredici tabelle, quindici FK, quattro enum; attributi,
  PK/candidate, nullabilità, cardinalità, DELETE/UPDATE, UNIQUE/CHECK/EXCLUDE,
  vista/identità e limiti applicativi coerenti con schema e ADR 0001/0002.
- Requisiti 8–10: sorgenti modificabili, matrici e omissioni motivate;
  conservazione precedente e stato editoriale coerenti.

PDF definitivo aperto indipendentemente: **84 pagine, 108 collegamenti
interni risolti, 71 font incorporati**, identico a `build/main.pdf`.
SHA-256: `0b25ad25962fb0886e408ac23d4a97a68eafe87e8fd8533b5647897fc7441a37`.
Osservate tutte le tredici tavole alle pagine fisiche
33, 35, 36, 38, 40, 41, 43, 44, 46, 47, 49–51, indice 2–3 e provvisori
54–56/84. Ricontrollate le correzioni e, dopo l'ultima compilazione,
le virgolette alle pagine 32/52. Letti controlli e verbale visivo finali:
testi, frecce, simboli, riferimenti e cardinalità risultano leggibili.

**Esito Spec: 0 rilievi aperti; 2.7–2.8 e capitolo 2 proponibili agli autori.**
I tentativi interrotti non costituiscono revisioni riuscite.
Nessuna suite applicativa, query DB o acquisizione UI eseguita; verifica
statica e documentale, senza certificazione generale del software.
Capitoli 3–5 e C restano provvisori; approvazione umana ancora necessaria.
