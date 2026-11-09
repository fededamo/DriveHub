# Versioni delle evidenze

La verifica corrente dopo l'uniformazione dei quattro enum PostgreSQL è in
[postgres-enums-2026-11-09](postgres-enums-2026-11-09/verification.md): contratto
SQL, DAO, suite completa, prova su copia e aggiornamento locale. Le prove di
consolidamento seguenti descrivono lo stato precedente a questa modifica.

La verifica precedente dopo consolidamento del database e rimozione dello script
è in [db-consolidation-2026-11-09](db-consolidation-2026-11-09/verification.md).
Le prove di conversione e il relativo harness sotto `data-conversion-03/`
richiedono il checkout storico `d31a1ce`: script e fixture sono stati ritirati
successivamente su richiesta dell'utente. I log precedenti restano immutati.


La conversione dal contratto precedente a entrambi i ticket e il risultato
integrato sono verificati nel [ticket 03](data-conversion-03/verification.md).
Le prove del [ticket 02: registro della concessionaria](sale-semantics-02/verification.md)
restano conservate per quella versione.
La verifica del solo ticket 01 resta in [request-semantics-01](request-semantics-01/verification.md);
gli altri risultati sotto descrivono le rispettive versioni storiche.

La [fotografia ZIP dei registri esterni](revision-records.zip) conserva piano,
tracker e audit delle revisioni, con manifest SHA256. Gli originali di lavoro
restano nella directory genitore di `DriveHub/`; l'archivio consente di recuperarli
senza spostare il tracker nel progetto. Non include `Relazione_SWE`.

La verifica integrata18 è in [r11-integrated/verification.md](r11-integrated/verification.md),
con [matrice AC01–AC20](r11-integrated/acceptance-matrix.md), baseline locale
comprensiva di ignorati pertinenti, input, diff della sola sessione e risultati
effettivi. I [diagrammi usati](../diagrams/README.md) sono solo sotto
`docs/diagrams/current/`; il render18 è distinto da quello17.

Tutte le altre directory di evidenza conservano i risultati **storici delle
rispettive sessioni**, con i relativi manifest e limiti. Non sono nuove esecuzioni18.
In particolare `394/394` di [r09-instructions](r09-instructions/verification.md)
è il risultato del16; anche il394/394del17 resta storico. Gli script/harness possono essere riusati, registrando
nuovi ambiente, input e risultati; i vecchi output non diventano correnti.

`full-stack/` e `jdk21-test/` appartengono a una versione precedente con package
`it.unifi.ing.drivehub`. I loro `source-manifest.json`, summary e report XML
(compreso LayeringTest) non provano il codice `it.unifi.drivehub` attuale.
Restano immutati per consultazione: [inventario e confronto17](r09-diagrams/historical-artifacts.json).
Nessuna percentuale di copertura si ricava dal numero dei test riusciti.

La chiusura18 e gli eventuali residui sono dichiarati soltanto nelle evidenze18
e nel ticket, sulla base delle prove realmente eseguite.
