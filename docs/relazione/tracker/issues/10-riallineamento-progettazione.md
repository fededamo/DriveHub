# 10 — Riallineamento di requisiti e progettazione

Status: done

Blocked by: None.

## What to build

Un capitolo di requisiti e progetto con le otto sezioni originali e lo stesso
percorso interno: sei template completi, funzioni dell'interfaccia per ruolo,
navigazione, package, quattro gruppi di classi, tre sequenze e database.
Rifare/riadattare tutti i diagrammi originali con la skill `drawio:drawio`,
partendo dai disegni degli autori; schede, screenshot e tavole attuali sono
materiale di lavoro. Le appendici completano il corpo mantenendo tutti i
contenuti del percorso originale.

Riferimenti: [specifica](../spec.md), [matrice, capitolo 2](../../allineamento.md#capitolo-2)
e [indice](../../indice.md#2-analisi-dei-requisiti-e-progettazione).
Per i diagrammi sono obbligatori i [vincoli, le basi degli autori e le undici corrispondenze](../../allineamento.md#diagrammi-vincoli-operativi).

## Acceptance criteria

- [x] Tutte le voci 2–2.8.3 della matrice hanno una trattazione identificabile,
  rispettando livelli e ordine, compresa la sottosezione non numerata sul dominio.
- [x] Sei schede complete nel corpo: test drive, noleggio, vendita al cliente,
  cessione, autorizzazione e sconti. Ogni scheda include condizioni, attori,
  livello, flusso, alternative riferite ai passi e riscontri delle prove.
- [x] UC-05 precede UC-04 nell'esposizione ma gli identificativi restano
  coerenti in diagrammi, schede e rinvii. Le schede principali non sono
  duplicate in appendice A; restano disponibili le nove schede aggiuntive.
- [x] Ripristinato il dettaglio delle diciotto funzioni UI originali con
  schermate attuali, spiegazione delle viste condivise e limite esplicito
  per Welcome Page separata e panoramica prenotazioni del venditore assenti.
  I profili descrivono il dialogo comune, senza immagini storiche fuorvianti.
- [x] Quattro class diagram originali rifatti/riadattati nel corpo, uno per
  gruppo; appendice B e rinvii conservano la copertura degli undici package.
- [x] Rifatti/riadattati con la skill `drawio:drawio` tutti gli undici
  diagrammi originali, mantenendone collocazione, funzione, struttura e stile
  riconoscibili e verificandone i contenuti sul codice attuale.
- [x] ER, casi d'uso e navigazione derivano da SWE ER, Users SWE e Page
  Navigation Diagram degli autori; il package diagram riparte da Package
  diagram. Conservati gli originali e riscontrate anche le copie già corrette.
- [x] Per ogni tavola documentati base, adattamenti, sorgente nativo
  `.drawio`, esportazione aggiornata e controllo visivo nel PDF. Le viste di
  dettaglio attuali non sostituiscono i quattro class diagram e le tre
  sequenze d'insieme dell'originale.
- [x] Schema relazionale accompagnato da tredici relazioni,
  attributi/chiavi/nullabilità e legenda leggibili anche nella spiegazione.
- [x] Conservati i contenuti attuali su prezzi, identità, enum, concorrenza
  e vincoli; ogni adattamento di un argomento originale è motivato.
- [x] Baseline dei file coinvolti, anche non tracciati, prima delle modifiche;
  vecchia relazione ed evidenze storiche intatte. Ogni rifacimento,
  adattamento o nuova tavola segue la skill draw.io e conserva i sorgenti.
- [x] Compilazione reale, controllo dei rinvii e ispezione visiva delle pagine
  del capitolo e delle appendici coinvolte, incluse le tavole A3; consegnati
  estratto e riscontri agli autori senza presumere la loro approvazione.

## Comments

16 novembre 2026: secondo capitolo nell'ordine di lavorazione. Il ticket 09
non è un prerequisito tecnico: la base e la pianificazione sono già disponibili.
Nessuna modifica applicativa, prova su archivi operativi, commit o pubblicazione.

16 novembre 2026: chiusura tecnica del solo ticket 10. Verificate tutte le
47 voci del capitolo, sei schede principali e nove aggiuntive, diciotto
funzioni UI, undici tavole originali con nativi ed esportazioni e tredici
relazioni. PDF completo di 129 pagine, estratto di 96; compilazione reale
in tre passaggi e controllo visivo integrale del perimetro. Review
Standards e Spec indipendenti sulla baseline iniziale, inclusi i non
tracciati: zero rilievi aperti, tre P2 Spec corretti e ricontrollati.
[Confronto, fonti e verifiche](../../verifiche/ticket-10/README.md),
[rapporti iniziali e chiusura](../../verifiche/ticket-10/revisione.md).
Restano i cinque messaggi TeX preesistenti in C, osservati nella nuova
composizione. Capitolo 1, originali, software/schema/dati e vecchie
evidenze intatti; nessun test applicativo, DB, commit o invio. La chiusura
non costituisce approvazione degli autori; i ticket 11–13 restano aperti.
