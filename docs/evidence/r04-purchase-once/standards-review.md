# Ticket 07 — revisione indipendente Standards

Perimetro: esclusivamente `session.patch`, rispetto allo snapshot dedicato
`/tmp/drivehub-ticket07-baseline/repo` (HEAD
`c2a81122ee6be44760ded4160fde229bd33e837c` e modifiche locali pregresse).
Il lavoro precedente, incluso ticket 06, non è stato imputato al ticket 07.
`Relazione_SWE/**` esclusa da letture, ricerche e verifiche.

## Violazioni documentate

Nessuna rilevata nel diff. Fonti consultate: `AGENTS.md`, registro esterno,
`GLOSSARY.md`, ADR 0001, `docs/testing.md`, `docs/agents/issue-tracker.md`
e `docs/agents/domain.md`, oltre alle istruzioni della sessione.

Il totale concordato rimane la fonte del saldo, secondo ADR 0001. I quattro
percorsi modificati rileggono la richiesta nella transazione; i movimenti
mantengono `findByClientIdForUpdate` sulla stessa connessione, come richiesto
da `docs/testing.md`. Verificati l'unico implementatore di `RequestDao`,
i chiamanti di aggiornamento/eliminazione richiesta e i contratti DAO usati
per transizione e inserimento vendita. Nessuna nuova dipendenza, framework,
regola economica o modifica UI nel diff.

## Smell euristici

Un suggerimento a bassa priorità, non una violazione bloccante:

- **Possibile Duplicated Code** — hunk del nuovo
  `PurchaseConcurrencyIntegrationTest.java`, righe 291–355: `race`, `outcome`
  e `awaitWaiters` riproducono la struttura già presente in
  `BalanceConcurrencyIntegrationTest.java:302–358`, inclusi
  `workers.submit(outcome(first))`, attesa dei backend, rilascio del gate e
  cleanup. Eventuali correzioni del coordinamento richiedono interventi in
  entrambe le suite. Valutare un piccolo helper di test condiviso per il solo
  protocollo comune, conservando query e asserzioni specifiche. Le differenze
  attuali sui gate e sugli esiti possono comunque giustificare la separazione;
  il rilievo non richiede un refactor per chiudere questo ticket.

Revisione statica: nessun test eseguito, nessun commit o staging. La suite DB
in corso resta responsabilità della verifica principale; questa revisione
non ne certifica l'esito.
