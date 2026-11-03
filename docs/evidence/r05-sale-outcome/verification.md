# Ticket 10 — esito della cessione e registro della concessionaria

Verifica del 2 novembre 2026. Implementato soltanto il ticket 10.
**Ticket 10 chiuso: 6/6 criteri soddisfatti e verificati.**
Prerequisiti 07 e 09 e tutti i ticket 01–09 controllati chiusi:
[controllo](prerequisites.json). Nessun commit/staging; Relazione_SWE esclusa.

## Versione e conservazione

HEAD iniziale `c2a81122ee6be44760ded4160fde229bd33e837c`. Baseline acquisita prima
delle modifiche: 390 file tracciati/non ignorati presenti, esclusi Relazione_SWE,
Git e output di build; piano e ticket esterni copiati separatamente in
`/tmp/drivehub-ticket10`. [Manifest](baseline-sha256.json).
Il lavoro locale 01–09 non è attribuito a questa sessione: nessuna review da HEAD.

[Patch fissata per la review](review.patch), [16 file](review-files.txt), SHA256
`8da69d421468addde119df3ec2105e40c54c9ff892c49bdfb3b4fe6cacf39f53`.
Comprende codice, test, testing.md e harness GUI; log, immagini e documenti di
chiusura sono evidenze aggiuntive. [Conservazione](preservation.json),
[controlli finali](integrity-checks.json), [patch finale della sessione](session.patch).
Gli hash della [suite finale](final-full-files.sha256.json) identificano sorgenti,
test, schema, fixture e POM realmente ricompilati; [hash GUI](ui-files.sha256.json).
I documenti finali non sono nuove prove runtime. Evidenze storiche 01–09 conservate.

## Mappa dei chiamanti

| Confine | Risultato |
|---|---|
| `approveSaleProposal` / `rejectSaleProposal` | Firme pubbliche conservate; helper comune rilegge e blocca richiesta e veicolo, verificando SALE e WAITING prima degli effetti |
| Richieste / veicoli / conti | Ordine richiesta → veicolo → conto singolo; lock richiesta del 07 e conto del 04 riusati; stesso conn fino a commit/rollback; nessuna autorità da oggetti UI |
| DAO veicolo | Aggiunta lettura ID FOR UPDATE con rifiuto autocommit; transizioni atomiche WAITING → SALE/SOLD; DELETE richiede una riga |
| Storico | Riutilizzata la verifica di tutte le operazioni del veicolo, con overload sulla connessione del chiamante; eliminata la verifica sale su connessione separata. Ultima PURCHASE per titolarità 09 resta distinta e invariata |
| Rifiuto | Richiesta eliminata nella transazione; senza storico elimina il VIN, con sale/rent/test_drive conserva tutte le righe e ripristina SOLD |
| Dominio / factory / mapping | ACQUISITION negativa nel registro, accredito cliente positivo. PURCHASE/PENALTY positive. Tipo esplicito 09 conservato dai mapper; nessuna classificazione dal segno o da soggetti nulli |
| Schema / fixture | Check segno/tipo nel dominio e DB; demo invariata. Due fixture 09 mantengono valore assoluto, clienti, venditori e storico con acquisizioni negative; aspettativa 05 ora verifica il segno esatto |
| AppContext / query / cliente | Stessa composizione e stesse dipendenze; servizio economico e letture esistenti riusati. Vista cliente invariata, saldo controllato anche nella GUI |
| Venditore / manager | Messaggi di cessione coerenti; registro dal punto di vista concessionaria, tipo da SaleType, contrasto selezione riusando CSS esistente |

Nessun nuovo conto o procedura economica; formula/base/centesimi 05, accordi 06,
elaborazioni 07, Observer 08 e titolarità/unicità 09 preservati e verificati.

## Ambiente e comandi

[Identità/privilegi/sentinella](environment.txt): JDK Linux 21.0.12.1,
Maven 3.9.16, PostgreSQL 16.15. Ricontrollati binari, PG_VERSION, cluster
arrestato e sei variabili del file privato 07 (0600) prima del riuso.
Il cluster dedicato è `/tmp/drivehub-ticket07-pgdata`, solo 127.0.0.1:55432;
DB/ruolo drivehub_test e copia applicativa distinta drivehub con R00_SENTINEL.
Nessun contatto ai database applicativi preesistenti. Credenziali fuori repository.
Avvio sandbox negato sui socket; avvio/prove riusciti con escalation sul solo cluster
sacrificabile. Il launcher temporaneo usa JDK21, `mvn -B -o` e variabili private 07.

