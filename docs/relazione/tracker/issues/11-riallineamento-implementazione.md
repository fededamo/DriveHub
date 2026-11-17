# 11 — Riallineamento dell'implementazione

Status: done

Blocked by: None.

## What to build

Un capitolo di implementazione che riprenda la progressione dell'originale:
gruppi di entità, coordinamento e responsabilità dei servizi, quattro flussi,
prezzi, autenticazione/conto, Observer, presentazione, mapping e pattern.
Riorganizzare il testo valido e recuperare spiegazioni, esempi e frammenti
significativi della stessa profondità, verificati sul codice corrente.

Riferimenti: [specifica](../spec.md), [matrice, capitolo 3](../../allineamento.md#capitolo-3),
[censimento dei frammenti](../../allineamento.md#frammenti-ed-esempi-da-conservare-per-funzione)
e [indice](../../indice.md#3-implementazione).

## Acceptance criteria

- [x] Tutte le voci 3–3.5.6 conservano ordine e funzione espositiva. Le
  invarianti trasversali sono spiegate negli argomenti cui appartengono.
- [x] Domain Model organizzato per Users, Vehicles, Rentals, Sales ed eventi;
  esplicitata l'appartenenza corrente degli eventi al package separato.
- [x] Business Layer distingue transazioni, responsabilità e quattro flussi
  applicativi, prima di prezzi, identità/conto e Observer. Ogni flusso spiega
  controlli, scritture, alternative ed esito, oltre al rinvio alle sequenze.
- [x] Formule/esempi e tabella sull'origine degli importi descrivono base
  manuale, sconti, totale fissato, saldo, noleggio e stima attuali; conservati
  i limiti documentati su connessioni, arrotondamenti e importi zero.
- [x] Presentation Layer riprende aree, costruzione/navigazione e dati/refresh,
  includendo AsyncLoad, errori, Riprova e ciclo di vita nel punto pertinente.
- [x] Persistenza articolata nei cinque argomenti originali: contratti,
  utenti/vista, operazioni/nullabilità, vincoli e connessione condivisa.
  Recuperata la funzione della tabella su garanzie e limiti.
- [x] Sei temi dei pattern riesaminati nello stesso ordine: Observer,
  calcolo senza Strategy, factory statiche, navigazione/contesto, DAO e
  coordinamento transazionale, idiomi Java. Nessuna attribuzione fittizia
  di Strategy, vecchio NavigationManager o UnitOfWork.
- [x] Ciascuno dei quattordici frammenti originali di implementazione ha un
  esempio corrente o un adattamento motivato; estratti riusati o nuovi
  confrontati esattamente con i sorgenti. Nessun obbligo di duplicare listati.
- [x] Baseline dei file coinvolti, anche non tracciati; fonti ed evidenze
  storiche conservate. Compilazione reale, controllo dei rinvii e ispezione
  visiva del capitolo; estratto e riscontri proposti agli autori.

## Comments

16 novembre 2026: terzo capitolo nell'ordine di lavorazione. Nessuna dipendenza
tecnica dal riallineamento del capitolo 2: tavole, etichette e fonti esistono
già; l'integrazione finale controllerà anche i rinvii modificati. Il ticket
documenta il software e segnala separatamente eventuali difetti, senza patch,
test applicativi nuovi, commit o pubblicazione.

17 novembre 2026: conclusione tecnica dopo tre passaggi XeLaTeX finali,
controllo dei rinvii e osservazione di tutte le pagine del capitolo e delle
accessorie pertinenti. Verificate 35 voci, quattordici frammenti originali
e 27 estratti correnti esatti; recuperate le due tabelle e tutte le funzioni
espositive. Due correzioni documentali (AbstractUser astratta e spiegazione
SQLSTATE23503) sottoposte a ricontrollo indipendente Standards/Spec, senza
rilievi aperti. [Confronto, fonti, PDF, estratto e verbali](../../verifiche/ticket-11/README.md).
Capitoli 1/2, altre parti, diagrammi e storici conservati; nessun vecchio
manifest riscritto. Restano cinque messaggi TeX preesistenti in C, osservati
e non attribuiti al ticket 11. Nessuna prova applicativa/DB o modifica al
software, nessun commit/invio/pubblicazione. Ticket 12/13 e approvazione
degli autori restano distinti da questa chiusura.
