# Istruzioni di progetto

- `Relazione_SWE/**` è gestita esclusivamente dall'utente: escluderla esplicitamente da letture, ricerche, modifiche, generazioni e verifiche, salvo nuova istruzione esplicita dell'utente.
- Per lavorare alle revisioni, leggere prima [il registro esterno al progetto](../PIANO_REVISIONI_DRIVEHUB.md). Mantenerlo fuori da questa directory, accanto agli audit. Aggiornare attività, decisioni, comandi/esiti, limiti e prossimo passo alla fine di ogni sessione; segnare completato solo ciò che è stato verificato.
- Usare [il glossario](GLOSSARY.md) per i termini di dominio; le decisioni sui prezzi sono registrate in [ADR 0001](docs/adr/0001-base-manuale-e-prezzo-concordato.md).

## Agent skills


### Issue tracker

Tracker Markdown locale esterno in `../TRACKER_DRIVEHUB/`, accanto al piano. Vedere [configurazione e convenzioni](docs/agents/issue-tracker.md).

### Triage labels

Cinque etichette standard delle skill, registrate nel campo `Status:` dei ticket. Vedere [vocabolario](docs/agents/triage-labels.md).

### Domain docs

Contesto unico: glossario e ADR esistenti, con relazione sempre esclusa. Vedere [regole di consultazione](docs/agents/domain.md).
