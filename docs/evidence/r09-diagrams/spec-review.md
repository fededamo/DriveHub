# Review indipendente Spec — ticket17

Esito iniziale: **1 rilievo P2**, nessuno scope creep rilevato. Review della sola
sessione17 rispetto alla baseline locale `/tmp/drivehub-r17-baseline/tree`,
non rispetto a HEAD. Patch congelata `review.patch`, SHA256
`1d07fb5bca5373935e98d7ea833d334aade090683d0ebe4f1290a6fe7c6317cc`;
manifest `review-files.sha256.json`:130file verificati, zero discrepanze.
HEAD di riferimento: `c2a81122ee6be44760ded4160fde229bd33e837c`.

## Requisiti mancanti/parziali e conformità apparente errata

**[P2] Ritorno dai dialoghi cliente rappresentato come permanenza nel dialogo.**
Requisito: ticket17 riga18, AC14 della specifica riga163 e R09 del piano
righe324–327 richiedono navigazione coerente con i flussi correnti.
Nel nuovo hunk `@@ -0,0 +1,37 @@` di
`docs/diagrams/current/page-navigation-customer.puml`, righe22 e29, i self-loop
`TD --> TD` e `Topup --> Topup` mostrano conferma ed esito senza uscita dal dialogo.
Il codice invece elabora `showAndWait().ifPresent(...)` dopo la chiusura:
`ClientDashboardView.java` righe165–174 e344–355. Conferma, annullamento ed
errore lasciano quindi l'utente sulla tab chiamante; per il test drive può
essere vendita o noleggio. Anche la nota riga33 confonde chiusura confermata
con annullamento senza comando. Correggere i ritorni e la nota, poi aggiornare
SVG e registrazione del render. Riscontro anche nel PNG ispezionato.

## Altri riscontri

Controllati i18sorgenti correnti, glossario/ADR0001/architettura, servizi
acquisto/cessione/noleggio/test drive, Observer, AsyncLoad e navigazione.
Nessun altro rilievo di specifica individuato. I riferimenti distinguono
controlli statici e prove GUI; manuale e totale concordato rispettano ADR0001.

Verificati hash dei37artefatti storici contro la baseline e identità dei93input
runtime originali. Ricalcolati dagli XML79/79 mirati e394/394 finali, zero
failure/errori/salti; log di successo GUI/avvio e impronte sentinella identiche
supportano le affermazioni. Tentativi fallito/interrotto e394/394del16 sono
distinti dai successi17. Nessuna copertura percentuale dedotta.

Limiti: review statica e lettura delle prove salvate; ispezione indipendente
di cinque PNG rappresentativi, senza rieseguire suite/render o certificare
ogni gesto GUI. Relazione sempre esclusa; nessun codice/DB/tracker modificato.
Il rilievo richiede una correzione documentale e un follow-up separato sulla
nuova versione; la chiusura pendente del ticket non è conteggiata come mancanza.
