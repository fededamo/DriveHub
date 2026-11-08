# Conversione dei dati delle operazioni

Questa procedura aggiorna un database popolato senza ricrearlo. È distinta da
`schema.sql` (inizializzazione/reset distruttivo) e `demo.sql` (sostituzione
distruttiva dei dati con esempi). Non usare questi due script per un aggiornamento.
Nessuna conversione viene eseguita automaticamente all'avvio.

Origine supportata: **841d4bce1dc8b5217f6f2e2a39fc42b09b5da044**,
ossia `a260598^`, precedente a entrambi i ticket semantici. Destinazione:
contratto finale con **ticket 01 (`a260598`), 02 (`f4a7f0e`) e 03 (questa
conversione)** installati insieme. I due contratti intermedi sono rifiutati.
Non è una conversione per versioni storiche arbitrarie o schemi personalizzati.

La procedura è verificata con PostgreSQL **16.15**. Il riconoscimento confronta
colonne (anche precisione e nullabilità), enum e definizioni dei vincoli di
`request` e `sale` con il contratto sorgente. Rifiuta stime sorgenti positive,
importi NaN, vincoli mancanti/aggiuntivi/non validati, trigger o regole
personalizzati sulle tre tabelle coinvolte e tabelle con sicurezza per riga.
Le stime del vecchio servizio erano non positive; il solo vecchio CHECK SQL
non ne imponeva il segno. Dati scritti fuori da quel contratto vanno esaminati,
senza correggerli automaticamente con un valore assoluto.

## Preparazione e comando

1. Fermare tutte le istanze dell'applicazione e gli altri processi che usano
   il database, mantenendoli fermi fino alla verifica finale. PostgreSQL resta
   avviato. Chiudere anche console SQL e connessioni inattive verso quel database.
2. Verificare esplicitamente server, database, ruolo proprietario delle tabelle
   e versione sorgente. Preparare un backup completo e provarne il ripristino
   su un database dedicato. Non ricavare la versione dai soli valori delle righe.
3. Provare prima l'aggiornamento e i flussi sulla copia dedicata. Conservare
   conteggi delle righe, sequenze, associazioni e saldi prima/dopo.
4. Eseguire dalla radice del repository il comando seguente, sostituendo i
   segnaposto con la destinazione verificata e usando il ruolo proprietario.
   La password è richiesta da `psql`, senza inserirla nel comando o nei file.

```bash
psql -X -W -h HOST -p PORT -U RUOLO_PROPRIETARIO -d DATABASE_VERIFICATO \
  -v ON_ERROR_STOP=1 -f src/main/resources/db/convert-operation-semantics.sql
```

Lo script contiene già `BEGIN`/`COMMIT`: non racchiuderlo in un'altra
transazione e non eseguirne singole porzioni. Richiede di essere l'unica
connessione al database e acquisisce lock esclusivi su `request`, `sale` e
`bank_account`; in caso di contesa fallisce. Il controllo delle connessioni
non sostituisce l'obbligo di mantenere ferma l'applicazione.

L'esecuzione riuscita segnala `Conversione 841d4bc -> contratto finale
completata; nessun movimento bancario`. Un database già nel contratto finale
segnala `Contratto finale già presente: nessuna modifica`, senza riscrivere
le righe. Controllare anche il codice di uscita di `psql`.

## Trasformazioni e controlli

| Entità | Valore originario | Valore finale | Importo |
|---|---|---|---|
| Request | PURCHASE | SALE | Acconto e totale concordato invariati |
| Request | SALE | PURCHASE | Stima negata, `agreed_total` resta nullo |
| Sale | PURCHASE | SALE | Invariato |
| Sale | ACQUISITION | PURCHASE | Importo negato |
| Sale | PENALTY | PENALTY | Invariato |

Il tipo originario decide la conversione, anche con importo zero. Non si
ricalcolano stime, acconti o totali dal listino. Identificativi, date, venditori,
approvazioni, scadenze, collegamenti e sequenze rimangono invariati. `agreed_total`
resta `numeric(10,2)` e il suo uso immutabile nel dominio non cambia.
Non si aggiornano i conti e non si invocano servizi applicativi di pagamento,
accredito, rimborso o penale. I vincoli finali rifiutano i nuovi importi negativi.

Prima di riavviare, confrontare righe, sequenze e saldi con la fotografia iniziale
e verificare che il database sia nel contratto finale. Installare tutti e tre
i ticket; non avviare versioni precedenti o intermedie sui dati convertiti.
La gestione ordinaria delle scadenze riparte all'accesso del cliente: i movimenti
di quel flusso avvengono dopo la conversione e devono restare distinti dal confronto
dei saldi prima/dopo lo script.

Un errore prima del commit annulla insieme dati e vincoli. Con il comando
indicato `psql` si arresta e la chiusura della connessione annulla la transazione;
in una console rimasta aperta eseguire `ROLLBACK`. Risolvere la causa e ripetere
lo script intero. Dopo un commit riuscito non applicare una conversione inversa:
per tornare alla versione sorgente occorre ripristinare il backup completo,
con applicazione ferma, prima di qualsiasi nuova operazione applicativa.

[Prove, comandi effettivamente eseguiti e limiti](evidence/data-conversion-03/verification.md).
