# Capitolo 5: fonti e limite delle attribuzioni

17 novembre 2026. Sono conservati i dieci nuclei AI-01–AI-10 del ticket 07.
Il [delta](delta-documentale.diff) registra l’aggiunta dell’integrazione
iniziale, l’estensione di AI-07 alle review 09–12 e il nuovo AI-11 in 5.4.
Le tre pagine restano 119–121. La [matrice precedente](../ticket-07/matrice-fonti.md)
continua ad attestare la propria versione; non è stata riscritta o rieseguita.

| Affermazione | Fonte e riscontro | Limite |
| --- | --- | --- |
| Scheletro e prime classi manuali; revisione assistita | Dichiarazione degli autori in `piano.md`, “Origine del lavoro e uso dell’AI”; AI-01 conservato. | Dichiarazione di provenienza, non deduzione dal codice o da Git. |
| Audit, piano, ticket e decisioni iniziali | `docs/evidence/revision-records.zip`, 24 membri verificati contro il manifest interno in [registri-originali.json](registri-originali.json); ADR 0001/0002 e verifiche citate in AI-02–05. | Non sono trascrizioni; decisioni registrate come confermate non provano l’approvazione di ogni patch. |
| Sessioni storiche applicative | [riscontro-storico.json](riscontro-storico.json): E1 423, E2 394; mirati, RED ed E3 distinti; AI-06 conservato. | Non sommati, nessuna percentuale di coverage, nessuna nuova esecuzione. |
| Review 09–12 concluse tecnicamente | Rapporti `ticket-09/revisione.md` fino a `ticket-12/revisione.md`, compresi rapporti separati e ricontrolli degli stati; acquisiti nella baseline 13. | Revisioni assistite da agenti, non approvazioni degli autori. AI-07 lo dichiara esplicitamente. |
| Richiesta di conservare scansione e contenuto originale | `piano.md`, “Riallineamento richiesto dagli autori — 16 novembre 2026”; `allineamento.md`, `indice.md`, specifica e ticket 09–13. | Richiesta documentata, non validazione del risultato ora prodotto. |
| Attività documentale dell’AI in 09–13 | [102 corrispondenze](corrispondenza-102.md), [23 funzioni dei frammenti](corrispondenza-23.json), [materiali](corrispondenza-materiali.md), fonti e rapporti dei ticket. | Identificano operazioni e artefatti; non attribuiscono a Codex le basi degli autori né ogni riga del software. |
| Undici rifacimenti/adattamenti del ticket 10 | [tavole 10](../ticket-10/tavole.md), [riscontro 13](tavole.md), [hash di esportazione conservati](confronto-export-10.json). Quattro copie dai nativi degli autori e sette ricostruzioni da PDF/PlantUML originali. | Nel 13 riuso di rifacimenti già validi, nessuna nuova esecuzione Desktop dichiarata. |
| Verifiche effettive del ticket 13 | Compilazioni baseline e documento, [controllo visivo di 151 pagine](controllo-visivo.md), [autonomia dello ZIP](autonomia-pacchetto.json), review Standards/Spec separate nel README. | Controlli editoriali; né suite Java né harness, UI o DB. Chiusura tecnica distinta dalla revisione degli autori. |

AI-08/09 mantengono le tracce su documentazione e diagrammi; AI-10 mantiene
espliciti i dati non ricostruibili: prompt integrali, modelli, durata,
percentuali, integrazione IDE e attribuzioni individuali. Non sono state
aggiunte metriche, conversazioni o approvazioni umane. PDF e sorgenti sono
proposti agli autori per la loro revisione, senza invio a terzi.
