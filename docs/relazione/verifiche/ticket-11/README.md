# Ticket 11 — Riallineamento dell'implementazione

17 novembre 2026. **Ticket 11 concluso tecnicamente** dopo compilazione,
controllo visivo e review indipendenti Standards/Spec: nessun rilievo aperto.
Le due correzioni documentali sono ricontrollate nei sorgenti e nel PDF;
i rapporti iniziali restano distinti dai verbali conclusivi.
La revisione degli autori resta distinta e ancora da svolgere.

- [PDF completo aggiornato](../../relazione-drivehub.pdf), 143 pagine.
- [Estratto del capitolo 3](capitolo-3-per-revisione.pdf), 23 pagine, fisiche 83–105.
- [Confronto originale/nuovo](confronto.md), [35 corrispondenze strutturali](copertura-strutturale.json).
- [Copertura dei quattordici frammenti originali](copertura-frammenti.json),
  [27 estratti letterali con fonti/intervalli](frammenti.json) e [pagine PDF](pagine-frammenti.json).
- [Fonti tecniche e confini](fonti-tecniche.md), [72 impronte](fonti.json), [esempi ricalcolati](esempi-importi.json).
- [Verifiche ripetibili](verifica.py), [risultato](esito-documentale.json),
  [output dei controlli](controlli-documentali.txt), [comandi e strumenti](comandi.md).
- [Verbale visivo e messaggi TeX](controllo-visivo.md), [log conclusivo](compilazione-review-3.txt).
- [Conservazione nel PDF dei capitoli 1/2](conservazione-pdf.json),
  [delta dal working tree iniziale](delta-files.json), [diff editoriale](delta-documentale.diff).
- [Conservazione dei capitoli 4/5 e delle appendici](conservazione-altre-parti.json).
- [Limiti applicativi separati](difetti-applicativi.md), senza patch.

- [Review Standards/Spec e ricontrolli delle correzioni](revisione.md).
- [Manifest finale](SHA256SUMS) e [verifica di chiusura](finalizzazione.txt).

## Risultato documentale

Cinque sezioni, venticinque sottosezioni e quattro sottosottosezioni,
stessi livelli e ordine dell'originale fino a 3.5.6. Recuperati i cinque
gruppi Domain Model; coordinamento, responsabilità e quattro flussi;
formule/esempi e tabella origine importi; tre responsabilità della UI;
cinque argomenti DAO e tabella garanzie; sei temi dei pattern. Il testo
valido e tutti i tredici estratti precedenti sono riusati; quattordici
estratti aggiunti per coprire le funzioni originali e gli adattamenti.

Letture autonome distinte da quelle sulla Connection transazionale;
due callback reali, commit/rollback/close, causa e soppressa; prezzi
concreti senza Strategy, accordo persistito, stima, zero e arrotondamenti;
AsyncLoad e ciclo di vita, scritture ancora sincrone e refresh dopo commit;
mapping, tipi, NULL, limiti di ID/email e assenza identity map/UnitOfWork.
Le tavole del ticket 10 sono riusate con gli stessi identificativi.

## Baseline e conservazione

[baseline.json](baseline.json): 3.277 file censiti prima delle modifiche,
inclusi non tracciati, ignorati, PDF e storici; [stato Git](stato-iniziale.txt).
[Archivio iniziale](baseline-editoriale.tar.gz) di 223 copie editoriali,
con [impronta](baseline-editoriale.sha256). Non vengono rigenerati.
HEAD iniziale `61ce18ae1d4e0458b63e970b793b0b3a634a3adf`.
Il [punto fisso della review](punto-fisso-review.txt) è il working tree
iniziale: il solo diff Git è vuoto e insufficiente.

Capitoli 1 e 2 identici nel testo e nei pixel su tutte le 78 pagine;
slittano fisicamente di una pagina per l'indice più lungo. Tutti i loro
sorgenti, appendici, 43 coppie diagrammi, originali degli autori,
screenshot, software, schema, dati, glossario, ADR e vecchie verifiche
restano intatti. La nuova composizione sposta le parti successive di
quattordici pagine; non riscrive i contenuti estranei. Nessuna appendice
modificata richiede un estratto nuovo. Nessun vecchio manifest è riscritto.

## Limiti

Cinque messaggi TeX preesistenti nell'appendice C, osservati sulle nuove
pagine 137–141 e non dichiarati eliminati. Zero nuovi box difettosi,
rinvii irrisolti o caratteri mancanti nel log finale. I difetti applicativi
restano riscontri statici separati, senza riproduzione o patch.

Nessuna build Java, test applicativo, UI, DB, Docker o harness; nessun
commit, invio o pubblicazione. Toolchain temporanea estratta e venv,
senza installazioni di sistema; non verificati Tectonic, Windows/macOS
o un nuovo ZIP autonomo (ticket 13). Testing da riallineare nel ticket 12.
Il documento è proposto agli autori, senza attribuirne l'approvazione.
