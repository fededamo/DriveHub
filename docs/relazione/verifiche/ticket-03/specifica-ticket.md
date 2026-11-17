# Ticket 03 — Package, classi e architettura

Specifica operativa ricevuta nella conversazione del 12 novembre 2026.
Questo verbale ne conserva il perimetro per i revisori; non proviene da
un ticket prepubblicato o da materiali temporanei.

Completare 2.5 Package Diagram, 2.6 Class Diagram (Domain Model, Business
Layer, DAO, Presentation Layer) e appendice B, mantenendo titoli e ordine.
Riscontrare sui sorgenti di produzione correnti struttura, responsabilità,
collaborazioni e limiti; la vecchia relazione serve soltanto per struttura.

Usare la skill draw.io per un package diagram con tutti gli undici package
reali e viste delle classi riconoscibili per ciascuno: root, domain.user,
domain.vehicle, domain.discount, domain.operation, service, event, dao,
dao.postgres, presentation.view, security (prefisso it.unifi.drivehub).
Verificare questo elenco prima dell'uso. Nessuna tavola vuota per package
intermedi privi di tipi. Distinguere avvio/composizione, dominio, servizi,
eventi, persistenza, presentazione e sicurezza, con direzione reale delle
dipendenze, comprese quelle trasversali significative.

Diagrammi progettuali selettivi, non esportazioni UML automatiche: tipi e
membri utili a responsabilità, collaborazioni e invarianti, senza elenchi
indiscriminati di getter/setter. Relazioni, navigabilità e cardinalità
devono essere giustificate. Distinguere oggetti da ID (clientId, vehicleId,
modelId); non dedurre associazioni o composizioni dalla sola presenza di
un campo/iniezione. Aggiungere legenda quando necessaria.

Spiegazione sintetica e argomentata su App/AppContext, servizi, contratti
DAO e PostgreSQL, eventi, navigazione per ruolo e sicurezza. Collegare ai
casi già documentati quando utile, senza anticipare il capitolo Implementazione.
Corpo con viste selezionate e rimandi coerenti; appendice B con tavole estese
e indicazioni di lettura, identificativi e nomi stabili. Conservare sorgenti
modificabili .drawio ed esportazioni usate sotto docs/relazione/diagrammi.
Ogni esportazione deve contenere l'intera tavola senza tagli/suddivisioni.

Registrare in verifiche/ticket-03 una matrice package → tavola → tipi →
sorgenti, motivando omissioni significative. Integrare LaTeX, compilare
realmente, aprire e osservare tutte le nuove tavole nel PDF, indice,
didascalie/riferimenti, appendice B e confini provvisori. Concludere due
revisioni indipendenti Standards e Spec, correggere rilievi documentali
e ripetere soltanto controlli interessati. Non attribuire esiti a tentativi
interrotti. Registrare comandi, ambiente, data, fonti, esiti e impedimenti.

Conservare capitolo 1, 2.1–2.4, appendice A, screenshot e diagrammi dei
ticket completati, senza dichiarare già ottenuta l'approvazione degli autori.
Mantenere provvisori 2.7–2.8, capitoli 3–5 e appendice C. Ammesse integrazione
e aggiornamento dello stato. Preservare integralmente software, SQL, dati,
vecchia relazione, evidenze e manifest precedenti. Non modificare i manifest
01/02 per adattarli al nuovo PDF; creare verifiche aggiornate 03.

Nessun commit, test applicativo nuovo, inizializzazione DB o acquisizione UI.
Verificare strumenti realmente disponibili e separarli dalle dipendenze app.
Segnalare separatamente eventuali difetti applicativi, senza correggerli.
Alla fine collegare PDF, LaTeX, diagrammi e verifiche e proporre 2.5–2.6/B
alla revisione degli autori.
