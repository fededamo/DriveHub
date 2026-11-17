# Ticket 13 — integrazione e verifica del riallineamento

17 novembre 2026. **Concluso tecnicamente: `done`**, dopo le review finali
indipendenti Standards e Spec, entrambe conformi senza rilievi.
**151 pagine**, capitoli 1–5 e appendici A–C. PDF e sorgenti sono proposti
agli autori per revisione: nessuna approvazione, invio o pubblicazione dedotti.

- [PDF completo](../../relazione-drivehub.pdf).
- [ZIP autonomo aggiornato](sorgenti-relazione.zip) e [istruzioni incluse](../../LEGGIMI-consegna.md).
- [PDF ricompilato dall’estrazione](ricompilato-da-zip.pdf), [prova di autonomia](autonomia-pacchetto.json) e [manifest dei 118 membri](manifest-pacchetto.json).
- [Corrispondenza delle 102 voci](corrispondenza-102.md), [dettaglio verificabile](corrispondenza-102.json), [23 frammenti originali](corrispondenza-23.json) e [materiali censiti](corrispondenza-materiali.md).
- [Aggiornamento e fonti del capitolo 5](capitolo-5-fonti.md), [fonti tecniche e storiche](fonti-tecniche.md).
- [Undici tavole, basi, nativi ed esportazioni](tavole.md), [43 coppie verificate](diagrammi-43.json) e [hash uguali al ticket 10](confronto-export-10.json).
- [Osservazione di tutte le 151 pagine, comprese A3](controllo-visivo.md), [messaggi TeX](messaggi-tex.json) e [paginazione](paginazione.json).
- [Comandi, strumenti, log e tentativi](comandi.md), [richiesta e perimetro delle review](richiesta-operativa.md).
- [Review Standards e Spec, mantenute separate](revisione.md), con ricontrolli conclusivi degli stati.
- [Impronte degli artefatti finali](artefatti-finali.json), [manifest SHA-256](SHA256SUMS) e [finalizzazione](finalizzazione.txt).

## Baseline e conservazione

[Stato iniziale](stato-iniziale.txt), [baseline di 2.899 file](baseline.json)
e [archivio editoriale](baseline-editoriale.tar.gz), con
[impronta](baseline-editoriale.sha256), comprendono i non tracciati e gli
storici pertinenti. L’archivio conserva 227 copie dei materiali editoriali
attivi e originali, incluso il PDF precedente; i precedenti ZIP, log, XML,
harness e verifiche restano ai loro percorsi e sono protetti dalle impronte
della baseline. [Conservazione degli storici](conservazione-storici.json).
Il punto fisso delle review è questa fotografia, **non il solo HEAD**:
[istruzione](punto-fisso-review.txt), [delta dei file](delta-files.json) e
[diff documentale](delta-documentale.diff). HEAD è rimasto
`61ce18ae1d4e0458b63e970b793b0b3a634a3adf`; nessun commit.

Il documento dei ticket 09–12 è riusato integralmente. Il solo intervento
al corpo aggiorna 5.3/5.4 sull’attività documentale 09–13 e le sue fonti.
[Stati e rapporti delle dipendenze](dipendenze.json): tutti `done`, con
ricontrolli conservati e nessuna approvazione degli autori dedotta.
Le pagine 119–121 cambiano; tutte le altre **148 pagine sono identiche a
testo e pixel** alla baseline, compresi frontespizio, indice e capitoli
1–4/A–C. Nessun cambio di paginazione. La nuova compilazione della baseline
è identica su tutte le 151 pagine al PDF acquisito:
[confronto](confronto-baseline-ricompilata.json).

## Controlli effettivi

`consolida.py` espande i 24 input originali effettivi e i 15 correnti;
verifica 102 livelli/voci nell’ordine, 23 funzioni dei frammenti, 27 estratti
di implementazione e 15 blocchi di testing, inclusi i comandi. Non sostituisce
il confronto delle spiegazioni e degli oracoli: le corrispondenze collegano
intervalli, trattamento e fonti analitiche 09–12; le letture nuove sono in
[estratti-fonti.txt](estratti-fonti.txt). Sei schede complete in 2.2,
diciotto funzioni UI, due tabelle argomentative, undici rifacimenti originali;
A aggiuntiva senza duplicazioni, B copre undici package, C conserva i limiti.

`verifica.py` controlla conservazione di tutti i file iniziali, ambito del
delta, PDF, 82 riferimenti espliciti risolti, 238 collegamenti interni,
107 risorse font incorporate, formati e presenza integrale dei frammenti.
La compilazione XeLaTeX reale termina in tre passaggi; indice e riferimenti
sono stabili fra secondo e terzo. [Esito](esito-documentale.json),
[input effettivi](input-compilazione.fls), [log finale](latex-log.txt).
La riuscita dei rendering resta distinta dall’osservazione diretta nel verbale.

`riscontro-storico.py` legge log/XML e impronte; non riesegue le prove.
[Riscontro](riscontro-storico.json): E1 423, mirato 82; E2 394, mirati 363;
RED, errori iniziali ed E3 distinti, input cambiati e lacune conservati.
Nessuna somma fra sessioni o percentuale di coverage. S0 è lettura statica.

`pacchetto.py` crea l’archivio, lo estrae in directory nuova, compila davvero
tre volte e controlla gli input registrati: **73 input editoriali interni**,
nessun input editoriale dal repository o da percorsi esterni. Toolchain,
pacchetti e font TeX restano prerequisiti dichiarati. I 118 membri sono:
15 TeX, 43 nativi draw.io, 43 PDF vettoriali, 16 PNG e LEGGIMI. PDF di
consegna, software, SQL, build e storici sono esclusi dallo ZIP; il PDF si
consegna separatamente. Tutte le 151 pagine ricompilate coincidono per
testo, formato e resa a 96 dpi; date/ID, prefissi dei subset Latin Modern
e conseguenti lunghezze compresse/offset spiegano le differenze binarie:
[confronto degli oggetti e stream](confronto-binario-pdf.json).
Lo ZIP è ricontrollato prima e dopo l’estrazione.

## Limiti residui

Persistono cinque messaggi `Infinite glue shrinkage` in C, pagine fisiche
145–149, come nella baseline e nello ZIP ricompilato. Nessun difetto visivo
associato rilevato; nessun Overfull, Underfull, glifo mancante o riferimento
irrisolto. Non sono dichiarati risolti o soppressi. La skill draw.io è
applicata alla verifica dei rifacimenti validi del ticket 10; nessun nuovo
export richiesto o dichiarato. Il vecchio Desktop temporaneo non è disponibile.
Nativi, esportazioni e basi degli autori sono intatti.

Nessuna build Java/Maven, suite JUnit, harness, UI o accesso DB eseguiti.
Difetti applicativi e incompatibilità storiche restano separati, senza patch.
La verifica tecnica non certifica il software e non sostituisce gli autori.
