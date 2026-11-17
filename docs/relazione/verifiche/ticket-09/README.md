# Ticket 09 — Riallineamento dell'introduzione

16 novembre 2026. Ticket concluso tecnicamente: verifica documentale e
[review indipendenti Standards e Spec](revisione.md) completate senza rilievi.
Il capitolo è proposto agli autori per revisione, non approvato.

- [PDF completo ricompilato](../../relazione-drivehub.pdf), 113 pagine.
- [Estratto del capitolo 1](capitolo-1-per-revisione.pdf), tre pagine.
- [Confronto di tutti i blocchi e fonti](confronto.md).
- [Controllo visivo e messaggi TeX](controllo-visivo.md).
- [Comandi effettivi, ambiente e limiti](comandi.md).

## Modifica e perimetro

Mantenuti tutti i testi validi e la scansione Introduzione → Descrizione →
Stack tecnologico → Semplificazioni adottate. Unica modifica al capitolo:
il punto sull'ambiente di esecuzione precede quello sui processi aziendali,
rispettando il percorso dell'originale. Nessuna riga aggiunta o eliminata.
La matrice puntuale verifica contenuti e motivazioni degli adattamenti già
presenti: tre ruoli, catalogo comune, conto simulato, registrazione dei soli
clienti, personale predisposto, JavaFX in Java, JDBC/PostgreSQL, avvio e limiti.

L'originale non contiene figure, tabelle, schede o listati da recuperare.
Nessun diagramma creato, adattato o esportato: le esportazioni esistenti
sono input della compilazione. Gli undici rifacimenti e le basi degli autori
rimangono al ticket 10, con skill `drawio:drawio` e vincoli della matrice.
Capitoli 2–5, appendici e tavole restano intatti. Aggiornati soltanto gli
stati di README, piano, indice, matrice e ticket 09 dopo le review.

## Baseline e conservazione

Prima di modificare: HEAD `61ce18ae1d4e0458b63e970b793b0b3a634a3adf`,
nessuna modifica tracciata e 468 file non tracciati. Stato completo in
`stato-iniziale.txt`. `baseline.json` censisce 2.430 file con dimensioni,
SHA-256 e stato tracciato/non tracciato, inclusi gli storici eventualmente
ignorati di `Relazione_SWE`, `docs/evidence` e delle verifiche precedenti.
`baseline-editoriale.tar.gz` contiene 217 copie dei materiali editoriali
coinvolti, originale e PDF precedente inclusi; impronta separata nel file
`baseline-editoriale.sha256`. La baseline non è stata rigenerata. `SHA256SUMS` identifica i sette file
aggiornati e le nuove evidenze; si verifica dalla radice del repository con
`sha256sum -c docs/relazione/verifiche/ticket-09/SHA256SUMS`.

Il [controllo ripetibile](verifica-documenti.py) confronta tutti i file
censiti e i nuovi file, non soltanto il diff Git. Produce
`delta-documentale.diff`, `delta-files.json`, `confronto-pdf.json` ed
`esito-documentale.json`. Originali, software, schema, dati demo, ADR,
glossario ed evidenze pregresse sono conservati byte per byte.

## Verifiche effettive

- Confronto di 17 blocchi originali con la nuova stesura e riscontro statico
  delle affermazioni; 29 fonti tecniche identificate da impronta in `fonti.json`.
- Compilazione integrale con XeLaTeX / TeX Live 2025/Debian in tre passaggi
  finali riusciti: `compilazione-1.txt`–`compilazione-3.txt`. Motore e librerie
  in `/tmp`; nessuna installazione di sistema. Conservati anche i tre passaggi
  precedenti al riordino, prefisso `prima-`, e i tentativi di predisposizione.
- 62 riferimenti espliciti risolti, etichette del capitolo conservate,
  indice e segnalibri verificati; nessun riferimento indefinito, richiesta
  di ulteriore passaggio o messaggio di box troppo largo/stretto nel log finale.
- Ispezione visiva delle pagine fisiche 4–6 (numerate 1–3), dell'indice
  fisico 2–3 (i–ii) e delle cinque pagine associate ai messaggi TeX residui;
  rendering a 108 dpi in `pagine/`.
- PDF completo confrontato con il PDF acquisito nella baseline: 112 delle
  113 pagine identiche nel testo e nel rendering a 72 dpi. Cambia soltanto
  la pagina fisica 6, corrispondente al riordino. Indice e segnalibri identici.
  Estratto di tre pagine confrontato con le pagine del PDF completo.

## Limiti e consegna agli autori

Restano cinque messaggi `ignored error: Infinite glue shrinkage` nelle
tabelle dell'appendice C, pagine numerate 104–108 (fisiche 107–111).
Sono preesistenti: quelle pagine sono identiche alla baseline e sono state
osservate anche in questa verifica. Non interessano il capitolo 1; la
compilazione riesce, ma non viene dichiarato un log completamente pulito.

Nessun test applicativo, build Java, avvio UI, accesso DB o harness eseguito.
Le affermazioni tecniche sono riscontri sul codice e sulla documentazione,
non nuove prove di comportamento. Tectonic, Windows e macOS non verificati.
Nessun commit, invio o pubblicazione. Gli ZIP precedenti restano storici;
il pacchetto complessivo e il riallineamento degli altri capitoli appartengono
ai ticket 10–13. La revisione e l'approvazione degli autori restano da svolgere.
