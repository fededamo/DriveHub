# Versioni delle evidenze

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
