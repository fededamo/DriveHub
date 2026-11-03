# Documentazione del dominio

DriveHub ha un solo contesto di dominio. Riutilizzare i documenti esistenti:

- [GLOSSARY.md](../../GLOSSARY.md) nella radice del progetto.
- [docs/adr](../adr/) per le decisioni; leggere quelle pertinenti all'attività, in particolare [ADR 0001 sui prezzi](../adr/0001-base-manuale-e-prezzo-concordato.md) quando si toccano prezzi o acquisti.
- [Piano e registro esterno](../../../PIANO_REVISIONI_DRIVEHUB.md) per decisioni operative, attività, prove e avanzamento.

Prima dell'esplorazione leggere il glossario e gli ADR pertinenti. Usare i termini canonici nei ticket, nei test e nelle spiegazioni. Segnalare esplicitamente conflitti con una decisione accettata prima di cambiarla.

Il glossario contiene solo termini di dominio. Registrare scelte operative e avanzamento nel piano; creare altri ADR soltanto per decisioni con alternative e conseguenze rilevanti. Non creare nuovi documenti di dominio solo perché manca un file.

`Relazione_SWE/**` è esclusa da letture, ricerche, modifiche, generazioni e verifiche. Non usarla come fonte del modello di dominio o come destinazione di questi flussi.
