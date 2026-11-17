# Segnalazioni e limiti separati — ticket documentale 06

Riscontro statico del 13 novembre 2026, nessuna riproduzione, compilazione Java,
correzione software, modifica ai test o accesso DB.

I limiti applicativi già registrati restano nei rapporti immutati
[04](../ticket-04/difetti-applicativi.md) e
[05](../ticket-05/difetti-applicativi.md): movimenti zero, unicità email,
errore di refresh dopo commit. Nessun nuovo difetto produttivo dimostrato.
Il test corrente `SaleOutcomeIntegrationTest.zeroPaymentCannotConsumeTheProposalOrWriteAnAcquisition`
asserisce il rifiuto della cessione a zero e lo snapshot invariato: dimostra
quel comportamento, non che l'intento di accettare una stima non negativa
sia interamente soddisfatto. La discrepanza è già nota, non una nuova scoperta
funzionale né una sua correzione.

**Nuovo riscontro sugli strumenti storici: harness cessione incompatibile.**
`docs/evidence/r05-sale-outcome/SaleOutcomeUiCheck.java:47,144,150` usa
`ACQUISITION` e l'importo `-16000.00`; il `SaleType` corrente contiene
solo SALE/PURCHASE/PENALTY e gli importi sono non negativi. L'accesso a
`SaleType.ACQUISITION` non è compatibile con l'enum corrente. Il launcher R11
compila anche questo file quando seleziona altri harness: le sue sole patch
conservate non aggiornano i tipi. Quindi quel runner storico non va proposto
come comando portabile sul checkout corrente. È un limite di riproducibilità
emerso dalla lettura, non un tentativo di compilazione eseguito qui.

L'harness rimane intatto come evidenza della sua versione. Un futuro lavoro
applicativo/di verifica potrà aggiornarne una copia con nuove prove e manifest;
nessun aggiornamento è autorizzato in questo ticket. Per i soli controlli di
tabelle/modelli la procedura documentata usa invece il launcher R08, che compila
PriceUiCheck/ReactiveTables/SlowModels/TableLifecycle e non SaleOutcome.

Il distacco scena/logout con query pendente non ha una specifica asserzione
negli harness esaminati: lacuna della prova, non un nuovo difetto runtime.
Nessun audit esaustivo del prodotto.
