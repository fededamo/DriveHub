# Ticket 04 / R02 — saldi concorrenti

Verifica del 31 ottobre 2026. Prerequisito 01 e precedenti 02/03 controllati
`State: closed`, con tutti i criteri soddisfatti. Nessun commit. Relazione_SWE
esclusa da ogni operazione. Revisione indipendente finale completata: Standards 0 rilievi, Spec 0 rilievi.

## Perimetro e protocollo

La lettura seguita da UPDATE assoluta perdeva movimenti: due richieste riuscite
da 100 e 200 euro, partendo da 1000, lasciavano 800 invece di 700.
Il DAO ora espone `findByClientIdForUpdate`: SELECT FOR UPDATE sulla connessione
ricevuta, con rifiuto dell'autocommit. La validazione e il movimento di dominio,
UPDATE e altre scritture restano nella transazione esistente fino al commit o
rollback. Nessuna transazione aggiuntiva; letture UI/login senza lock.

Il precedente overload con fallback a una connessione esterna è rimosso; unico
implementatore PostgreSQL e tutti gli otto chiamanti aggiornati. I rimborsi di
noleggi ordinano i conti per ID cliente crescente prima di leggerli/modificarli.
Un conto mancante ora solleva errore: rimborsi e cancellazioni già eseguiti sono
annullati dalla transazione, senza successo parziale.

| Servizio pubblico | Movimento | Chiamanti applicativi controllati |
|---|---|---|
| `AuthService.topUpClientBalance` | Accredito ricarica | ClientDashboardView, AccountManagementDialog |
| `PurchaseService.initiatePurchase` | Addebito acconto | ClientDashboardView |
| `PurchaseService.completePurchase` | Addebito saldo | ClientDashboardView |
| `PurchaseService.rejectPurchase` | Rimborso acconto | SalesmanDashboardView |
| `PurchaseService.checkAndHandleExpiredRequests` | Rimborso meno penale | ClientDashboardView, FullWorkflowIntegrationTest |
| `SaleToDealershipService.approveSaleProposal` | Accredito cessione | SalesmanDashboardView |
| `RentalService.bookRental` | Addebito noleggio | ClientDashboardView |
| `RentalService.changeVehicleRentalState` | Rimborsi multipli | SalesmanDashboardView |

Controllati anche `BankAccount.deposit/withdraw`, tutti gli usi di `updateBalance`,
registrazione con conto iniziale, login/letture saldo, DAO e composizione AppContext.
Interfacce pubbliche dei servizi, messaggi degli altri flussi, schema, demo,
fixture precedenti e dipendenze invariati. Solo il DAO cambia il contratto di
lettura per i movimenti. Nessun altro difetto corretto.

## Riproduzione e prove PostgreSQL

`BalanceConcurrencyIntegrationTest` usa servizi pubblici e verifica saldo e
operazioni tramite gli stessi servizi. Preparazione fixture e osservazione dei
lock usano SQL sul solo database dedicato. Una connessione gate possiede il conto;
si avvia la prima operazione, si attende il backend in attesa, poi la seconda.
`pg_stat_activity`, PID distinti e `pg_blocking_pids` attestano la competizione
prima del rilascio. Il vecchio codice si ferma sull'UPDATE dopo aver letto il saldo;
quello corretto sulla SELECT FOR UPDATE prima di leggerlo. Nessun mock o sleep.
Attese e future limitate a 10 secondi, gate rilasciato in finally, query in attesa
cancellate e worker terminati con attesa limitata.

Prove: 1000−100−200=700 con entrambe le richieste persistite; fondi 250 per
addebiti 100/200 con un solo successo, saldo 150 oppure 50 e veicolo/richiesta
dell'operazione fallita invariati; tutti gli otto movimenti contro una ricarica
concorrente; rimborsi su conti ordinati, su veicoli diversi e più volte sullo
stesso conto; conto mancante senza annullamenti/rimborsi parziali.

Trigger PostgreSQL temporanei controllano che addebito o rimborsi/cancellazioni
siano già avvenuti, poi sollevano un errore identificabile. Verificati rollback
del saldo, del blocco veicolo e della richiesta; per rimborsi multipli, rollback
di entrambi i saldi, cancellazioni e stato veicolo. Funzioni/trigger rimossi
in AfterEach; nessuna iniezione di errore nel codice di produzione.

## Ambiente, comandi ed esiti

