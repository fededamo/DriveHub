# Ticket 10 — Riallineamento di requisiti e progettazione

16 novembre 2026. **Ticket 10 concluso tecnicamente** dopo compilazione,
controllo visivo e review Standards/Spec indipendenti: zero rilievi aperti.
I tre P2 Spec iniziali sono stati corretti e ricontrollati; rapporti iniziali
e verbali di chiusura restano distinti e disponibili.
La revisione degli autori resta da svolgere e non è attribuita a questa attività.

- [PDF completo aggiornato](../../relazione-drivehub.pdf), 129 pagine.
- [Estratto capitolo 2 e appendici A–B](capitolo-2-appendici-A-B.pdf), 96 pagine.
- [Confronto originale / nuova stesura](confronto.md), [47 corrispondenze strutturali](copertura-strutturale.json).
- [Undici tavole: basi, adattamenti, fonti, nativi, PDF e pagine](tavole.md).
- [ZIP delle undici tavole: 11 nativi e 11 PDF A3](tavole-ticket-10.zip).
- [Controlli ripetibili e risultati](esito-documentale.json), [comandi e ambiente](comandi.md).
- [Controllo visivo e messaggi TeX](controllo-visivo.md), [review Standards/Spec](revisione.md).
- [Impronte finali di PDF, sorgenti ed evidenze](SHA256SUMS).

## Contenuto e conservazione

Otto sezioni e stesso percorso interno: sei schede complete nel corpo in
ordine UC-01, UC-02, UC-03, UC-05, UC-04, UC-06; in A restano le nove
aggiuntive. Le diciotto funzioni UI hanno spiegazioni e screenshot attuali,
con viste condivise, dialoghi e funzioni assenti espliciti. Quattro class
diagram d'insieme nel corpo; B conserva dodici dettagli sugli undici package.
Tre sequenze principali d'insieme affiancate dai dettagli esistenti. ER,
legenda e tredici relazioni testuali con tutti gli attributi/chiavi/nullabilità.
Prezzi, accordi, enum, identità, transazioni e limiti attuali conservati.

Gli undici diagrammi originali hanno un corrispondente nella stessa sezione:
quattro copie/adattamenti delle basi native degli autori e sette ricostruzioni
UML da PDF e PlantUML originali, con palette, notazione e struttura riconoscibili.
`confronto-tavole.json` documenta gli ID/etichette delle quattro basi;
le tre copie già corrette sono confrontate anche con la baseline precedente.
Non è una generazione di UML dal codice. In totale 43 coppie native/PDF:
le 32 tavole di dettaglio esistenti sono conservate byte per byte.

Capitolo 1 intatto anche nel PDF, confrontato a testo e pixel sulle tre pagine.
Originali, screenshot, software, schema, dati, glossario, ADR e verifiche
precedenti intatti. Capitoli 3 e 5 invariati; in 4 e C cambia solo il rinvio
alle schede spostate. B riceve il raccordo alle quattro viste del corpo.
Nessun vecchio manifest riscritto; gli ZIP precedenti restano storici.

## Baseline e verifica

`baseline.json`: 3.053 file censiti prima delle modifiche, inclusi materiali
non tracciati e ignorati; `stato-iniziale.txt` conserva lo stato Git.
`baseline-editoriale.tar.gz` contiene 210 copie editoriali, PDF precedente
incluso, con impronta separata. Questi file non sono rigenerati.
Il punto fisso della review è il working tree iniziale, non il solo HEAD:
`delta-documentale.diff` e `delta-files.json` comprendono nuove aggiunte e
modifiche dei non tracciati. HEAD iniziale `61ce18ae1d4e0458b63e970b793b0b3a634a3adf`.

Compilazione integrale XeLaTeX in tre passaggi finali riusciti, con log
separati. Verificati livelli e ordine delle 47 voci, 18 funzioni, sei schede
principali e nove aggiuntive, attributi delle 13 relazioni, nativi e allegati
PDF delle 11 tavole, riferimenti risolti e corrispondenza dell'estratto.
Le fonti sono identificate in `fonti.json`; i test citati restano prove storiche.

## Limiti

Restano cinque messaggi preesistenti `Infinite glue shrinkage` nelle tabelle
dell'appendice C; non sono dichiarati eliminati. Le pagine sono osservate
anche nella nuova composizione, come documentato nel verbale visivo.
I diagrammi A3 sono vettoriali: per la stampa mantenere il formato originale.
Il PDF allega i nativi, che restano anche file autonomi modificabili.

Nessun test applicativo, build Java, avvio UI, DB, Docker o harness eseguito.
Nessuna installazione di sistema, commit, invio o pubblicazione. Tectonic,
Windows e macOS non verificati. Toolchain temporanea in `/tmp`, non garantita
su altri PC; i sorgenti e le esportazioni sono autonomi come input editoriali.
La revisione degli autori e i ticket 11–13 restano distinti da questa chiusura.
