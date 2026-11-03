# Ticket 06 / R04 — totale concordato all'acconto

Verifica del 31 ottobre 2026, chiusura il 2 novembre 2026: **ticket 06 chiuso, 6/6 criteri**. Ticket 01–05 e prerequisiti 04/05
controllati chiusi prima di iniziare. Nessun commit/staging; Relazione_SWE esclusa
da letture, ricerche, copie, modifiche, generazioni e verifiche.

## Baseline e perimetro

HEAD `c2a81122ee6be44760ded4160fde229bd33e837c` invariato. Baseline acquisita in
`/tmp/drivehub-ticket06-baseline` prima degli interventi, 242 file del progetto,
compresi tutti i cambi locali precedenti; piano e ticket acquisiti separatamente.
[Hash iniziali](baseline-sha256.json), [patch della sola sessione](session.patch),
[file revisionati](files.txt), [conservazione](preservation.json), [hash finali](hashes.json).
Il diff rispetto a HEAD includerebbe lavoro precedente e non è la base di review.

Chiamanti verificati con rg mirato in src, prima della modifica:

- Request: create/factory acquisto e cessione, deposit, consumatori dei suoi importi;
  acconto e totale ora immutabili. `getRemainingAmount` restituisce la differenza
  solo per PURCHASE. Importi acquisto non negativi, a centesimi e acconto ≤ totale.
- PostgresRequestDao: tutti i cinque SELECT, mapRow, INSERT, oggetto restituito da
  save, UPDATE e DELETE. UPDATE di approvazione conserva il totale immutabile.
  RequestDao e OperationQueryService conservano le interfacce di consultazione.
- Schema: agreed_total richiesto per acquisto, NULL per cessione; demo con totali
  espliciti per i 10 acquisti e NULL per le 4 cessioni. Fixture acquisto ticket 04
  aggiornata insieme allo schema, senza alterare il protocollo dei conti.
- PurchaseService: prenotazione atomica, formula condivisa, addebito e snapshot nella
  stessa transazione. Completamento da totale/acconto persistiti, senza basi/sconti/anno.
  Approvazione, rifiuto, scadenza e lock conti preservati; eventi invariati.
- Cessioni: deposit rimane stima col segno preesistente, agreedTotal NULL. Submit,
  approve e reject conservano il contratto; i test preesistenti sono passati.
- Viste cliente/venditore: colonne separate totale/acconto/saldo, stima cessione
  distinta, messaggi di avvio e saldo con importi effettivamente salvati/riletti.
  La sola correzione CSS rende leggibile il testo selezionato in queste due tabelle.

## Ambiente e isolamento

[Versioni](environment.txt): OpenJDK Linux 21.0.12.1, Maven 3.9.16, PostgreSQL 16.15,
JavaFX 21.0.2. Target POM 21 invariato, nessuna nuova dipendenza.
Ripreso [ambiente R00](../../testing.md): solo cluster sacrificabile
127.0.0.1:55432, DB/ruolo drivehub_test e copia applicativa distinta con sentinella.
Nessun contatto con DB applicativo preesistente. Configurazione privata caricata
tramite launcher /tmp, mai copiata nelle evidenze. Reset/prove DB eseguiti in sequenza.

## TDD e comandi

Dalla radice del progetto, `python3 /tmp/drivehub-r00-maven.py <etichetta> ...`:

| Etichetta e argomenti | Esito | Log |
|---|---|---|
| `ticket06-agreement-red -Ppostgres-tests -Dtest=PurchaseAgreementIntegrationTest test` | 2 failure attese: 28000/24000 anziché 20000 | [RED](agreement-red.log) |
| `ticket06-agreement-green-v3` con gli stessi argomenti e la stessa regressione | 2/2 | [GREEN](agreement-green-v3.log) |
| `ticket06-targeted -Ppostgres-tests -Dtest=RequestAgreementTest,PurchaseAgreementIntegrationTest,PriceCalculatorServiceTest,PriceSelectionIntegrationTest,BalanceConcurrencyIntegrationTest,DemoSqlValidationTest test` | 57/57 | [Mirati](targeted.log) |
| `ticket06-ui-final-compile -DskipTests test-compile` | BUILD SUCCESS, nessun test eseguito | [Compilazione](ui-final-compile.log) |
| `ticket06-final-full -Ppostgres-tests clean test` | **103/103**, 0 failure/errori/saltati | [Suite](final-full.log), [XML](surefire/), [riepilogo](summary.json) |

La stessa regressione è fallita prima della correzione e passata dopo, tramite
initiatePurchase/approvePurchase/completePurchase e servizi pubblici di lettura.
È stata poi ampliata con controlli sullo snapshot e sugli altri percorsi richiesti.
La suite finale comprende 40 prove senza DB e 63 PostgreSQL: 7 nuove di dominio,
12 nuove PostgreSQL, 16 conti concorrenti preesistenti e isolamento/sentinella.
Ricompilati 63 sorgenti principali e 13 test con target 21.

