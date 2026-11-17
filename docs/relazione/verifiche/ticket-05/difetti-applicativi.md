# Segnalazioni applicative separate

Solo riscontro statico del 13 novembre 2026; nessuna riproduzione applicativa,
accesso DB o correzione software. Non costituiscono rilievi documentali
aperti: il capitolo descrive questi limiti senza attribuire garanzie ulteriori.

- **Importi zero e controlli email:** confermati i limiti già segnalati nel
  [ticket 04](../ticket-04/difetti-applicativi.md), che rimane intatto.
  `BankAccount.deposit/withdraw` rifiutano zero; `Request` e `Sale` lo
  ammettono. `SaleToDealershipService.approveSaleProposal:126–136` invoca
  deposit anche per stima zero: la proposta è possibile, l'accettazione
  fallisce nel normale percorso di rollback. Le email sono cercate con
  LOWER nel DAO, mentre UNIQUE/trigger confrontano i valori esatti;
  nessuna unicità globale fra ruoli con ID coincidente è dimostrata.
- **Esito economico e aggiornamento UI nello stesso try:**
  `ClientDashboardView.handlePurchaseAction:223–233` e
  `handleCompletePurchaseAction:518–525` chiamano il servizio, poi aggiornano
  saldo/operazioni e mostrano successo nello stesso try. Se una lettura
  successiva al commit fallisce, il catch può mostrare «Operazione Fallita»
  o «Errore» nonostante la scrittura già completata. È una conseguenza del
  flusso statico, non un guasto DB/UI riprodotto. Serve una correzione
  separata se si vuole distinguere l'errore di aggiornamento dall'esito
  del comando; in questo ticket non si modifica l'applicazione.

Altri confini (servizi senza autorizzazione generale del chiamante, DI
parziale, nessuno snapshot comune del listino, cancellazione JDBC non
immediata) sono descritti come proprietà correnti, senza audit esaustivo
né certificazione di sicurezza o concorrenza.
