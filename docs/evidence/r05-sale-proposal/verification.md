# Ticket 09 — proposta di cessione e titolarità

Verifica del 2 novembre 2026: **6/6 criteri soddisfatti e verificati,
ticket 09 chiuso**. Revisione indipendente Standards/Spec completata senza rilievi. Implementato soltanto 09.
Prerequisito 01 e precedenti 01–08 tutti chiusi: [controllo](prerequisites.json).
Nessun commit/staging; Relazione_SWE sempre esclusa.

## Versione e baseline

HEAD `c2a81122ee6be44760ded4160fde229bd33e837c`. Prima delle modifiche acquisiti
401 file locali in `/tmp/drivehub-ticket09-baseline`, inclusi piano e tracker
esterni, istruzioni e lavoro preesistente 01–08. Esclusi `.git`, `target` e
Relazione_SWE. Nessun confronto da HEAD usato per attribuire il lavoro di oggi.

[Manifest baseline](baseline-sha256.json), [versione RED iniziale](red-files.sha256.json),
[regressione RED/GREEN iniziale](regression-source.java.txt),
[hash sorgenti/schema/fixture/POM verificati nella suite finale](tested-files.sha256.json).
[Patch fissata per le revisioni](review.patch), [18 file revisionati](review-files.txt),
[conservazione della baseline](preservation.json), [controlli finali](integrity-checks.json).
La patch di revisione ha SHA256
`59bb122564c1a085fc68bb6f0e1b15b612b375638786eee1e6b86cfff50adb4a`.
387 file preesistenti identici, 14 aggiornati intenzionalmente, nessuna eliminazione;
aggiunti enum e due classi di test, oltre alle nuove evidenze. Tutti i ticket e le
evidenze 01–08 intatti; HEAD e indice invariati. La patch passa
`git apply --check --reverse -p1` dalla directory esterna.

I GREEN intermedi nei log certificano solo la rispettiva fase TDD; la suite finale
ricompila e verifica l'intera versione indicata dal manifest finale.

## Chiamanti e responsabilità

| Percorso | Cambiamento / verifica |
|---|---|
| `SaleToDealershipService.submitSaleProposal` | Lock/rilettura del VIN noto; SOLD, ultima vendita al proponente, nessuna richiesta aperta; modello/anno coerenti; aggiornamento e richiesta nella stessa transazione; base/modalità conservate |
| Dominio `Sale` / `SaleType` | Tipo obbligatorio PURCHASE/ACQUISITION/PENALTY nelle factory e nella ricostruzione; importi e segni invariati |
| `SaleDao` / JDBC / storico | Tutti i SELECT/INSERT/mapper conservano il tipo; ultima vendita effettiva filtra PURCHASE, data DESC e ID DESC a parità di data; query sulla connessione del chiamante |
| `VehicleDao` / JDBC | Lettura VIN con FOR UPDATE solo in transazione attiva; aggiornamento richiede una riga per non lasciare richieste dopo scritture saltate |
| `RequestDao` / JDBC / schema | Controllo su tutte le richieste aperte, comprese PURCHASE approvate; UNIQUE request(vehicle) anche contro scritture dirette o concorrenti |
| VIN nuovo | Inserimento con base modello e stima preesistente; unicità VIN impedisce sovrascritture; qualsiasi errore annulla inserimento e richiesta |
| Schema/demo | `sale.type` obbligatorio con tre valori; tutte le 40 righe della demo conservano ID, soggetti, veicolo, data e importo; aggiunto solo il tipo già documentato (32 vendite, 8 penali) |
| Fixture 05 | Il veicolo sintetico SOLD mantiene base manuale 10000 e stima 14400; aggiunta la vendita effettiva al cliente 1, per 7200, coerente con il prezzo commerciale della fixture |
| AppContext / catalogo / acquisto / noleggio / richieste | Composizione e firme dei servizi invariati; tutte le factory dello storico impostano ora il tipo. Catalogo, conti 04, formula/base/centesimi 05, accordo 06, elaborazioni 07 e Observer/rollback 08 verificati nelle suite |
| Viste cliente/venditore/manager e messaggi | Chiamanti esaminati; nessuna vista modificata. Il messaggio di dominio viene esposto dal servizio dopo rollback, così il catch esistente mostra il motivo del rifiuto; errori SQL mantengono il wrapper transazionale |

## Ambiente, comandi ed esiti

