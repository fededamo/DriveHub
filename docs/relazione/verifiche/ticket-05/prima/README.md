# Relazione DriveHub

Sorgenti della nuova relazione di Ingegneria del Software. Il documento è una
**stesura progressiva**: i capitoli 1–2 e le appendici A–B sono completi
e proposti per revisione. Il capitolo 1 è conservato dal ticket 01;
nessuna di queste parti è considerata già approvata dagli autori.
I capitoli 3–5 e l'appendice C restano provvisori. Non è ancora la
relazione finale da consegnare.

- [PDF compilato](relazione-drivehub.pdf)
- [Sorgente principale](main.tex)
- [Capitolo 1](capitoli/1_introduzione.tex)
- [Sezioni 2.1–2.4](capitoli/2_funzioni.tex) e [appendice A](appendici/a_casi_uso.tex)
- [Sezioni 2.5–2.6](capitoli/2_architettura.tex) e [appendice B](appendici/b_classi.tex)
- [Sezione 2.7](capitoli/2_sequenze.tex) e [sezione 2.8](capitoli/2_database.tex)
- [Capitolo 2 per la revisione degli autori](verifiche/ticket-04/capitolo-2-per-revisione.pdf), con [estratto delle nuove 2.7–2.8](verifiche/ticket-04/ticket-04-per-revisione.pdf)
- [Verifiche ticket 04](verifiche/ticket-04/README.md): sequenze, ER e modello relazionale, riscontro statico, compilazione e controllo visivo
- [Revisioni indipendenti Standards e Spec del ticket 04](verifiche/ticket-04/revisione.md)
- [Estratto del ticket 03 per la revisione degli autori](verifiche/ticket-03/ticket-03-per-revisione.pdf)
- [Verifiche del ticket 03](verifiche/ticket-03/README.md), con matrice package–tavole–sorgenti e revisioni indipendenti
- [Revisioni Standards e Spec del ticket 03](verifiche/ticket-03/revisione.md): concluse e conformi, nessun rilievo aperto
- [Estratto del ticket 02 per revisione](verifiche/ticket-02/ticket-02-per-revisione.pdf)
- [Diagrammi draw.io ed esportazioni](diagrammi/README.md), [screenshot attuali](screenshot/)
- [Verifiche del ticket 02](verifiche/ticket-02/README.md)
- [Revisioni indipendenti concluse dei ticket 01 e 02](verifiche/ticket-02/revisione.md): Standards e Spec conformi, nessun rilievo aperto.
- [Verifiche del ticket 01](verifiche/ticket-01/README.md)
- [Indice concordato](indice.md), [piano](piano.md) e [specifica](tracker/spec.md)

## Compilazione

Eseguire dalla directory `docs/relazione`. Con **Tectonic 0.17.0**, che gestisce
automaticamente i passaggi necessari per indice e riferimenti:

```bash
mkdir -p build
tectonic --untrusted --keep-intermediates --keep-logs --outdir build main.tex
cp build/main.pdf relazione-drivehub.pdf
```

La prima compilazione richiede Internet per scaricare le risorse LaTeX.
La compilazione finale del ticket 04 usa Tectonic 0.17.0, variante GNU,
in `/tmp/drivehub-ticket04-tools`, con cache separata. La variante musl
si arrestava nella generazione del documento completo. Gli strumenti
temporanei sono stati ripristinati il 13 novembre dopo la loro rimozione.
Comando effettivo, impedimenti e risultati sono nel verbale del ticket 04.
Questi percorsi temporanei non sono prerequisiti garantiti su un altro PC.
Il PDF distribuito non richiede tali strumenti per
essere aperto.

I sorgenti usano pacchetti LaTeX comuni (`babel` con italiano, `geometry`,
`graphicx`, `enumitem`, `xcolor`, `hyperref`) e mantengono le convenzioni di
`Relazione_SWE/main.tex`: classe `report`, A4, corpo 12 pt, margini di 2,5 cm,
frontespizio, indice in numeri romani e capitoli in numeri arabi. Il ramo per
`pdfminorversion` è condizionale per consentire anche il motore di Tectonic.
Non è stata verificata una seconda distribuzione LaTeX.

## Materiali e perimetro

`capitoli/` e `appendici/` contengono il testo; `main.tex` ne fissa l'ordine.
L'indice include tutte le sezioni concordate e le sottosezioni strutturali
di progettazione. Le sottosezioni di dettaglio dei capitoli ancora da scrivere
saranno riscontrate durante la rispettiva stesura, senza riproporre nomi di
classi o meccanismi rimossi.

`verifiche/ticket-01/drawio/` contiene esclusivamente la prova tecnica di
esportazione: non è una figura della relazione e non è inclusa da LaTeX.
I diagrammi del ticket 02 sono in `diagrammi/`, con sorgenti modificabili
ed esportazioni PDF; le schermate sono acquisizioni della versione corrente
con dati demo su PostgreSQL temporaneo. Le verifiche del ticket 01 sono
conservate intatte e descrivono il risultato storico di quel ticket: non
vanno rieseguite contro il documento avanzato per pretendere che l'appendice A
sia ancora provvisoria. Il controllo aggiornato è in `verifiche/ticket-02/`.
Il ticket 03 completa 2.5–2.6 e B con tredici nuove tavole draw.io
(package diagram e dodici tavole delle classi per undici package).
Le verifiche 01 e 02, inclusi i rispettivi manifest e PDF estratti, restano
fotografie intatte delle stesure precedenti.
Il ticket 04 completa 2.7–2.8 con otto tavole di sequenza coordinate,
due viste ER e tre viste relazionali delle tredici tabelle applicative.
Anche le verifiche e i manifest del ticket 03 rimangono intatti;
il controllo aggiornato è in `verifiche/ticket-04/`. Non vanno applicate
al PDF attuale le vecchie asserzioni sulle sezioni allora provvisorie.
La relazione precedente, il software, lo schema, i
dati e le evidenze storiche restano separati da questa stesura.

Gli intermedi di compilazione sono ignorati da Git; il PDF e i verbali di
verifica sono conservati. Questa attività non esegue test applicativi e non
crea commit.
