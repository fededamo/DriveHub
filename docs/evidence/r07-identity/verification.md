# Ticket 13 — cancellazione identità e storico

Sessione del 2 novembre 2026, soltanto ticket13. Prerequisito01 e ticket01–12
verificati `State: closed`, senza criteri aperti:
[stati e hash](prerequisites.json). Letti AGENTS, piano, glossario, ADR0001,
specifica e testing; riusati i confini già approvati. `Relazione_SWE` esclusa.
Nessun commit/staging, nessun ticket successivo.

## Versione e baseline

Fotografia iniziale di **846 file** in `/tmp/drivehub-ticket13-session/before`:
progetto, evidenze precedenti, intero tracker e piano esterni. Esclusi `.git`,
`target` e `Relazione_SWE`. [Manifest](baseline-sha256.json),
[HEAD](initial-head.txt), [indice](initial-index.sha256).
Un diff da HEAD includerebbe impropriamente il lavoro precedente: la review
usa esclusivamente il confronto con questa fotografia locale.

Nuova baseline prima di modificare sorgenti/test: **330/330**, zero failure,
errori o salti, distinta dal risultato storico12:
[log](baseline.log), [XML](baseline-surefire/), [input](baseline-files.sha256.json).
Il primo tentativo nella sandbox fallì con 283 errori di connessione perché
il server non persisteva fra comandi; [log separato](baseline-sandbox-failed.log).
Non è una baseline riuscita né una regressione del prodotto.

## Chiamanti e responsabilità

| Confine / chiamanti | Comportamento verificato |
|---|---|
| `AccountManagementDialog.show` | Unico chiamante applicativo di `deleteAccount`; continua a passare `getRoleDisplayName`, come email/password. Il dialogo è aperto dalle dashboard cliente, venditore e manager; l'overload con AppContext resta invariato |
| `AuthService.deleteAccount` | Firma pubblica conservata. Password richiesta; lock identità → conto cliente → verifica associazioni → DELETE di una riga sulla stessa connessione. Rifiuti di dominio leggibili dopo rollback; successo soltanto al ritorno |
| `PostgresUserDao.resolveTable` | Mappatura unica già esistente Cliente/CLIENT, Venditore/SALESMAN, Manager/MANAGER, riusata ora anche dal controllo associazioni; nessuna conversione aggiunta a dialogo o servizio |
| `UserDao.hasAssociatedOperations` | Unico chiamante AuthService aggiornato alla connessione transazionale; unica implementazione PostgreSQL. Cliente: request/test_drive/rent/sale; venditore: request/sale; manager senza associazioni di dominio |
| `UserDao.lockUser` / `deleteUser` | Chiamati solo da AuthService; lock NO KEY UPDATE con transazione obbligatoria, identità/ruolo esistenti; DELETE richiede esattamente una riga e traduce FK23503 in rifiuto leggibile |
| Email/password | Firme, tre chiamanti nel dialogo e mappatura condivisa invariati; regressioni email03 e suite completa verificano il comportamento precedente |
| Acquisto | `initiatePurchase`, `approvePurchase`, `completePurchase`, `rejectPurchase`, scadenze: firme invariate; FK richiesta/storico protegge identità e venditore. Gara contro acquisto già sul conto riproduceva un deadlock: NO KEY UPDATE lascia terminare il KEY SHARE della FK, poi si rilegge sotto lock del conto |
| Prenotazioni11 | `bookRental`/`bookTestDrive` restano veicolo → cliente NO KEY UPDATE → conto; gare con cancellazione nei due ordini, anche dopo rollback effettivo di identità/conto |
| Cessione09–10 | `submitSaleProposal`/`approveSaleProposal`: FK cliente/venditore sulla stessa transazione; provati inserimento del nuovo veicolo, accredito e acquisizione effettivi prima di conflitto/rollback. Nessun altro servizio modificato |
| Schema / fixture | Solo `fk_sale_salesman`: SET NULL → RESTRICT. Le altre FK identità erano già RESTRICT. `bank_account.client` resta CASCADE per il conto dimostrativo; demo intatta |

Le ricerche sono limitate a src, istruzioni e documenti pertinenti; nessun grafo
esistente da consultare, nessuna generazione Graphify o nuova dipendenza.

## Ambiente ricreato

`/tmp` precedente assente. Java17 iniziale insufficiente; scaricati pacchetti
Ubuntu ufficiali ed estratti senza installazioni di sistema in
`/tmp/drivehub-ticket13-tools/root`. OpenJDK **21.0.12.1**, Maven **3.9.16**,
PostgreSQL **16.15**: [versioni](versions.txt). Corrette solo nelle copie
estratte le symlink Java e il percorso xkbcomp di Xvfb; aggiunte le librerie
necessarie. Nessuna modifica alle versioni richieste dal POM.

