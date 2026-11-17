# Tracker locale della relazione

Le specifiche e i ticket della riscrittura risiedono in Markdown sotto
`docs/relazione/tracker/`, con percorsi relativi alla radice del repository.

## Convenzioni

- Specifica: `docs/relazione/tracker/spec.md`.
- Ticket: `docs/relazione/tracker/issues/NN-slug.md`, uno per file, numerati da
  `01` nell'ordine delle dipendenze.
- Ogni ticket riporta `Status:`, criteri di accettazione e `Blocked by:` con
  numeri e titoli dei ticket bloccanti, oppure `None`.
- I ruoli triage sono quelli definiti in [triage-labels](triage-labels.md).
  `Status: done` indica un ticket concluso con criteri verificati.
- Un ticket può iniziare quando tutti i suoi bloccanti sono `done`.
- Commenti e storia delle decisioni si aggiungono in fondo, sotto `## Comments`.

## Operazioni delle skill

- Pubblicare una specifica o un ticket significa creare il relativo file locale.
- Recuperare un ticket significa leggere il file indicato o individuato dal numero.
- Il tracker si applica alla riscrittura della relazione; gli altri registri ed
  evidenze esistenti conservano il proprio ruolo storico.
