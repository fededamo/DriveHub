# Ticket 04 — Sequenze e database

Specifica operativa ricevuta nella conversazione il 12 novembre 2026.
Questa copia rende la revisione riproducibile senza dipendere da un ticket
già pubblicato o da materiali in `/tmp`. La richiesta in conversazione
prevale in caso di differenze. Riferimento generale: [spec.md](../../tracker/spec.md).

## Obiettivo e struttura

Completare 2.7–2.8 della relazione LaTeX corrente, chiudendo il capitolo 2
“Analisi dei requisiti e progettazione”. Il capitolo sarà completo e
proposto per revisione degli autori, senza attribuirgli approvazione umana.
I ticket 01–03 sono conclusi e revisionati indipendentemente.

Conservare titoli e ordine:

- 2.7 Sequence Diagram: Acquisto di un veicolo; Prenotazione di un noleggio;
  Acquisizione di un veicolo del Customer.
- 2.8 Database: Schema ER; Modello relazionale; Utenti, vincoli e semplificazioni.

Terminologia: vendita al cliente e acquisto dal cliente, detto anche
cessione del cliente. I titoli storici non autorizzano componenti rimossi.

## Contenuti richiesti

1. Leggere `docs/AGENTS.md` e istruzioni richiamate, specifica, indice, piano,
   glossario, README della relazione, README/revisione dei ticket 02 e 03,
   `ticket-03/relazioni.md`, architettura, ADR pertinenti e schema corrente.
2. Riscontrare i tre flussi sui sorgenti di produzione: vendita al cliente
   (avvio, acconto, approvazione/rifiuto, saldo, scadenza); noleggio
   (prenotazione, disponibilità, addebito, cancellazioni test drive);
   acquisto dal cliente (proposta, esito, registrazione economica).
   Vecchia relazione usata solo per struttura e riconoscimento dei flussi;
   riutilizzare casi d'uso e identificativi documentati.
3. Diagrammi con skill draw.io, riconoscibili e leggibili, più tavole solo
   quando utile. Spiegare notazione, principale e alternative significative.
4. Partecipanti e messaggi corrispondenti a tipi/chiamate reali; distinguere
   attore, handler, servizi, DAO, DB, oggetti in memoria e ID. Mostrare
   confini transazionali, riuso reale della Connection, commit, rollback,
   propagazione errori. Observer sincrono nel punto reale. Letture autonome
   fuori dalla transazione di scrittura. Nessun lock, permesso, snapshot o
   garanzia di concorrenza non provati; scadenze solo all'innesco effettivo.
   Etichette di sintesi ammesse se distinguibili da metodi Java.
5. Testo sintetico su responsabilità, accordi/importi, richiesta rispetto
   a registro, alternative, garanzie e limiti; collegare ai casi d'uso,
   senza anticipare il capitolo Implementazione o parafrasare ogni riga.
6. Ricostruire dal DDL PostgreSQL attuale ER e viste relazionali draw.io,
   coprendo tutte le tabelle, PK/FK/candidate, cardinalità, nullabilità,
   partecipazione minima/massima, DELETE delle FK, UNIQUE/CHECK/EXCLUDE,
   quattro enum nativi e significati, importi, accordo, storico, tempi e
   disponibilità. Confrontare DAO/dominio dove necessario; nessuna
   derivazione automatica dalle classi o schema export come risultato.
7. Distinguere vincoli SQL da regole applicative. Descrivere identità,
   ruoli e conto dimostrativo; “utenti” applicativi distinti dai ruoli PG.
   Nessuna credenziale personale o valore sensibile di configurazione.
   Motivare scelte e limiti correnti, rispettare ADR 0001/0002, nessuna
   migrazione, conversione o proposta di schema ideale sostitutivo.
8. Conservare `.drawio` modificabili ed esportazioni usate sotto
   `docs/relazione/diagrammi/`, nomi/ID stabili senza sovrascrivere precedenti.
   Ogni export deve contenere tutta la tavola senza tagli o fogli accidentali.
9. In `verifiche/ticket-04/`: matrice sequenze–partecipanti/messaggi–sorgenti;
   matrice tavole dati–tabelle/relazioni/vincoli–SQL; omissioni motivate;
   comandi, ambiente, data, fonti, risultati, impedimenti. Separare statico,
   storico e nuove esecuzioni. Un test esistente non prova ogni messaggio.
10. Integrare LaTeX e compilare realmente. Aprire il PDF e osservare tutte
    le nuove tavole (testi, frecce, simboli, cardinalità, dimensioni), indice,
    didascalie/riferimenti, completamento 2.7–2.8 e conservazione precedente.
    Capitoli 3–5 e C rimangono provvisori; aggiornare lo stato necessario.
11. Skill code-review con due revisori indipendenti e separati: Standards
    e Spec. Concludere entrambe le revisioni prima di dichiarare concluso;
    correggere rilievi documentali e ripetere solo controlli interessati;
    nessun successo attribuito a revisioni interrotte.

## Perimetro, verifica e consegna

Solo documentazione del ticket 04. Software applicativo, SQL, dati,
vecchia relazione, evidenze precedenti invariati. Nessun commit né test
applicativo nuovo. Conservare capitolo 1, 2.1–2.6, A–B, screenshot e
diagrammi completati; soli cambi necessari per integrazione/stato.

Usare riscontro statico di sorgenti/schema, verifica delle relazioni,
compilazione LaTeX e osservazione PDF. Nessuna inizializzazione/query
operativa, acquisizione UI o suite applicativa. Verificare strumenti reali,
eventuale ripreparazione separata dalle dipendenze applicative.
Manifest ed evidenze 01–03 intatti, senza rieseguire controlli storici che
pretendano provvisorie sezioni ora completate. Segnalare separatamente
difetti applicativi emersi, senza correggerli. Procedere autonomamente
sui punti decisi. Consegna con link a PDF, LaTeX, diagrammi e verifiche,
presentando 2.7–2.8 e il capitolo 2 agli autori per revisione.
