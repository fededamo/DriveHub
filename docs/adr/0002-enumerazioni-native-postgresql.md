# Enumerazioni native PostgreSQL per i domini a valori chiusi

Decisione accettata dall'utente il 9 novembre 2026: uniformare la rappresentazione
dei quattro domini enumerati Java usando tipi enumerati nativi anche in
PostgreSQL. `vehicle.state` e `request.type` conservano rispettivamente
`vehicle_state` e `request_type`; `vehicle.price_mode` e `sale.type` passeranno
da `VARCHAR` con `CHECK` ai tipi dedicati `price_mode` e `sale_type`.

La motivazione è la coerenza della rappresentazione e della tipizzazione nel
database. L'alternativa precedente con `VARCHAR` e `CHECK` resta valida per
limitare i valori ammessi; viene superata per uniformità, non per un errore
funzionale dimostrato. Gli enum nativi rendono più impegnativa la futura
rimozione di un valore e richiedono query JDBC coerenti con il tipo SQL.

Valori e significati restano quelli correnti: `AUTOMATIC` e `MANUAL` per la
scelta della base del prezzo; `SALE`, `PURCHASE` e `PENALTY` per il registro,
dal punto di vista della concessionaria. La decisione non cambia formule,
importi, stati o flussi applicativi. Lo sviluppo è sulla branch `dev`.

L'utente ha confermato anche l'aggiornamento del database locale `drivehub-db`,
con applicazione chiusa, backup verificato e prova preventiva su copia.
La conversione dovrà preservare tutte le righe, gli identificativi, le sequenze,
le associazioni e i valori applicativi, compresi prezzi, importi e saldi.
Non verranno eseguiti movimenti economici né ricaricati schema o dati demo
sull'archivio da conservare. Una conversione fallita dovrà essere annullata
integralmente dalla transazione.

L'utente ha scelto di consegnare soltanto lo stato corrente: lo script di
conversione sarà temporaneo e non rimarrà nel repository dopo l'aggiornamento.
Resteranno schema, codice JDBC, test e documentazione coerenti con i quattro
enum nativi, insieme alle evidenze delle verifiche; i backup rimarranno privati
fuori dal repository. Non è richiesto un sistema di migrazioni automatiche.

Le verifiche useranno i confini PostgreSQL già approvati nel progetto:
contratto SQL e valori ammessi, persistenza tramite i DAO, conservazione dei
dati e rollback della conversione. Il ritorno alla branch `main` non annulla
le modifiche al database: per tornare alla versione precedente occorre
ripristinare anche il backup corrispondente.

L'utente ha confermato il perimetro complessivo al termine dell'intervista.
L'implementazione e l'aggiornamento del database locale sono autorizzati
alle condizioni descritte sopra.

Decisione attuata sulla branch `dev`: [verifiche, aggiornamento locale e limiti](../evidence/postgres-enums-2026-11-09/verification.md).
