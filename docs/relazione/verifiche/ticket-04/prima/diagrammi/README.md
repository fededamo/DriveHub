# Diagrammi della relazione

Tavole progettate con la skill draw.io e riscontrate sul codice corrente,
12 novembre 2026. Non sono un'esportazione UML automatica. Ogni PDF contiene
anche l'XML modificabile nel campo Subject; il sorgente `.drawio` resta la
copia primaria per le modifiche.

| Vista | Sorgente modificabile | Esportazione inclusa nel PDF |
| --- | --- | --- |
| Casi d'uso cliente | [draw.io](casi-uso-cliente.drawio) | [PDF](casi-uso-cliente.drawio.pdf) |
| Casi d'uso venditore | [draw.io](casi-uso-venditore.drawio) | [PDF](casi-uso-venditore.drawio.pdf) |
| Casi d'uso manager | [draw.io](casi-uso-manager.drawio) | [PDF](casi-uso-manager.drawio.pdf) |
| Navigazione cliente | [draw.io](navigazione-cliente.drawio) | [PDF](navigazione-cliente.drawio.pdf) |
| Navigazione venditore | [draw.io](navigazione-venditore.drawio) | [PDF](navigazione-venditore.drawio.pdf) |
| Navigazione manager | [draw.io](navigazione-manager.drawio) | [PDF](navigazione-manager.drawio.pdf) |

Nei casi d'uso azzurro significa funzionalità applicativa, grigio supporto.
UC-04 è una sottofunzione di UC-03; gli altri UC-01–UC-11 sono obiettivi,
UC-S01–UC-S04 funzioni di supporto. Gli archi sono associazioni, senza
significato temporale. La partecipazione del venditore a UC-03 avviene
attraverso UC-04; UC-05 compare nelle viste di entrambi i partecipanti.

Nella navigazione i riquadri indicano tab, azioni e dialoghi/risultati, con
gli stessi identificativi delle schede. I risultati nella stessa scheda non
sono pagine nuove. Le tab sono selezionabili liberamente. Le righe dei
diagrammi non impongono un ordine fra le tab.

Per rigenerare l'XML dalla disposizione manuale conservata nelle verifiche:

```bash
python3 docs/relazione/verifiche/ticket-02/crea-diagrammi.py
```

La rigenerazione sovrascrive le tavole: dopo modifiche manuali in draw.io,
conservare il nuovo sorgente e aggiornare anche la disposizione nello script
solo se si vuole continuare a rigenerarlo. Per esportare una tavola con
draw.io Desktop 32.6.10:

```bash
drawio --disable-gpu -x -f pdf -e --crop -b 10 \
  -o docs/relazione/diagrammi/casi-uso-cliente.drawio.pdf \
  docs/relazione/diagrammi/casi-uso-cliente.drawio
```

`--crop` evita la suddivisione delle tavole su fogli predefiniti. Comandi
effettivi, strumenti temporanei e controlli sono nelle
[verifiche](../verifiche/ticket-02/README.md).

## Ticket 03 — Architettura

Tavole selettive disposte manualmente con la skill draw.io e riscontrate
sui sorgenti correnti, 12 novembre 2026. Tutti gli undici package di
produzione hanno una vista riconoscibile. Il package DAO è separato in
contratti e gestione transazionale. I sorgenti modificabili sono primari;
ogni esportazione è su una pagina e incorpora l'XML nel Subject del PDF.

| ID | Vista / sorgente | Esportazione inclusa |
| --- | --- | --- |
| PKG | [Package e dipendenze](package-architettura.drawio) | [PDF](package-architettura.drawio.pdf) |
| CL-01 | [Avvio e composizione](classi-avvio.drawio) | [PDF](classi-avvio.drawio.pdf) |
| CL-02 | [Identità e ruoli](classi-utenti.drawio) | [PDF](classi-utenti.drawio.pdf) |
| CL-03 | [Catalogo](classi-catalogo.drawio) | [PDF](classi-catalogo.drawio.pdf) |
| CL-04 | [Sconti](classi-sconti.drawio) | [PDF](classi-sconti.drawio.pdf) |
| CL-05 | [Operazioni](classi-operazioni.drawio) | [PDF](classi-operazioni.drawio.pdf) |
| CL-06 | [Servizi](classi-servizi.drawio) | [PDF](classi-servizi.drawio.pdf) |
| CL-07 | [Eventi](classi-eventi.drawio) | [PDF](classi-eventi.drawio.pdf) |
| CL-08a | [Contratti DAO](classi-dao.drawio) | [PDF](classi-dao.drawio.pdf) |
| CL-08b | [Gestore transazionale](classi-transazioni.drawio) | [PDF](classi-transazioni.drawio.pdf) |
| CL-09 | [PostgreSQL](classi-postgres.drawio) | [PDF](classi-postgres.drawio.pdf) |
| CL-10 | [Presentazione](classi-presentazione.drawio) | [PDF](classi-presentazione.drawio.pdf) |
| CL-11 | [Credenziali](classi-sicurezza.drawio) | [PDF](classi-sicurezza.drawio.pdf) |

PKG usa dipendenze tratteggiate; i legami trasversali sono dichiarati in
calce per evitare archi sovrapposti. Nelle classi triangolo vuoto continuo
significa generalizzazione, tratteggiato realizzazione; freccia aperta
continua significa associazione navigabile, tratteggiata dipendenza.
Molteplicità solo al bersaglio; nessun rombo di composizione. `ID`/`chiave`
indicano scalari. Campi e note completano gli archi selezionati.

[Disposizioni manuali](../verifiche/ticket-03/crea-diagrammi.py),
[matrice e fonti](../verifiche/ticket-03/matrice-copertura.md),
[riscontro delle relazioni](../verifiche/ticket-03/relazioni.md).
Per rigenerare soltanto le tredici tavole del ticket 03:

```bash
python3 docs/relazione/verifiche/ticket-03/crea-diagrammi.py
```

Come per il ticket 02, la rigenerazione sovrascrive le disposizioni manuali:
preservare eventuali modifiche fatte nell'editor prima di rilanciarla.
L'esportazione usa il medesimo comando draw.io sopra, sostituendo i nomi.
I verbali `*-esportazione.txt` del ticket 03 riportano i comandi effettivi.
