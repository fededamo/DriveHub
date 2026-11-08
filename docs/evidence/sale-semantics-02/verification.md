# Ticket 02 — Registro della concessionaria con importi non negativi

Baseline: `a260598`, albero inizialmente pulito. Fonte: sezione 02 di
`TRACKER_DRIVEHUB/semantica-operazioni/proposta-ticket.md` e relativa `spec.md`,
nel tracker esterno al repository. Data della verifica: 8 novembre 2026.

## Contratto verificato

`SaleType` contiene `SALE` (vendita al cliente), `PURCHASE` (acquisto dal cliente)
e `PENALTY` (penale incassata). Le factory, la ricostruzione e PostgreSQL rifiutano
importi negativi; nessun valore assoluto o conversione automatica dei nuovi input.
Anche a zero il tipo rimane esplicito; i pagamenti a zero continuano a fallire.
Il registro manager indica entrata/uscita nel testo, mostrando importi non negativi.

Il contratto Request del ticket 01 resta invariato: `SALE` con acconto/totale
concordato non negativi e acconto non superiore al totale; `PURCHASE` con stima
non negativa in `deposit` e `agreed_total` nullo.

| Requisito | Prova sul confine pubblico |
|---|---|
| Tipi, negativi, zero esplicito | `SaleTypeTest`; `SaleOutcomeIntegrationTest` per vincoli, salvataggio e rilettura DAO |
| Vendita 20.000, acconto 4.000, saldo 16.000 dopo cambio listino | `PurchaseAgreementIntegrationTest`: accordo riletto, `SALE` da 20.000 e saldo conto 10.000 da 30.000 iniziali |
| Cessione 16.000, un solo accredito | `SaleOutcomeIntegrationTest`: `PURCHASE` da 16.000, conto da 1.000 a 17.000, doppio invio, gare, sessioni obsolete e rollback dopo le scritture |
| Scadenza: penale 800, rimborso 3.200 su acconto 4.000 | `PurchaseConcurrencyIntegrationTest`: tipo `PENALTY`, conto finale 49.200 da 50.000, unicità e rollback |
| Ultimo compratore | `SaleProposalIntegrationTest`: solo `SALE`, ordinamento data/ID, acquisto della concessionaria più recente ignorato, vendita a zero riconosciuta |
| Demo e isolamento | `DemoSqlValidationTest`: 32 `SALE`, 8 `PENALTY`, importi non negativi e ricaricamenti stabili; `DatabaseIsolationIntegrationTest` |
| Presentazione | Harness JavaFX sotto, sei righe rilette in una seconda JVM, tutti i tipi anche a zero e navigazione da tastiera |

## Ambiente e comandi

OpenJDK 21.0.12.1, Maven 3.9.16, PostgreSQL 16.15 e JavaFX 21.0.2 su Linux/WSL.
Riutilizzati i binari temporanei sotto `/tmp/drivehub-ticket01/tools`, senza
nuove dipendenze applicative. Cluster temporaneo su `127.0.0.1:55437`, database e
ruolo dedicati `drivehub_test`; sentinella distinta `drivehub_sentinel`.
Configurazione privata tramite variabili d'ambiente come in [testing.md](../../testing.md).
Nessuno schema/demo eseguito su un database applicativo preesistente.

```bash
mvn -o -Dtest=SaleTypeTest test
mvn -o -Ppostgres-tests -Dtest=SaleTypeTest,PurchaseAgreementIntegrationTest,SaleProposalIntegrationTest,SaleOutcomeIntegrationTest test
mvn -o -Ppostgres-tests -Dtest=SaleTypeTest,SaleOutcomeIntegrationTest test
mvn -o -Ppostgres-tests -Dtest=PriceSelectionIntegrationTest test
mvn -o -B -Ppostgres-tests clean test
DISPLAY=:97 DRIVEHUB_UI_DIR=/tmp/drivehub-ticket02/ui-final python3 docs/evidence/sale-semantics-02/run-ui.py
```

La prima prova di dominio fallisce sul vecchio insieme di tipi. Le prime prove
persistenti rilevano lo schema precedente. Una successiva esecuzione mirata trova
l'atteso negativo residuo della sessione obsoleta; dopo l'adeguamento passano
78/78 prove di dominio/cessione. La prima suite completa esegue 419 prove e trova
un solo fallimento, l'atteso `-14400.00` del registro nel test dei prezzi.
Corretto a `14400.00` con tipo `PURCHASE`, il file dei prezzi passa 10/10 prove.

Esito finale della suite completa: **419/419, zero fallimenti, errori o saltati**,
22 classi di test. [Log completo](full-suite.txt) e [conteggi dai report XML](summary.json).
Traccia delle prove: [dominio red](domain-red.txt), [flussi red](flows-red.txt),
[78 prove green](domain-outcomes-green.txt), [prezzi green](prices-green.txt),
[prima suite con atteso precedente](full-first.txt).
Nei log archiviati sono rimossi soltanto gli spazi a fine riga.

Il controllo JavaFX usa [SaleRegisterUiCheck.java](SaleRegisterUiCheck.java),
riutilizzando Robot e focus degli harness precedenti, tramite [run-ui.py](run-ui.py).
Display isolato Xvfb `:97`, 1440×1000, rendering software; fixture ricreate soltanto
nel database dedicato. Il primo controllo passa, ma l'ispezione della schermata
rileva testi troncati: le etichette sono accorciate conservando il verso economico.
Esito finale JavaFX: **UI CHECK SUCCESS**, etichette complete e importi leggibili
con riga selezionata. [Log](ui-register.txt) e [schermata definitiva](manager-ledger.png).

I cinque diagrammi correnti interessati sono rigenerati con PlantUML 1.2026.2
(`-charset UTF-8 -failfast2 -tsvg`) e ispezionati anche come PNG temporanei:
dominio delle operazioni, saldo, cessione, stati veicolo e casi d'uso staff.

## Revisione e limiti

Revisione statica indipendente con la skill `mattpocock-skills:code-review`,
confronto del working tree con `a260598`: **Standards: 0 rilievi; Spec: 0 rilievi**.
Gli esiti delle esecuzioni sono raccolti dal processo principale; i revisori non
attribuiscono il passaggio dei test alla sola presenza del codice di prova.

Nessuna conversione dei dati esistenti: è il ticket 03. Questa versione richiede
un database nuovo o sacrificabile; schema/demo non sono una migrazione.
Evidenze storiche e `Relazione_SWE` conservate. Non verificati altri sistemi
operativi, screen reader o flussi grafici oltre al registro modificato.
