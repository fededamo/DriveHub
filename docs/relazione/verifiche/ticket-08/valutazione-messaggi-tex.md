# Messaggi TeX finali — rilievo Spec e valutazione

15 novembre 2026. Il primo verbale aveva omesso cinque messaggi effettivi.
La review Spec ha individuato il limite del filtro automatico: cercava
Warning/Overfull/Underfull/caratteri mancanti/riferimenti indefiniti, ma non
`ignored error`. Il campo vuoto `warnings_last_pass` nell'esito originario
non esclude quindi questi altri messaggi. Log ed esito originario restano
conservati, senza riscrivere la prova del 14 novembre.

## Riscontro

In `latex-log.txt:805`–809 e `pacchetto-latex-log.txt:801`–805 compaiono,
una volta per ciascun numero stampato 98–102:

```text
ignored error: Infinite glue shrinkage found in box being split
```

Gli stessi cinque messaggi compaiono nei log dei passaggi finali del
repository e del pacchetto estratto; gli eseguibili hanno prodotto il PDF
completo di 107 pagine e sono terminati con exit 0. Non sono riferimenti
indefiniti o caratteri mancanti; segnalano comunque un problema del motore
durante la divisione di un riquadro e non sono classificati come assenti.
Non è stata dimostrata la causa interna, né applicata una soppressione.

## Osservazione e decisione documentale

L'agente principale ha riaperto con `view_image` tutti i rendering originali
a 120 dpi delle pagine fisiche **101–106**, numerate **98–103**, confrontandole
con le righe di `appendici/c_verifiche.tex:121`–366. La tabella C.1 prosegue
da UC-01/02/03 a UC-03/04/05/06, UC-07/08/09/10, UC-11/S01/S02/S03 e S04;
C.2 inizia sul foglio 102 e termina sul 103. Intestazioni ripetute, note di
continuazione, separatori, ultime righe e transizione alla prosa risultano
leggibili, senza celle sovrapposte o tagli rilevati. Questa è osservazione
personale aggiuntiva, non un esito dedotto dagli assert.

Non emerge un difetto del documento che motivi cambiamenti di impaginazione
o ricompilazione: PDF e ZIP rimangono quelli già osservati e ricompilati
autonomamente. **Restano cinque messaggi del motore, accettati con questo
limite**, non dichiarati risolti. Altra versione LaTeX o stampa richiedono
una nuova valutazione; non si garantisce la stessa resa su ogni ambiente.

Il filtro corrente di `verifica-pacchetto.py` ora include `ignored error`,
li conserva nell'esito e li stampa per la valutazione manuale. Il filtro è
stato verificato sui due log conservati: cinque rilevamenti per log.
L'intero confronto dell'estrazione non è stato ripetuto perché il relativo
albero temporaneo non esiste più. Nessun sorgente LaTeX, figura, PDF o ZIP
è cambiato per chiudere questo rilievo. Ricontrollo affidato al revisore Spec.
