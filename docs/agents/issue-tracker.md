# Tracker Markdown locale esterno

La destinazione scelta dall'utente è `../TRACKER_DRIVEHUB/`, relativa alla radice del repository `DriveHub/`. Piano, specifiche e ticket restano esterni al progetto. Non creare un tracker sotto `.scratch/` nel repository né pubblicare issue sui servizi remoti.

## Percorsi

- Registro generale: [PIANO_REVISIONI_DRIVEHUB.md](../../../PIANO_REVISIONI_DRIVEHUB.md).
- Radice del tracker: [TRACKER_DRIVEHUB](../../../TRACKER_DRIVEHUB/).
- Una directory per intervento: `<tracker>/<feature-slug>/`.
- Per le revisioni già approvate: `<tracker>/revisioni/`.
- Specifica: `<tracker>/revisioni/spec.md`.
- Ticket: `<tracker>/revisioni/issues/<NN>-<slug>.md`, numerati da `01` in ordine di dipendenza; un file per ticket.
- I percorsi sopra designano destinazioni: il setup non crea specifiche o ticket vuoti.

## Operazioni delle skill

- «Pubblicare sul tracker»: creare o aggiornare il file nella destinazione esterna prevista.
- «Leggere un ticket»: leggere il percorso indicato; se viene dato solo un numero, cercarlo nell'intervento corrente e risolvere eventuali ambiguità.
- «Commentare»: aggiungere testo in fondo al ticket sotto `## Comments`, conservando quanto già presente.
- Usare `Status:` per il [vocabolario di triage](triage-labels.md). Le specifiche e i ticket prodotti da `to-spec`/`to-tickets` sono `ready-for-agent` e non richiedono un altro triage.
- Ogni ticket dichiara `Blocked by:` con numeri/titoli dei prerequisiti, oppure `None`; essere `ready-for-agent` non significa avere già soddisfatto le dipendenze.
- Per l'esecuzione usare `State: open | in-progress | closed`. Chiudere soltanto dopo criteri di accettazione e verifiche richieste soddisfatti, poi aggiornare il registro generale con esiti e prossimo passo.
- Collegare ogni ticket all'attività R00–R11 e ai rilievi pertinenti del piano. Il numero del ticket e l'ID del piano sono identificatori distinti.
- Non eseguire commit o pubblicazioni come conseguenza implicita di un cambio di stato.

## Fonti da riutilizzare

Il [piano approvato](../../../PIANO_REVISIONI_DRIVEHUB.md), il [glossario](../../GLOSSARY.md) e gli [ADR](../adr/) sono già presenti: sintetizzarli senza ripetere l'intervista sulle decisioni accettate. Ignorare sempre `Relazione_SWE/**`.