Casi: 20000/4000/16000, base iniziale automatica e manuale, modifica basi/modalità/
sconti, nuova sessione AppContext e rilettura; centesimi 20000.03/4000.01/16000.02;
fixture di accordo dell'anno precedente, con prezzo corrente 19000 e vendita 20000
in entrambe le modalità, senza cambiare il clock di sistema. Prenotazione già occupata,
approvazione/scadenza, fondi insufficienti all'acconto e al saldo preservano i dati.
Trigger temporanei osservano un addebito già avvenuto e poi sollevano un errore su
INSERT request, INSERT sale, DELETE request e UPDATE vehicle: la rilettura tramite
servizi verifica rollback di conto, richiesta, vendita e stato. Nessun mock certifica
la persistenza; SQL aggiuntivo è preparazione di fixture e iniezione di guasti.

Tentativi iniziali, non prove superate: [prima sonda](red.log) senza server persistente
nella sandbox e primo RED con un prezzo rimasto per coincidenza a 20000;
[GREEN iniziale](agreement-green.log) e [secondo](agreement-green-v2.log) falliti in
setup per righe demo ancora da allineare. Corrette tutte le 14 righe prima del GREEN
finale. Avvio/prove DB poi eseguiti con escalation sul solo ambiente sacrificabile.
Il warning del modello dipendenze JavaFX è preesistente.

## JavaFX e tastiera

[Sorgente](AgreementUiCheck.java), [launcher](run-ui.py), [log finale](ui.log).
Il controllo riusa Robot, screenshot e attese del [ticket 05](../r03-prices/PriceUiCheck.java).
Xvfb temporaneo :96 senza TCP; librerie e binario già predisposti nel ticket 05.
Il launcher avvia seed e check in **due JVM distinte**: il primo processo termina
dopo accordi 20000/4000/16000 e cambi listino; il secondo ricostruisce tutti i servizi
e le viste dai dati PostgreSQL senza reset. Esito finale **UI CHECK SUCCESS**.

Verificati:

- Due accordi, automatico e manuale, e una cessione riletti dal venditore; valori
  20000/4000/16000 e stima cessione 16000 senza confondere saldo e stima.
- Selezione righe con frecce, focus tabelle richieste, Tab/Spazio/Invio per approvare;
  la rilettura dal servizio conferma venditore e saldo concordato nel messaggio.
- Navigazione ai tab con frecce, importi cliente coerenti, saldo da tastiera con
  conferma dei tre importi, vendita registrata da 20000 e addebito solo di 16000.
- Nuovo acquisto da tastiera al listino successivo: conferma provvisoria distinta
  dal messaggio restituito dall'accordo salvato 27000/5400/21600.
- Colore effettivo delle celle selezionate scuro (#1e1b4b su #e0e7ff), con contrasto
  corretto nelle sole tabelle richieste. Nessuna modifica alla navigazione applicativa.

Cinque screenshot finali ispezionati a 1280×760:
[venditore](seller-agreements.png), [cliente](client-agreements.png),
[conferma saldo](client-confirmation.png), [completamento](client-completed.png),
[nuovo accordo](client-new-agreement.png). Primi tentativi di harness affinati per
persistenza di Xvfb, navigazione tra tab, focus e attesa del frame della cattura;
solo log e immagini finali attestano la verifica.

## Revisione e limiti

Review indipendenti sul diff fissato della sola sessione concluse:
[Standards](standards-review.md) **0 violazioni / 0 smell**, [Spec](spec-review.md)
**0 rilievi**. Il 2 novembre entrambi i revisori hanno confermato l'esito anche
sul codice corrente dopo il ticket 07, autorizzato e verificato separatamente.
**Ticket 06 chiuso con 6/6 criteri verificati.**

Schema ricreabile D02, nessuna migrazione storica: installazioni esistenti richiedono
schema adeguato e totali realmente concordati, mai inferiti dal listino corrente.
La GUI usa fixture ridotta: nessuna certificazione prestazionale o screen reader.
Competizione completamento/rifiuto/scadenza al ticket 07, eventi/transizioni ai
rispettivi ticket, prestazioni tabelle al 14. Nessun altro ticket lavorato.
Ambiente /tmp temporaneo; arresto di PostgreSQL/Xvfb a fine sessione.


## Tracciabilità della chiusura — 2 novembre 2026

Nessuna modifica a codice, test, schema, fixture o viste in questa ripresa.
I 17 file interni dello snapshot del 06 corrispondono esattamente alla copia
`/tmp/drivehub-ticket07-baseline/repo`; i 7 file del manifest verificato del 07
corrispondevano ai file correnti prima degli aggiornamenti documentali di chiusura,
senza altri cambi ai sorgenti. La baseline /tmp
originale del 06 non esiste più: patch e hash qui conservati restano la fotografia
storica revisionata e non vengono sostituiti con un diff cumulativo del 07.

Gli XML della [suite finale 07](../r04-purchase-once/verification.md) sono stati
ricontrollati: **163/163**, zero failure/errori/saltati, compresi i 7 test di dominio
+ 12 PostgreSQL del 06, isolamento/sentinella e conti concorrenti. Non sono nuove
esecuzioni in questa ripresa. Le prove GUI/tastiera sopra rimangono quelle eseguite
sulle viste e sull'harness immutati, con nuova JVM e importi riletti.

[Controlli e hash di continuità](closure-checks.json), [solo delta documentale di
chiusura](closure.patch). Baseline di questa ripresa acquisita prima degli interventi
in `/tmp/drivehub-ticket06-closure-20261102`; il confronto non comprende lavoro
preesistente. HEAD e indice invariati, nessun commit/staging. R04 parziale per il
solo 08, non avviato; nessun lavoro aggiuntivo. Limiti sopra invariati.