[Ambiente verificato](environment.txt): OpenJDK Linux 21.0.12.1, Maven 3.9.16,
PostgreSQL 16.15. Verificati prima del riuso binari, cluster arrestato, launcher,
sei variabili private e modalità 0600 del file del ticket 07. R00 assente;
nessuna reinstallazione. Primo avvio sandbox negato sui socket, poi avvio e
prove con escalation sul solo cluster sacrificabile `127.0.0.1:55432`.
Identità/privilegi ricontrollati in sola lettura: DB/ruolo `drivehub_test`, copia
applicativa distinta `drivehub`, sentinella presente. Nessun DB applicativo
preesistente contattato. Credenziali fuori repository e mai stampate.
Cluster arrestato dopo suite/controlli con `pg_ctl -D /tmp/drivehub-ticket07-pgdata -m fast -w stop` (`server stopped`).

Dalla directory esterna: `python3 /tmp/drivehub-ticket09-maven.py <etichetta> <argomenti>`.
Il launcher temporaneo deriva dall'08 e usa JDK 21, `mvn -B -o`, variabili del 07;
i comandi Maven equivalenti sono in [testing.md](../../testing.md).

| Etichetta / selezione | Esito | Evidenza |
|---|---|---|
| `red -Ppostgres-tests -Dtest=SaleProposalIntegrationTest test` | 1 failure attesa: il VIN della concessionaria viene accettato | [log](red.log), [XML](red.xml) |
| `green`, stessi argomenti e stessa regressione | 1/1 dopo controllo stato | [log](green.log), [XML](green.xml) |
| `ownership-red -Ppostgres-tests -Dtest=SaleProposalIntegrationTest#soldVinWithoutAnEffectiveSaleIsRejected test` | 1 failure attesa: SOLD senza vendita accettato | [log](ownership-red.log), [XML](ownership-red.xml) |
| `ownership-green -Ppostgres-tests -Dtest=SaleProposalIntegrationTest test` | 2/2 dopo verifica ultima vendita | [log](ownership-green.log), [XML](ownership-green.xml) |
| `duplicates-red -Ppostgres-tests -Dtest=SaleProposalIntegrationTest#databaseRejectsAnotherOpenRequestOfAnyType+competingProposalsKeepOnlyTheFirstCompleteVehicleAndRequest test` | 2 failure su 3: duplicato SQL accettato e gara VIN noto con due successi; VIN nuovo già protetto | [log](duplicates-red.log), [XML](duplicates-red.xml), [sorgente](duplicates-regression-source.java.txt) |
| `duplicates-green -Ppostgres-tests -Dtest=SaleProposalIntegrationTest test` | 5/5 dopo lock e UNIQUE | [log](duplicates-green.log), [XML](duplicates-green.xml) |
| `writes-red -Ppostgres-tests -Dtest=SaleProposalIntegrationTest#skippedVehicleUpdateCannotLeaveAnOpenRequest+aKnownVinCannotUseAnotherModelOrYearForItsEstimate test` | 2 failure su 3: UPDATE saltata e modello incongruente accettati; anno già rifiutato dalla stima | [log](writes-red.log), [XML](writes-red.xml), [sorgente](writes-regression-source.java.txt) |
| `cases -Ppostgres-tests -Dtest=SaleProposalIntegrationTest,SaleTypeTest,PriceSelectionIntegrationTest test` | 31/31 dopo controllo righe/identità e ampliamento | [log](cases.log) |
| `message-red -Ppostgres-tests -Dtest=SaleProposalIntegrationTest#rejectionMessageIsAvailableToTheExistingView test` | 1 failure attesa: la vista riceve solo il wrapper generico | [log](message-red.log), [XML](message-red.xml) |
| `targeted -Ppostgres-tests -Dtest=SaleProposalIntegrationTest,SaleTypeTest,TestDriveCancellationIntegrationTest,PurchaseConcurrencyIntegrationTest,PurchaseAgreementIntegrationTest,BalanceConcurrencyIntegrationTest,PriceCalculatorServiceTest,PriceSelectionIntegrationTest,DemoSqlValidationTest test` | **142/142**, zero failure/errori/saltati | [log](targeted.log) |
| `full -Ppostgres-tests clean test` | **195/195**, zero failure/errori/saltati | [log](full.log), [16 XML](surefire/), [riepilogo](summary.json) |

Suite finale: 41 test senza DB e 154 PostgreSQL, inclusi isolamento/sentinella e
fixture ripetibili. Ricompilazione target 21 con `-Xlint:all`; rimane il warning
preesistente del modello dipendenze JavaFX. La baseline storica 171/171 del 08
è preservata e non usata come verifica delle modifiche di oggi.