Dalla directory esterna: `python3 /tmp/drivehub-ticket10/maven.py <etichetta> <argomenti>`.
Argomenti Maven ripetibili predisponendo l'ambiente di [testing.md](../../testing.md).

| Etichetta / selezione | Esito |
|---|---|
| `state-red -Ppostgres-tests -Dtest=SaleOutcomeIntegrationTest test` | 8 failure attese sugli stati errati; [log](state-red.log), [XML](state-red.xml), [stessa regressione](state-regression-source.java.txt) |
| `state-green`, stessi test | 8/8; [log](state-green.log), [XML](state-green.xml) |
| `amount-red -Ppostgres-tests -Dtest=SaleTypeTest,SaleOutcomeIntegrationTest#approvalPaysTheClientAndRecordsOneDealershipExpense test` | 2 failure attese sul segno; [log](amount-red.log), [sorgente servizio](amount-regression-source.java.txt) |
| `amount-green -Ppostgres-tests -Dtest=SaleTypeTest,SaleOutcomeIntegrationTest test` | 10/10; [log](amount-green.log) |
| `history-red`, metodi `rejectionPreservesEveryKindOfHistoryAndRemovesOnlyANewVehicle+aSkippedVehicleDeletionCannotConsumeTheRequest` | 1 failure e 2 errori attesi su 7: DELETE saltata, storico rent/test_drive; [log](history-red.log), [XML](history-red.xml), [sorgente](history-regression-source.java.txt) |
| `history-green -Ppostgres-tests -Dtest=SaleOutcomeIntegrationTest test` | 16/16; [log](history-green.log) |
| `races`, stessa classe ampliata | 31/31; [log con PID e blocker](races.log) |
| `validation-red`, metodi su venditore mancante e segno/tipo dominio/DB | 7 failure attese; [log](validation-red.log), [XML](validation-red-surefire/) |
| `validation-green -Ppostgres-tests -Dtest=SaleTypeTest,SaleOutcomeIntegrationTest,SaleProposalIntegrationTest,PriceSelectionIntegrationTest test` | 72/72; [log](validation-green.log) |
| `outcomes`, estensione rollback | 63/64: il nuovo test ipotizzava un pagamento zero consentito; il conto preesistente lo rifiuta. Aspettativa corretta a fallimento atomico, senza modificare la regola economica; [log](outcomes.log) |
| `stale-baseline-red`, stessi tre metodi obsolescenza/storico | 5 failure e 1 errore su 7 sulla copia isolata della baseline; [log](stale-baseline-red.log), [hash](stale-baseline-red-files.sha256.json). Invariati i metodi selezionati; escluso dal sorgente compilato solo il test DAO del nuovo metodo assente nella baseline |
| `targeted -Ppostgres-tests -Dtest=SaleOutcomeIntegrationTest,SaleTypeTest,SaleProposalIntegrationTest,TestDriveCancellationIntegrationTest,PurchaseConcurrencyIntegrationTest,PurchaseAgreementIntegrationTest,BalanceConcurrencyIntegrationTest,PriceCalculatorServiceTest,PriceSelectionIntegrationTest,DemoSqlValidationTest test` | 206/206; [log](targeted.log) |
| `full -Ppostgres-tests clean test` | 259/259 prima dell'ultimo riuso CSS del registro; risultato intermedio, [log](full.log) |
| `final-full -Ppostgres-tests clean test` | **259/259, zero failure/errori/salti**; [log](final-full.log), [XML](final-full-surefire/), [riepilogo](summary.json) |

Suite finale: **45 senza DB e 214 PostgreSQL**, inclusi isolamento/sentinella, fixture ripetibili e tutti i protocolli 04–09. Target Java21 con `-Xlint:all`; resta il warning Maven preesistente sulle dipendenze JavaFX.

La suite 195/195 del 09 è storica. Nessun mock certifica la persistenza.
I primi cicli conservano sorgenti RED e XML; i cicli successivi anche manifest
prima della compilazione. La copia RED isolata contiene solo baseline src/POM e
il test corrente: non ha sovrascritto il repository o il lavoro locale.

## Criteri e prove