Nuovo cluster `/tmp/drivehub-ticket13-pgdata`, solo **127.0.0.1:55433**, socket
in /tmp. Database/ruolo `drivehub_test` non amministrativo, senza appartenenze
o altri DB posseduti; separata copia sacrificabile `drivehub` con sentinella.
[Identità e privilegi](environment.txt), [log PostgreSQL](postgres.log).
Avvio e prove autorizzati fuori sandbox per mantenere il processo e la rete
raggiungibili fra comandi. Nessun DB applicativo preesistente contattato.
Sei variabili private in `env.json` con permessi0600, mai copiate nelle prove.

Xvfb isolato **:95**, 1440×1000, senza TCP, rendering JavaFX software.
[Log Xvfb](xvfb95.log): warning EGL su /dev/dri e listener Unix, display attivo
tramite socket locale astratto e prove Robot riuscite. Nessun servizio di
sistema avviato; ambiente temporaneo da ricontrollare al riuso.

## TDD e PostgreSQL reale

Launcher privato `python3 /tmp/drivehub-ticket13-session/maven.py <etichetta>
<argomenti>`: Java21, variabili private e `mvn -B -o`. Comandi portabili in
[testing](../../testing.md). Ogni esecuzione conserva log, XML e hash di src,
POM, schema e demo. [Riepilogo machine-readable](summary.json).

| Etichetta / comando Maven | Esito |
|---|---|
| baseline `-Ppostgres-tests clean test` | 330/330, zero failure/errori/salti |
| roles-red `-Ppostgres-tests -Dtest=IdentityDeletionIntegrationTest test` | 3 failure attese su6: ruolo Venditore permetteva DELETE e azzeramento storico |
| roles-green, stesso comando | 6/6 |
| requests-red, stesso comando | 4 failure attese su10: sola richiesta non riconosciuta dal controllo, errore generico FK |
| requests-green, stesso comando | 10/10 |
| history-race-red, stesso comando | 5 failure attese su15: tre DELETE dirette azzeravano l'attribuzione; gara vendita/cancellazione perdeva il venditore; acquisto/cancellazione produceva deadlock PostgreSQL reale |
| history-race-green, stesso comando | 15/15 |
| skipped-delete-red, stesso comando | 1 failure attesa su16: DELETE saltata riportava successo |
| skipped-delete-green, stesso comando | 16/16 |
| extended-cases, stesso comando | 56 casi,4 failure del nuovo test: attesi16000 invece di20000 per l'accredito della fixture, quindi gate non raggiunti |
| extended-fixed, stesso comando | 56/56, corretta solo l'aspettativa della prova |
| final-cases, stesso comando | **60/60**, zero failure/errori/salti |
| targeted, selezione estesa sotto | **356/356**, zero failure/errori/salti |
| final-full `-Ppostgres-tests clean test` | **390/390**, zero failure/errori/salti;47 senzaDB/343 PostgreSQL |

Selezione esatta di `targeted`:

```bash
mvn -Ppostgres-tests -Dtest=IdentityDeletionIntegrationTest,AuthServiceIntegrationTest,VehicleDestinationIntegrationTest,BookingRulesTest,BookingConcurrencyIntegrationTest,TestDriveCancellationIntegrationTest,BalanceConcurrencyIntegrationTest,PriceCalculatorServiceTest,PriceSelectionIntegrationTest,PurchaseAgreementIntegrationTest,PurchaseConcurrencyIntegrationTest,SaleProposalIntegrationTest,SaleOutcomeIntegrationTest,SaleTypeTest,DemoSqlValidationTest,DatabaseIsolationIntegrationTest test
```

Quattro [coppie RED/GREEN con test identico](regression-identity.json).
Gli ampliamenti successivi hanno hash propri; non sono spacciati per RED
precedenti. Il deadlock è anche nel log PostgreSQL. Le prove di cessione
corrette non sono regressioni del prodotto.

I60 casi coprono nomi ruolo visualizzati/persistiti, ruoli errati/iniezione,
password, cliente con richieste PURCHASE/SALE, venditore con sola richiesta
assegnata o sole vendite PURCHASE/ACQUISITION/PENALTY, storico test drive e
noleggi, identità prive di vincoli, cliente con/senza conto dimostrativo.
Le attribuzioni e tutti i dati sono confrontati da connessioni nuove.

Gare con connessioni distinte, gate SQL o advisory, PID e `pg_blocking_pids`
registrati **prima** del rilascio; nessuno sleep nelle gare. Timeout di10s per
query, osservazione, future e cleanup; gate sempre rilasciato e worker
cancellati/chiusi in uscita. Ordini coperti: prenotazione/cancellazione,
acquisto/cancellazione, acquisizione/cancellazione venditore e operazioni
in attesa durante una cancellazione fallita. Anche proposta di nuovo VIN e
approvazione acquisto non possono lasciare FK a identità già cancellate.

