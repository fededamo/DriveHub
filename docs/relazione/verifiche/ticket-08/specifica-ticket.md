# Ticket documentale 08 — Verifica finale e pacchetto di consegna

Specifica operativa ricevuta dall'utente il 14 novembre 2026. Questa richiesta
è indipendente da ticket già pubblicati e da contenuti in `/tmp`.

## Stato e obiettivo

Ticket 01–07 conclusi; capitoli 1–5 e appendici A–C completi e proposti agli
autori, non approvati. Verificare lo stato effettivo iniziale: le 107 pagine
consegnate dal ticket 07 non sono un vincolo d'impaginazione.
Verificare la relazione come documento unitario e preparare un pacchetto
utilizzabile dagli autori per la revisione finale.

## Criteri operativi

1. Leggere `docs/AGENTS.md` e istruzioni richiamate; specifica generale,
   indice, piano, glossario e README della relazione; verifiche/revisioni 07
   e parti pertinenti di 01–06; note del corso indicate nel piano.
2. Verificare corrispondenza a specifica, indice e corso; frontespizio,
   titoli, ordine, indice, numerazione, didascalie e riferimenti; coerenza
   tra requisiti, casi d'uso, diagrammi, implementazione, frammenti,
   verifiche e dichiarazioni AI; terminologia della concessionaria e
   distinzione tra contratto corrente e versioni storiche; attribuzione
   di risultati, date, versioni, ambienti, lacune e limiti; leggibilità
   di tutte le pagine; assenza di segnaposto, stati editoriali superati
   e approvazioni umane non documentate.
3. Riscontrare staticamente le affermazioni dubbie su sorgenti, schema ed
   evidenze pertinenti, senza riaprire indiscriminatamente verifiche concluse
   né duplicare le matrici: produrre matrice finale criteri → parti della
   relazione → riscontri → esiti/lacune.
4. Apportare soltanto correzioni documentali necessarie e motivate, conservando
   struttura e contenuti corretti. Evitare riscritture generali, sezioni
   speculative e rigenerazioni in blocco. Riusare i diagrammi esistenti;
   eventuali nuove tavole soltanto draw.io, modificabili ed esportate.
5. Nessun commit, modifica software/test/SQL/dati, compilazione Java,
   test applicativo, DB o harness. Preservare integralmente vecchia relazione,
   evidenze e manifest 01–07. Non riscrivere risultati storici né eseguire
   vecchi verificatori sul documento aggiornato come controlli correnti.
   Difetti applicativi separati, senza dichiararli risolti.
6. Prima delle modifiche conservare qui specifica, HEAD, stato iniziale,
   baseline e copie necessarie al delta, inclusi non tracciati. Conservare
   poi matrice, comandi, risultati, correzioni, limiti e impronte.
7. Verificare realmente gli strumenti: `/tmp` può contenere strumenti/cache,
   mai specifiche o input editoriali indispensabili. Compilare il PDF completo
   e osservare tutte le pagine a risoluzione leggibile, indice e riferimenti
   inclusi. Rendering/assert non sostituiscono l'osservazione. Correggere,
   ricompilare e ricontrollare le pagine interessate. Valutare gli avvisi
   effettivi senza sopprimerli per ottenere un esito favorevole.
8. Preparare PDF completo e ZIP LaTeX con figure e tavole modificabili.
   Compilare i sorgenti estratti in directory temporanea senza dipendenze
   editoriali esterne. Documentare strumenti, prerequisiti e limiti.
9. Concludere con due revisori Standards e Spec in contesti nuovi indipendenti
   sul delta effettivo iniziale; correggere e far ricontrollare rilievi;
   conservare rapporti distinti e identificare artefatti finali revisionati.
10. Aggiornare tracker, README e piano distinguendo completamento tecnico,
    revisione degli autori da svolgere e limiti residui. Consegnare PDF, ZIP
    e verifiche; proporre agli autori senza approvazione, invio o pubblicazione.

Procedere autonomamente. La richiesta esplicita prevale sulle indicazioni
generiche di `implement` relative a test applicativi e commit.