`SaleOutcomeIntegrationTest` conta 60 casi PostgreSQL, `SaleTypeTest` 5 di dominio.
I primi test accertano che il vecchio servizio accettava stati non WAITING,
registrava il segno errato e non gestiva correttamente storico e DELETE saltata.
La prova aggiuntiva sulla baseline riproduce client/importo/tipo/stato obsoleti e
lettura storica separata. Non si attribuisce un doppio pagamento osservato a una
gara che i controlli delle righe precedenti già facevano fallire in rollback.

- Gare di tutti gli esiti nei due ordini, con/senza storico, richieste ripetute:
  una sola acquisizione -16000, conto 1000 → 17000, una sola modifica/eliminazione
  del veicolo misurata da trigger transazionale. Perdente fallisce senza effetti.
- Gate su richiesta/veicolo; PID distinti e pg_blocking_pids osservati prima del
  rilascio, nessuno sleep. Rilettura di cliente/importo/tipo/stato dopo l'attesa.
  Query/worker/cleanup con limiti di 10 secondi e cancellazione delle attese residue.
- Rifiuto senza storico e con PURCHASE, ACQUISITION, RENT, TEST_DRIVE e combinati:
  nessun residuo o pagamento; storico e attribuzioni confrontati integralmente.
  Un trigger scrive storico dopo DELETE richiesta nella stessa transazione:
  il rifiuto deve vederlo e conservare il veicolo SOLD.
- Errori e RETURN NULL su conto, INSERT sale, UPDATE/DELETE veicolo e DELETE
  richiesta; account/veicolo/richiesta/venditore mancanti, tipo errato, overflow,
  vincoli segno/tipo. Snapshot completi prima/dopo e riletture pubbliche da nuovi AppContext.
- Trigger AFTER sull'ultima scrittura osservano conto già accreditato, acquisizione,
  richiesta consumata e stato finale oppure eliminazione fisica del veicolo;
  poi errore PostgreSQL e rollback integrale. Non basta un errore prima degli effetti.

## JavaFX, tastiera e review

[Sorgente](SaleOutcomeUiCheck.java), [launcher](run-ui.py), [log finale](ui.log):
**UI CHECK SUCCESS**. JDK21 e JavaFX21.0.2, Xvfb :97 senza TCP, rendering software,
pacchetti ufficiali estratti solo in /tmp; nessuna modifica di sistema o desktop utente.
Seed/check in due JVM; solo DB dedicato. Primo seed fallito per targhe duplicate
della fixture, poi corrette; un primo passaggio Robot perdeva focus dopo il dialogo,
risolto ricostruendo la vista. [Log seed](ui-seed-failed.log), [log focus](ui-focus-failed.log).

Verificati frecce, Tab, Spazio e Invio; selezione con focus e testo scuro leggibile,
approvazione e sessione obsoleta senza doppio pagamento, rifiuto dei due VIN,
saldo cliente, registro con -16000/+20000/+800 e due tipi distinti a zero con
attribuzioni nulle. Schermate: [venditore](seller-approved.png),
[cliente](client-balance.png), [manager](manager-ledger.png). Schermate finali
ispezionate visivamente; riutilizzato il CSS di selezione del 06 per il registro.

[Standards](standards-review.md): **0 violazioni, 0 smell azionabili**.
[Spec](spec-review.md): **0 rilievi**. Revisori distinti, contesti nuovi, stessa
patch finale fissata. Nessun codice/test/harness cambiato dopo le prove finali.

## Limiti e prossimo passo

Schema ricreabile secondo D02: prima dell'uso su installazioni esistenti occorre
adeguare il vincolo e gli importi delle acquisizioni già classificate dai fatti;
non inferire i tipi dai vecchi segni né resettare i dati applicativi.
Non certificati screen reader, prenotazioni ticket 11, altre transizioni,
cancellazione account o prestazioni. Ambiente /tmp temporaneo, da ricontrollare.
[Lettura finale](environment-final.txt) conferma identità e sentinella dopo la suite.
Cluster [arrestato](cluster-stop.txt) e display Xvfb :97 terminato; nessun servizio
applicativo preesistente usato. HEAD e indice invariati; nessun commit.
**R05 completata per 09–10; AC03/#3 e AC10/#10 chiusi, totale 10/20**
(#1, #2, #3, #4, #6, #8, #10, #11, #17, #19). AC13 resta trasversale.
Prossimo passo: nessun lavoro residuo del ticket 10. Ticket 11 soltanto su una
nuova richiesta; nessun successivo avviato.