## Criteri e prove

| Criterio | Prova ripetibile |
|---|---|
| Stato, titolarità attuale e richieste nella transazione | `soldVinWithoutAnEffectiveSaleIsRejected`, `aPastSaleDoesNotAllowADealershipVehicle` (SALE/RENT/BLOCKED/WAITING), `everyOpenRequestBlocksTheProposal` (SALE, PURCHASE senza e con approvazione); lock/query richiedono transazione attiva |
| Concessionaria/altri clienti senza mutazioni | `dealershipVinIsRejectedWithoutAnyMutation`, rifiuto vecchio proprietario; snapshot completi PostgreSQL di veicoli, richieste, storico e conti prima/dopo |
| Ultima vendita effettiva | `onlyTheLatestEffectiveBuyerCanProposeAndHistoryIsPreserved`: data prima di ID, ID a pari data, nuovo compratore 2, acquisizione successiva al cliente 1 e penale ignorate. `anAcquisitionAloneDoesNotEstablishOwnershipButAZeroPriceSaleDoes`: acquisizione sola insufficiente, vendita a zero valida |
| Unicità SQL, duplicati e concorrenza | `databaseRejectsAnotherOpenRequestOfAnyType` verifica SQLSTATE 23505 anche per PURCHASE approvata contro SALE; `competingProposalsKeepOnlyTheFirstCompleteVehicleAndRequest` sui VIN noto/nuovo con due sessioni distinte, normalizzazione maiuscole/spazi, dati diversi dei concorrenti e doppio invio sequenziale |
| VIN nuovo e stima, fixture e messaggi | `newVinKeepsTheExistingEstimateAndRejectsSequentialDuplicates`, `aKnownVinCannotUseAnotherModelOrYearForItsEstimate`, `invalidNewProposalLeavesNoVehicleOrRequest`, `rejectionMessageIsAvailableToTheExistingView`, test tipi/factory e prezzi 05 |
| Rollback reale e scritture interessate | `requestFailureRollsBackNewAndKnownVehicles`: trigger AFTER INSERT verifica WAITING/targa/km già scritti, poi errore; nessuna scrittura residua. `skippedVehicleUpdateCannotLeaveAnOpenRequest`: trigger RETURN NULL non lascia richiesta. Riletture da nuovi AppContext; nessun mock |

Il gate SQL esterno blocca l'INSERT richiesta. Prima del rilascio, pg_stat_activity
con pg_blocking_pids conferma uno e poi due backend distinti in attesa: il secondo
attende il veicolo noto o l'INSERT del VIN nuovo. Il gate viene rilasciato solo dopo
questa osservazione. Nessuno sleep; timeout di 10 secondi su query/worker e cleanup
con rollback e cancellazione delle query ancora bloccate. I dati del concorrente
perdente non sovrascrivono targa/chilometri o identità del vincitore.

## Standards

[Revisione indipendente](standards-review.md): **0 violazioni documentate,
0 smell azionabili**.

## Spec

[Revisione indipendente](spec-review.md): **0 rilievi** (requisiti, perimetro,
correttezza). Due contesti nuovi e stessa patch fissata; nessuna suite/SQL
rieseguita dai revisori. Nessuna modifica a codice/test dopo la suite finale o
le revisioni. I documenti di chiusura aggiornati dopo le review non costituiscono
nuove prove runtime. [Patch finale della sessione](session.patch).

## Limiti e prossimo passo

Nessuna vista modificata: nessun nuovo controllo JavaFX/tastiera richiesto o eseguito.
Nessuna nuova procedura esterna di proprietà, nessuna modifica alla formula, agli
importi/segni o all'accettazione/rifiuto della cessione (ticket 10). Prenotazioni 11,
altre transizioni, prestazioni e screen reader esclusi. Schema ricreabile secondo
D02; installazioni esistenti richiedono adeguamento dello schema e classificazione
fattuale dello storico, senza dedurre i tipi dai segni attuali. `/tmp` temporanea.
**Ticket 09 chiuso, R05 parziale per il ticket 10.** AC02/#2 chiuso: totale
rilievi integralmente chiusi **8/20** (#1, #2, #4, #6, #8, #11, #17, #19).
AC03 per le approvazioni e AC10 restano al ticket 10; AC13 trasversale.
Nessun ticket successivo avviato. Prossimo passo: ticket 10 soltanto in una nuova
richiesta; ricontrollare prima l'ambiente temporaneo. Nessun blocco residuo del 09.
