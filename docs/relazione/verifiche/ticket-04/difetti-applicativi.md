# Segnalazioni separate — nessuna correzione applicativa

Riscontro statico del 12 novembre 2026. Nessuna riproduzione su database,
nessuna suite applicativa eseguita. Queste osservazioni non sono rilievi
documentali irrisolti: il testo e le tavole ne riportano il limite corrente.

1. **Movimenti di importo zero.** `Request.create` e i CHECK SQL ammettono
   stima/acconto/importi zero; `BankAccount.deposit` e `withdraw` rifiutano
   `amount <= 0`. I tre servizi economici invocano questi metodi senza
   saltare i movimenti zero. Di conseguenza una cessione con stima zero
   può essere proposta ma l'accettazione fallisce e va in rollback; anche
   addebiti zero o un residuo zero hanno questo limite. Le schede storiche
   intatte descrivono l'intento con valori non negativi; il ticket04 rende
   esplicita questa discrepanza, senza modificarle. Fonti:
   `domain/operation/Request.java:38–86`, `domain/user/BankAccount.java`
   (`deposit`, `withdraw`), `PurchaseService`, `RentalService`,
   `SaleToDealershipService.approveSaleProposal`, CHECK request/rent/sale.
2. **Identità: controlli email non equivalenti a unicità globale.**
   `schema.sql:201–215` confronta email esatte e ignora la riga dell'altro
   ruolo con lo stesso id. PK e UNIQUE sono locali; id espliciti sono
   consentiti. Quindi il DDL non esclude ogni duplicazione fra ruoli con
   id coincidente, e i trigger basati su SELECT non dimostrano l'esclusione
   di inserimenti concorrenti. Inoltre `PostgresUserDao.findByEmail`
   confronta LOWER(email), diversamente dai vincoli UNIQUE e dai trigger.
   È un limite statico, non una gara concorrente riprodotta in questo ticket.

Gli altri limiti descritti (assenza di snapshot comune del listino,
scadenze all'accesso, servizi senza controllo generale del chiamante,
assenza di PK globale fra ruoli) sono confini correnti già riscontrabili,
non nuove garanzie o correzioni. Non è stata condotta una ricerca esaustiva
di difetti né una certificazione di sicurezza/concorrenza.
