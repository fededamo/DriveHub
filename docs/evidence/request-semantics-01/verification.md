# Ticket 01 — Richieste dal punto di vista della concessionaria

Verifica dell'8 novembre 2026, sulla base `841d4bce1dc8b5217f6f2e2a39fc42b09b5da044`.
Fonti: specifica e sola sezione 01 della proposta in
`TRACKER_DRIVEHUB/semantica-operazioni/`, esterno al repository applicativo.
Nessun file individuale di ticket presunto o creato.

## Contratto consegnato

`RequestType.SALE` rappresenta la vendita della concessionaria al cliente:
`deposit` è l'acconto non negativo, `agreed_total` è il totale non negativo,
con acconto non superiore al totale. `RequestType.PURCHASE` rappresenta la
cessione del cliente: `deposit` contiene la stima non negativa e `agreed_total`
resta nullo. Le factory seguono gli stessi nomi; gli input negativi vengono
rifiutati nel dominio e nei vincoli SQL, senza valore assoluto.

Servizi, query, mapper, demo, fixture, dashboard e diagrammi correnti seguono
questo contratto. I servizi e i testi rivolti al cliente descrivono ancora la
sua azione. L'accordo 20.000/4.000/16.000 rimane invariato al cambiare del listino.

Il registro `Sale` conserva `PURCHASE` per la vendita al cliente,
`ACQUISITION` negativa per l'acquisizione e `PENALTY`. Per una stima di 16.000 €,
il cliente riceve una sola volta 16.000 € e il registro salva ancora -16.000 €.
Nessuna conversione dei dati o modifica dei contratti dei ticket 02/03.

## Ambiente isolato

OpenJDK 21.0.12.1, Maven 3.9.16, PostgreSQL 16.15, Linux/WSL e JavaFX 21.0.2.
Il JDK predefinito era 17: il primo tentativo non ha potuto compilare per
`invalid target release: 21`. Gli strumenti sono stati estratti sotto
`/tmp/drivehub-ticket01/tools`, senza installazioni di sistema.

Cluster nuovo in `/tmp/drivehub-ticket01/pgdata`, solo loopback sulla porta 55437.
Database/ruolo `drivehub_test` dedicati e senza privilegi amministrativi;
sentinella separata `drivehub_sentinel`, inizializzata soltanto nel cluster nuovo.
Nessun collegamento ai dati applicativi preesistenti. Password e configurazione
temporanee non sono incluse nelle evidenze.

Xvfb :96, 1440×1000, senza listener TCP o socket Unix su filesystem.
La sola copia temporanea del binario usa `/tmp/xkb` al posto di `/usr/bin` per
trovare xkbcomp estratto, senza scrivere in directory di sistema.
Le prove UI e la suite PostgreSQL sono state eseguite in sequenza.
Al termine cluster e Xvfb sono stati arrestati; i rispettivi lock sono stati
verificati assenti. I file temporanei degli strumenti restano soltanto in `/tmp`.

## Prove eseguite

Con JDK 21 e variabili del database dedicato configurati secondo
[testing.md](../../testing.md):

```bash
mvn -o -Dtest=RequestAgreementTest test
mvn -o -Ppostgres-tests -Dtest=RequestAgreementTest,PurchaseAgreementIntegrationTest,PurchaseConcurrencyIntegrationTest,SaleProposalIntegrationTest,SaleOutcomeIntegrationTest,BalanceConcurrencyIntegrationTest,PriceSelectionIntegrationTest,DemoSqlValidationTest test
mvn -o -B -Ppostgres-tests clean test
```

