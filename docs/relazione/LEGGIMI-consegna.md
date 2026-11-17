# DriveHub — sorgenti per la revisione degli autori

17 novembre 2026. Pacchetto autonomo del riallineamento documentale 09–13:
capitoli 1–5 e appendici A–C. PDF e sorgenti sono proposti agli autori;
chiusura tecnica e review assistite non ne attestano l'approvazione.
Nessun invio al docente o pubblicazione è stato effettuato.

## Compilare lo ZIP estratto

Estrarre tutto `sorgenti-relazione.zip` in una directory pulita e aprire
un terminale dove si trova `main.tex`. Occorrono XeLaTeX, lingua italiana,
font Latin Modern (anche OpenType) e pacchetti `geometry`, `graphicx`,
`pdfpages`, `enumitem`, `xcolor`, `listings`, `longtable`, `array`, `hyperref`.
Il ticket 13 verifica XeTeX 0.999997, TeX Live 2025/Debian, pacchetti
TeX Live `2025.20260124-1`, Latin Modern 2.005. Versioni, risorse effettive,
font incorporati e impronte sono nelle verifiche 13 del repository.

```bash
mkdir -p build
xelatex -no-shell-escape -halt-on-error -interaction=nonstopmode -recorder -output-directory=build main.tex
xelatex -no-shell-escape -halt-on-error -interaction=nonstopmode -recorder -output-directory=build main.tex
xelatex -no-shell-escape -halt-on-error -interaction=nonstopmode -recorder -output-directory=build main.tex
```

Il risultato è `build/main.pdf`. Gli ultimi passaggi stabilizzano indice,
etichette e riferimenti: confrontare anche `main.toc`, `main.aux` e `main.out`
e leggere tutti i messaggi. Restano cinque messaggi preesistenti
`ignored error: Infinite glue shrinkage found in box being split` nelle
tabelle dell'appendice C. Le pagine coinvolte sono state osservate nella
nuova composizione: nessun difetto visivo riscontrato, messaggi non eliminati.
Con una diversa toolchain occorre ricontrollare impaginazione e diagnostica.

## Materiali distribuiti

Lo ZIP contiene 118 file: 15 sorgenti LaTeX, 43 nativi `.drawio`,
43 esportazioni PDF, 16 screenshot PNG e queste istruzioni. Sono inclusi
anche i dettagli di diagramma conservati; tutti gli input editoriali del
recorder risultano nell'estrazione. I frammenti sono incorporati nel LaTeX.
Non servono Java, Maven, database, il repository applicativo o file editoriali
esterni. Toolchain e risorse TeX sono prerequisiti esterni espliciti;
nessuna cache o directory `/tmp` preesistente è richiesta all'autore.

Le undici tavole d'insieme riallineate occupano dodici pagine A3:
nove orizzontali e tre sequenze verticali; ER e legenda occupano due fogli.
Gli altri fogli sono A4. Per la stampa mantenere i formati originali,
valutando la leggibilità se si ridimensiona. Ogni esportazione conserva
il nativo come allegato o XML nel Subject; i `.drawio` autonomi restano
le copie primarie modificabili. Per modificarli usare draw.io e aggiornare
anche l'esportazione con lo stesso nome. La compilazione usa i PDF inclusi
e non richiede draw.io.

Sono esclusi PDF di consegna (fornito separatamente), intermedi `build`,
verifiche e ZIP storici, software/test, SQL/dati, toolchain, cache e backup.
Le figure originali degli autori restano nel repository: il pacchetto
include i corrispondenti nativi adattati e le esportazioni verificate.

## Provenienza e limiti

Il PDF ricompilato dal presente ZIP è confrontato con quello consegnato
per testo, paginazione e pixel di tutte le pagine. Differenze binarie possono
dipendere da date, identificativi PDF e percorsi registrati dal compilatore;
non si garantisce identità binaria o impaginazione fra versioni diverse.
Le istruzioni generiche sopra richiedono una distribuzione TeX configurata;
la prova locale usa una toolchain estratta, senza installazione di sistema.
Windows, macOS, stampa e accessibilità PDF con screen reader non verificati.

Le citazioni di codice, ADR e prove storiche sono percorsi nel repository
DriveHub, HEAD `61ce18ae1d4e0458b63e970b793b0b3a634a3adf`: servono per
approfondire le fonti, non come input di compilazione. S0 è lettura statica;
E1/E2/E3 restano sessioni applicative storiche distinte. Non sono stati
rieseguiti test, harness, avvii UI o accessi DB. Restano i limiti su movimenti
zero, identità, aggiornamenti dopo commit e riproduzione degli harness.

Le verifiche del pacchetto corrente, i log della compilazione da estrazione,
la corrispondenza consolidata, il verbale visivo e le due review indipendenti
sono in `docs/relazione/verifiche/ticket-13/`. Le impronte definitive di
PDF e ZIP sono in `artefatti-finali.json` e `SHA256SUMS` di quella directory.
I pacchetti dei ticket 07/08 e delle tavole del 15 novembre restano storici.