OpenJDK Linux **21.0.12.1**, Maven **3.9.16**, PostgreSQL **16.15**, ambiente R00
di [testing.md](../../testing.md). Solo cluster `/tmp/drivehub-r00-pgdata`,
`127.0.0.1:55432`, database/ruolo `drivehub_test` e copia applicativa distinta con
`R00_SENTINEL`. Nessun collegamento al database applicativo preesistente.
Il primo avvio nella sandbox è terminato tra comandi: prima sonda Maven con
connessione rifiutata, senza eseguire prove funzionali; riavvio e prove riusciti
con escalation autorizzata. Credenziali caricate dal launcher privato, mai nei log.

Comandi dalla directory esterna, con il launcher documentato in R00:

```bash
python3 /tmp/drivehub-r00-maven.py ticket04-red -Ppostgres-tests -Dtest=BalanceConcurrencyIntegrationTest#concurrentPurchaseDepositsKeepBothDebits test
python3 /tmp/drivehub-r00-maven.py ticket04-green -Ppostgres-tests -Dtest=BalanceConcurrencyIntegrationTest#concurrentPurchaseDepositsKeepBothDebits test
python3 /tmp/drivehub-r00-maven.py ticket04-refunds-red -Ppostgres-tests '-Dtest=BalanceConcurrencyIntegrationTest#concurrentRefundsAcquireAccountsInClientOrder+missingRefundAccountRollsBackEveryRefundAndCancellation' test
python3 /tmp/drivehub-r00-maven.py ticket04-final-targeted -Ppostgres-tests -Dtest=BalanceConcurrencyIntegrationTest test
python3 /tmp/drivehub-r00-maven.py ticket04-full -Ppostgres-tests clean test
```

| Prova | Esito | Evidenza |
|---|---|---|
| RED addebiti | 1 failure attesa: 700 attesi, 800 effettivi; 0 errori/saltati | [log](red.log), [XML](red.xml) |
| GREEN stesso test | 1/1, 0 failure/errori/saltati | [log](green.log) |
| RED rimborsi | 2 failure attese: conto 2 già bloccato prima del conto 1; conto mancante ignorato | [log](refunds-red.log), [XML](refunds-red.xml) |
| Primo ampliamento fixture | 12/15; due errori e una failure: venditore ID 21 inesistente, corretto solo nel test a 16 | [log](targeted-fixture-error.log) |
| Mirati dopo correzione fixture | 15/15 | [log](targeted.log) |
| Mirati finali, incluso rifiuto autocommit | **16/16**, nessun errore/failure/salto | [log](final-targeted.log) |
| Suite completa con clean, isolamento e sentinella | **69/69**, 0 failure/errori/saltati; 62 sorgenti e 10 test compilati target 21 | [log](full.log), [XML Surefire](surefire/) |

La suite completa include **28 prove senza DB e 41 PostgreSQL**, compresi i
16 casi nuovi e `DatabaseIsolationIntegrationTest` (sentinella e rifiuto dei
target non sicuri prima dei reset). XML controllati; hash dei file compilati e
della documentazione di test in [tested-files.sha256](tested-files.sha256).

## Revisione finale e conservazione

Skill `code-review` applicata al diff locale della sola sessione fissato prima
delle revisioni, perché HEAD e working tree includevano ticket precedenti. Due
agenti indipendenti con contesti nuovi: [Standards 0](standards-review.md),
[Spec 0](spec-review.md). [Patch esatta revisionata](review.patch); nessun cambio
a codice, test o docs/testing.md dopo la suite/revisione.

`git diff --check` superato con relazione esclusa. Dei 40 file locali preesistenti,
38 hanno SHA-256 invariato; AuthService mantiene la correzione ticket 03 con il
solo cambio della lettura conto, docs/testing.md conserva il testo precedente
con un'appendice. Hash snapshot verificati, XML/log controllati; nessuna password
privata nelle evidenze. HEAD invariato e indice senza staging.
Cluster temporaneo arrestato con `pg_ctl -D /tmp/drivehub-r00-pgdata -m fast -w stop`.

## Limiti e prossimo passo

Warning Maven JavaFX preesistente; nessuna UI modificata o prova grafica svolta.
Non certificati unicità/transizioni concorrenti delle richieste, prenotazioni,
eventi Observer, prezzi concordati, segni contabili o cancellazione account:
restano ai rispettivi ticket. L'ordine qui garantito riguarda i conti, senza
promettere serializzazione di tutti gli altri oggetti applicativi.
Le sequenze PostgreSQL non sono transazionali; non si richiede continuità degli ID.
Ambiente e credenziali `/tmp` temporanei. Nessun test sul database applicativo reale.

Tutti i 5 criteri soddisfatti e verificati: ticket 04 chiuso, R02 completata,
rilievo #4 chiuso; totale 4/20 (#1, #4, #8, #19). AC13 resta parziale fuori da
questo perimetro. Prossimo passo: ticket 05 / R03 in una richiesta successiva;
nessun ticket successivo avviato.