Trigger PostgreSQL AFTER DELETE attestano identità e conto realmente assenti
prima dell'errore; AFTER INSERT acquisizione controlla saldo50000 e record
storico già scritti; BEFORE INSERT richiesta vede il nuovo veicolo WAITING.
Sequenze diagnostiche e motivi di errore attestano le scritture anche dopo
rollback. Verificati rollback di conto, accredito, acquisizione, richiesta,
nuovo veicolo e approvazione; la seconda operazione può riuscire quando la
cancellazione fallisce. Nessun mock certifica vincoli o transazioni.

## GUI e tastiera

[Harness](IdentityUiCheck.java), [launcher](run-ui.py), [log finale](ui.log),
[hash input UI](ui-files.sha256.json): **UI CHECK SUCCESS**. Costruttore pubblico
del dialogo, iniezione AuthService e Robot05; seed/check in JVM distinte sul
solo DB dedicato. Frecce per le schede, Tab ai campi/pulsanti, password digitata,
Spazio, Invio ed Esc. Callback solo dopo commit/successo e uscita; nessun
callback per annullamento, password errata, richiesta o storico.

Sei schermate finali ispezionate: [regole cliente](client-rules.png),
[conferma](confirmation.png), [richiesta cliente](client-request-rejected.png),
[storico venditore](salesman-history-rejected.png),
[password errata](wrong-password.png), [successo](client-deleted.png).
La descrizione va a capo entro480px; focus e pulsante sono visibili. Il cliente
senza vincoli perde identità e conto dimostrativo, venditore e manager senza
vincoli sono eliminabili. Prima del consenso la UI distingue identità e conto.

Due avvii harness falliti sono conservati: titolo null di una finestra JavaFX
([log](ui-title-failed.log)), maiuscola iniziale del testo atteso
([log](ui-case-failed.log)); nessun cambiamento prodotto per correggerli.
Una prima esecuzione funzionale riusciva ma alcune catture erano premature:
la finale aspetta tre frame con AnimationTimer prima di catturare gli alert.
Le pause Robot ereditate riguardano il rendering, mai le gare DB.
Nessuna certificazione screen reader o benchmark prestazioni.

## Verifica finale e review

Suite finale **390/390**, inclusi isolamento/sentinella e tutte le regressioni:
[log](final-full.log), [XML](final-full-surefire/),
[hash input](final-full-files.sha256.json). Input runtime e harness identici
alle versioni verificate. [Sentinella finale in sola lettura](sentinel-final.txt),
[arresto PostgreSQL e Xvfb](shutdown.txt): porta dedicata non risponde,
display non in esecuzione. Nessun altro servizio arrestato.

Review indipendenti: [Standards](standards-review.md) **0 violazioni/0 smell
azionabili**, [Spec](spec-review.md) **0 rilievi**, sul solo diff congelato della
sessione: [patch](review.patch), [elenco](review-files.txt),
[hash snapshot](review-files.sha256.json),12file; SHA256
`08237df074ee2e3074135d31497fb0e627823fef856bf2da548cf32233aab68d`.
Nessun codice/test/harness cambiato dopo prove finali e review. I documenti di
chiusura successivi non costituiscono nuove prove runtime. Anche il
[delta documentale di chiusura](closing-docs.patch) è stato controllato da
entrambi i revisori indipendenti: **0 rilievi per asse**; SHA256
`cd141623bf5f1335067cc61b1a75bd33bd9de4e2dafcf4e273732ecfc1792c16`.
Il [diff finale della sessione](session.patch) include gli aggiornamenti
documentali; [hash finali](session-files.sha256.json).

[Conservazione](preservation.json):846file iniziali presenti,838 identici,
8aggiornamenti intenzionali (cinque file prodotto, testing, piano e ticket13),
zero eliminazioni. Ticket/evidenze01–12 e successivi intatti, HEAD/indice
invariati. [Controlli](integrity-checks.json): whitespace del diff, link locali,
input runtime/UI identici, perimetro e assenza credenziali private nelle prove.
Non si correggono spazi preesistenti dei documenti storici per alterarne gli hash.

**5/5 criteri verificati, ticket13 chiuso; R07 e AC09/#9 completati,13/20 rilievi
integralmente chiusi.** AC13 resta trasversale; nessun ticket successivo,
nessun commit/staging. Relazione_SWE esclusa.
Per installazioni
esistenti applicare il solo cambio FK documentato in testing prima del nuovo
codice; nessun reset applicativo e nessun recupero automatico delle
attribuzioni eventualmente già azzerate in passato.