| Prova | Esito e riscontro |
|---|---|
| Dominio | 9/9, [log](domain.log): tipi, stima positiva, rifiuto dei negativi, zero esplicito e accordo immutabile. |
| Selezione mirata | 201/201, [log](targeted.log): servizi pubblici, mapping, query filtrata, vincoli SQL, demo, lock, rollback e duplicati. |
| Suite completa finale | **406/406**, **49 senza DB / 357 PostgreSQL**, zero failure/errori/salti; [log](full.log), [riepilogo per classe](summary.json). Include la prova sentinella. |
| JavaFX: nuovi tipi | [Log](ui-requests.log), [venditore](seller-request-types.png), [cliente](client-request-filter.png): etichette, stima positiva, totale/acconto/saldo, filtro cliente e tastiera. |
| JavaFX: accordo | [Log](ui-agreement.log): approvazione e saldo da tastiera, accordo 20.000/4.000/16.000 dopo cambio listino e nuovo preventivo separato. |
| JavaFX: esiti cessione | [Log](ui-outcomes.log): accettazione, sessione obsoleta senza doppio accredito, rifiuto con/senza storico, conto cliente e registro manager nel contratto precedente. |

Il ciclo RED/GREEN ha prima rilevato le factory con le vecchie firme, poi i
disallineamenti di schema/flussi e infine la vista venditore che tentava di
calcolare il saldo di una cessione. [Estratti RED](red-excerpts.log).
Le fixture residue del vecchio contratto sono state aggiornate prima del GREEN.
Nei log salvati sono stati rimossi soltanto gli spazi finali delle righe.

Per rieseguire le prove grafiche su un display e database dedicati già pronti:

```bash
DISPLAY=:96 DRIVEHUB_UI_DIR=/tmp/drivehub-ticket01/ui-final python3 docs/evidence/request-semantics-01/run-ui.py
mkdir -p /tmp/drivehub-ticket06-ui
DISPLAY=:96 DRIVEHUB_UI_DIR=/tmp/drivehub-ticket01/ui-agreement DRIVEHUB_UI_CLASS=it.unifi.drivehub.AgreementUiCheck python3 docs/evidence/request-semantics-01/run-ui.py
DISPLAY=:96 DRIVEHUB_UI_DIR=/tmp/drivehub-ticket01/ui-outcomes python3 docs/evidence/r05-sale-outcome/run-ui.py
```

Gli harness precedenti sono riutilizzati senza riscriverne sorgenti o evidenze
storiche. Il launcher nuovo compila i controlli e avvia seed/check in JVM distinte.
L'harness dell'accordo salva ancora le sue schermate nel percorso storico in
`/tmp`; il nuovo controllo salva quelle qui allegate nella directory scelta.

I quattro diagrammi correnti interessati sono stati rigenerati con PlantUML
1.2026.2, verificato con l'hash già documentato dal progetto
`7a3eacbccd08311f14b107b1254e179adc1f81fa8bd52bbaf563a37f00ea026f`:

```bash
java -Djava.awt.headless=true -jar "$PLANTUML_JAR" -charset UTF-8 -failfast2 -tsvg docs/diagrams/current/class-domain-workflows.puml docs/diagrams/current/sequence-purchase-outcomes.puml docs/diagrams/current/sequence-sale-payment.puml docs/diagrams/current/sequence-purchase-proposal-approval.puml
```

## Revisione

Due revisioni statiche indipendenti sul diff rispetto alla base indicata:

| Standard | Specifica |
|---|---|
| Nessuna violazione documentata o smell azionabile. Architettura, dipendenze, lock e transazioni conservati. | Nessun rilievo aperto sul ticket 01. Verificati i due tipi, importi, accordo, chiamanti e separazione dal registro. |

Le descrizioni residue del vecchio tipo in `docs/testing.md` sono state corrette
durante la revisione. `git diff --check` non segnala problemi.

## Limiti

Versione intermedia per database nuovi o sacrificabili. Per aggiornare dati già
popolati servono anche i ticket 02 e 03: schema/demo non sono una migrazione.
Nessuna verifica su Windows nativo, altri sistemi grafici o screen reader;
nessuna pubblicazione remota. Le prove mirate UI non certificano ogni schermata.
