# Review indipendente — Spec, sessione16

Esito: **PASS, 0 rilievi**. Revisione del solo `review.patch` congelato
(SHA256 `5302f7b4445f7e12af229b056d9a3cf46a8b9c5172f00c74f0d314af3035ca0b`),
confrontato con la fotografia locale iniziale, senza attribuire alla sessione
le modifiche01–15. Verificata la corrispondenza dei15file a
`review-files.sha256.json`. Nessun commit della sessione.

- **Requisiti mancanti/parziali:** nessuno rilevato. Ticket16, righe18–19 e23,
  e AC15 (`spec.md:164`) trovano riscontro in installazione, testing e prove:
  Java21/Maven/PG16, configurazione realmente letta, inizializzazione,
  demo facoltativa e reset distruttivo separati dagli avvii ordinari.
  Ticket16, righe20–22, AC18 (`spec.md:167`) e R09 sono coperti da architettura:
  factory statiche/GoF/DAO/DI, SOLID/DDD, dipendenze/flusso dati e accessi
  globali residui, con esempi coerenti con AppContext, servizi, dominio,
  DatabaseManager e publisher esaminati. Prezzi e compromessi rispettano
  glossario e ADR0001.
- **Scope creep:** nessuno rilevato. Il probe e gli script raccolgono prove
  della configurazione/demo e dell'isolamento; non verificano la forma del
  documento. Nessun refactor produttivo o intervento sui diagrammi17.
- **Implementazioni errate:** nessuna rilevata nel diff. Controllati
  indipendentemente hash dei sorgenti rispetto a baseline/copia, input
  invariati build→suite,15ticket chiusi, XML47/47 e394/394 senza errori/salti,
  log build target21, inizializzazione vuota/demo, tre login e due finestre
  JavaFX con PID distinti. Impronte delle13tabelle/7sequenze applicative
  identiche prima/dopo suite; sentinella letta READ ONLY. Nessun uso del
  risultato storico15 per attestare16.

Lo stato in-progress e gli aggiornamenti finali ancora pendenti sono coerenti
con il congelamento della review; R09 resta parziale per17.

Non rieseguite build/suite: verificati artefatti e sorgenti delle nuove prove16.
Relazione esclusa integralmente; limiti Linux/Xvfb e mancata prova di tutti i
flussi GUI sono dichiarati, senza nuove garanzie globali.
