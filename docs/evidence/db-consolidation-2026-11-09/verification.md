# Consolidamento e verifica dell'installazione locale

Data: 9 novembre 2026. Baseline: `d31a1ce`, albero iniziale pulito.
L'utente ha autorizzato l'aggiornamento reale, la cancellazione completa dello
script anche dai test e il mantenimento di un solo contenitore `drivehub-db`,
chiedendo di verificare quale archivio fosse più recente.

## Scelta e conservazione dei dati

Erano presenti due archivi distinti, entrambi arrestati:

| Archivio | Creazione | Richieste | Registro | Struttura |
|---|---|---:|---:|---|
| `drivehub-db` | 29 ottobre | 13 | 41 | Precedente ad accordi e tipi del registro |
| `drivehub-quickstart-db` | 3 novembre | 14 | 40 | Contratto `841d4bc`, riconosciuto dalla conversione |

La copia più recente per struttura non includeva una scadenza già elaborata
nell'altro archivio. Il confronto completo ha isolato una sola richiesta rimossa,
una sola operazione aggiunta, un saldo e uno stato veicolo differenti. Gli altri
campi comuni di richieste e registro, anagrafiche, modelli, sconti, noleggi e test
drive coincidevano. I 56 prezzi di base della copia recente sono stati conservati
insieme al suo `price_mode`; non sono stati sostituiti con i vecchi valori.

L'operazione aggiuntiva `#41` è stata riconosciuta come esito della scadenza della
richiesta `#1`: stesso cliente, data successiva alla scadenza, veicolo/venditore
nulli nel registro, penale pari al 20% dell'acconto, differenza sul conto pari
esattamente al rimborso restante, veicolo tornato `SALE`. La classificazione non
è stata dedotta dal segno. Sono stati conservati data, identificativo, saldo già
rimborsato e richiesta già rimossa, senza richiamare servizi di pagamento.

Entrambi gli archivi sono stati salvati in formato `pg_dump -Fc`, con fotografie
private e metadati dei contenitori, fuori dal repository:
`/home/federico/dev/Drivehub/.drivehub-backups/2026-11-09/`.
Directory con permessi 700 e file con permessi 600. Nessun dump, credenziale,
anagrafica o saldo individuale è stato aggiunto al repository.

Entrambi i backup sono stati realmente ripristinati in database separati di un
contenitore temporaneo. Verificate tutte le righe e le sequenze. Il ripristino
PostgreSQL ha riscritto tre CHECK con cast equivalenti: sulla sola copia è stata
ricreata la sintassi sorgente riconosciuta, con vincoli validati, prima della
prova di conversione. Gli originali sono rimasti invariati fino al termine della
prova. Un errore SQL provocato dopo le modifiche e i nuovi vincoli ha dimostrato
il rollback integrale, compreso il contatore del registro.

Consolidamento dell'esito già elaborato e conversione sono stati poi applicati
alla copia recente in una sola transazione, con lock e confronto integrale
delle righe sorgenti prima delle scritture. La sequenza del registro conserva
il valore 41 già raggiunto nell'archivio con l'operazione aggiuntiva.

Risultato: **13 richieste** (`SALE` 9, `PURCHASE` 4) e **41 operazioni**
(`SALE` 32, `PENALTY` 9). I saldi coincidono con la copia dove la scadenza era
già stata gestita; nessun addebito, accredito o rimborso è stato ripetuto.
[Riepilogo e hash dei backup](database-checks.json).

Il vecchio contenitore è stato rimosso senza cancellarne il volume. La copia
consolidata è stata rinominata **`drivehub-db`**, mantenendo il proprio volume
`drivehub-quickstart-data` e la porta locale **55432**. Salvato anche
`drivehub-final.dump` nella directory privata. Dopo tutte le prove, un confronto
in sola lettura ha confermato ogni riga e ogni sequenza:
[esiti finali](final-data-checks.json).

## Rimozione richiesta e verifiche correnti

Rimossi completamente `convert-operation-semantics.sql`, il test dedicato,
il relativo helper e le due fixture sorgenti. Rimosso anche l'overload del
loader che serviva a caricarle. Le evidenze storiche sono immutate e restano
riproducibili dal checkout `d31a1ce`. Il nuovo harness usa dati nel contratto
finale, senza contenere o invocare una conversione.

Aggiornati `run.ps1`, README, installazione, testing e stato dell'aggiornamento.
La variazione nel launcher è soltanto il nome del contenitore; rimangono il
controllo dell'etichetta, la protezione del volume e il marchio di inizializzazione.

Ambiente di verifica: OpenJDK 21.0.12.1, Maven 3.9.16, PostgreSQL 16.15,
JavaFX 21.0.2 su Linux/WSL. Per la suite è stato creato un cluster nativo
temporaneo sulla porta 55439, con ruolo/database non amministrativi
`drivehub_test` e copia sentinella distinta `drivehub_sentinel`. Nessun test
ha usato il database applicativo.

```bash
mvn -o -B -Ppostgres-tests clean test
DISPLAY=:98 DRIVEHUB_UI_DIR=/tmp/drivehub-post-conversion/ui python3 docs/evidence/db-consolidation-2026-11-09/run-ui.py
mvn -o -B -DskipTests package
git diff --check
```

- **419/419 test**, zero fallimenti, errori o saltati, 22 classi, in 1 minuto e
  15 secondi. [Log completo](full-suite.txt), [conteggi XML](summary.json).
  I 25 test ritirati sono esclusivamente quelli dello script eliminato;
  i test del contratto finale, dei flussi e della concorrenza restano presenti.
- **UI CHECK SUCCESS**, con [harness corrente](FinalContractUiCheck.java),
  [launcher](run-ui.py) e [log](ui.txt). Tre dashboard reali, navigazione con
  Tab/frecce, cessione con accredito unico, scadenza, accordo 20.000/4.000/16.000
  dopo cambio listino e registro con tipi espliciti anche a zero.
  [Conferma saldo](client-final-confirmation.png),
  [registro finale](manager-final-ledger.png).
- **Build del JAR riuscita**, controllata anche l'assenza di risorse di
  conversione o fixture sorgenti nel pacchetto. [Log build](package.txt).
- **Parser PowerShell Windows e controlli launcher riusciti**: etichetta del
  contenitore, porta, disponibilità PostgreSQL e marchio `version=1` verificati.
  [Log](launcher.txt).

Il primo tentativo Maven si era fermato prima dei test per i collegamenti assoluti
del JDK estratto: corretti soltanto sotto `/tmp`. L'interoperabilità Windows ha
poi restituito errori `UtilAcceptVsock`; il socket della sessione era diventato
inutilizzabile. Usato il socket WSL funzionante per completare verifica reale,
parser Windows e pulizia. [Tentativo precedente](launcher-attempt.txt).
Il confronto finale tratta le righe come insiemi, senza dipendere dall'ordine
con cui vengono restituite.

Al termine sono stati rimossi il contenitore temporaneo di ripristino e tutte
le copie operative temporanee dello script; PostgreSQL di test e Xvfb sono
arrestati. Rimane attivo soltanto **`drivehub-db`**. Il vecchio volume resta
inattivo come ulteriore protezione dei dati.

Non avviata una sessione completa dell'app Windows sui dati reali, per evitare
che il login gestisse altre scadenze e alterasse il confronto dei saldi. I flussi
grafici sono stati eseguiti su dati dedicati. Il limite UI preesistente della
riga scaduta visibile fino al refresh rimane documentato nelle prove precedenti.
Non verificati altri sistemi operativi o screen reader. `Relazione_SWE` e le
evidenze storiche sono conservate integralmente.
