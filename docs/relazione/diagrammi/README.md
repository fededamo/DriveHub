# Diagrammi della relazione

Nel ticket 13 le undici tavole rifatte/riadattate del ticket 10 e le 32
viste di dettaglio sono riusate intatte. Verificati nativi, export e fonti,
con osservazione dei dodici fogli A3 e di tutte le pagine che li integrano:
[tavole e provenienza](../verifiche/ticket-13/tavole.md),
[43 coppie](../verifiche/ticket-13/diagrammi-43.json),
[confronto con gli hash finali del 10](../verifiche/ticket-13/confronto-export-10.json).
Nessuna nuova esportazione Desktop dichiarata; originali conservati.

## Ticket 10 — undici tavole riallineate

Le undici tavole originali sono state confrontate con basi, copie corrette,
codice e schema attuali, riadattate con la skill `drawio:drawio` ed esportate
con draw.io Desktop 32.6.10. [Registro completo delle corrispondenze,
fonti e controlli](../verifiche/ticket-10/tavole.md).
Quattro gruppi di classi e tre sequenze d'insieme sono nel corpo; le viste
preesistenti completano i dettagli. Le undici coppie sono su A3 vettoriale
(due fogli per ER); il nativo identico è allegato ai PDF. In totale restano
43 coppie native/PDF, comprese le 32 tavole di dettaglio conservate.
Gli originali in `docs/` e `Relazione_SWE` restano intatti.

| Nuova tavola d'insieme | Sorgente | Export |
| --- | --- | --- |
| 2.1 — casi-uso-autori | [draw.io](casi-uso-autori.drawio) | [PDF](casi-uso-autori.drawio.pdf) |
| 2.4 — navigazione-autori | [draw.io](navigazione-autori.drawio) | [PDF](navigazione-autori.drawio.pdf) |
| 2.5 — package-autori | [draw.io](package-autori.drawio) | [PDF](package-autori.drawio.pdf) |
| 2.6.1 — classi-dominio-insieme | [draw.io](classi-dominio-insieme.drawio) | [PDF](classi-dominio-insieme.drawio.pdf) |
| 2.6.2 — classi-business-insieme | [draw.io](classi-business-insieme.drawio) | [PDF](classi-business-insieme.drawio.pdf) |
| 2.6.3 — classi-dao-insieme | [draw.io](classi-dao-insieme.drawio) | [PDF](classi-dao-insieme.drawio.pdf) |
| 2.6.4 — classi-presentazione-insieme | [draw.io](classi-presentazione-insieme.drawio) | [PDF](classi-presentazione-insieme.drawio.pdf) |
| 2.7.1 — sequenza-vendita-insieme | [draw.io](sequenza-vendita-insieme.drawio) | [PDF](sequenza-vendita-insieme.drawio.pdf) |
| 2.7.2 — sequenza-noleggio-insieme | [draw.io](sequenza-noleggio-insieme.drawio) | [PDF](sequenza-noleggio-insieme.drawio.pdf) |
| 2.7.3 — sequenza-cessione-insieme | [draw.io](sequenza-cessione-insieme.drawio) | [PDF](sequenza-cessione-insieme.drawio.pdf) |
| 2.8.1 — database-er-autori | [draw.io](database-er-autori.drawio) | [PDF](database-er-autori.drawio.pdf) |

La sezione seguente descrive la versione storica del 15 novembre; i relativi
manifest e pacchetti restano conservati. Le tre esportazioni correnti sono
quelle verificate nel ticket 10, con allegato nativo, non il vecchio Subject.

## Tavole degli autori integrate — 15 novembre 2026

Le tre copie seguenti derivano direttamente dai file degli autori in `docs/`.
Conservano identificativi, forme, notazione, gerarchia e disposizione generale;
le correzioni riguardano contenuti superati, collegamenti e leggibilità.
Gli originali restano conservati senza modifiche.

| Originale | Copia corretta e inclusa | Esportazione |
| --- | --- | --- |
| `Page Navigation Diagram.drawio` | [Navigazione](navigazione-autori.drawio) | [PDF A3](navigazione-autori.drawio.pdf) |
| `Users SWE.drawio` | [Casi d'uso](casi-uso-autori.drawio) | [PDF A3](casi-uso-autori.drawio.pdf) |
| `SWE ER.drawio` | [ER e legenda](database-er-autori.drawio) | [PDF, due pagine A3](database-er-autori.drawio.pdf) |

Queste tavole d'insieme affiancano i dettagli già presenti. Il supertipo
`User` dell'ER è concettuale: il testo e la legenda distinguono le tre tabelle
di ruolo dalla vista SQL `user` e dai limiti dei vincoli globali.

Le esportazioni del 15 novembre usavano il viewer ufficiale draw.io eseguito solo su localhost,
con connessioni esterne bloccate, e stampa Chromium in A3. Il Subject di
ciascun PDF incorpora l'intero XML codificato come URL, incluse entrambe le
pagine dell'ER. Non è stato usato un renderer grafico riscritto per il progetto.
[Correzioni riproducibili](../verifiche/tavole-autori/aggiorna-tavole.py)
e [verifiche](../verifiche/tavole-autori/README.md).
Il generatore riparte dagli originali: non rilanciarlo dopo modifiche manuali
alle copie corrette senza prima conservarle. La compilazione usa i PDF inclusi.

## Tavole di dettaglio

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

## Ticket 04 — Sequenze e database

Tavole disposte manualmente con la skill draw.io, riscontrate su Java e
DDL PostgreSQL correnti il 12 novembre 2026. Identificativi stabili,
nessuna sovrascrittura delle tavole precedenti. Sorgenti primari modificabili;
esportazioni PDF con XML incorporato e intera tavola su una pagina.

| ID | Sorgente | Esportazione inclusa |
| --- | --- | --- |
| SQ-01a | [Vendita al cliente: avvio](sequenza-vendita-avvio.drawio) | [PDF](sequenza-vendita-avvio.drawio.pdf) |
| SQ-01b | [Vendita al cliente: esito](sequenza-vendita-esito.drawio) | [PDF](sequenza-vendita-esito.drawio.pdf) |
| SQ-01c | [Vendita al cliente: saldo](sequenza-vendita-saldo.drawio) | [PDF](sequenza-vendita-saldo.drawio.pdf) |
| SQ-01d | [Vendita al cliente: scadenza](sequenza-vendita-scadenza.drawio) | [PDF](sequenza-vendita-scadenza.drawio.pdf) |
| SQ-02 | [Noleggio](sequenza-noleggio.drawio) | [PDF](sequenza-noleggio.drawio.pdf) |
| SQ-OB | [Observer comune](sequenza-observer.drawio) | [PDF](sequenza-observer.drawio.pdf) |
| SQ-03a | [Cessione: proposta](sequenza-cessione-proposta.drawio) | [PDF](sequenza-cessione-proposta.drawio.pdf) |
| SQ-03b | [Cessione: esito](sequenza-cessione-esito.drawio) | [PDF](sequenza-cessione-esito.drawio.pdf) |
| ER-01 | [Catalogo e veicoli nelle operazioni](database-er-catalogo.drawio) | [PDF](database-er-catalogo.drawio.pdf) |
| ER-02 | [Identità e attribuzioni](database-er-identita.drawio) | [PDF](database-er-identita.drawio.pdf) |
| REL-01 | [Relazioni catalogo](database-relazionale-catalogo.drawio) | [PDF](database-relazionale-catalogo.drawio.pdf) |
| REL-02 | [Relazioni identità](database-relazionale-identita.drawio) | [PDF](database-relazionale-identita.drawio.pdf) |
| REL-03 | [Relazioni operazioni](database-relazionale-operazioni.drawio) | [PDF](database-relazionale-operazioni.drawio.pdf) |

Le sequenze separano ingresso attore/handler e dettaglio del servizio.
Blu: principale; rosso: alternativa; tratteggio: ritorno o linea di vita.
`alt`/`else`, `loop`, `ref` sono spiegati in 2.7. Le note «SQL», «sintesi»
e «memoria» sono etichette descrittive, non nuovi metodi. Le connessioni
sono locali alla tavola; le letture autonome sono marcate fuori dalla
transazione di scrittura. SQ-OB continua saldo e noleggio sullo stesso
thread e sulla connessione del chiamante prima del commit.

L'ER è decomposto per relazioni: rettangoli entità, rombi relazioni,
numero vicino a un'entità = sue istanze per una dell'altra. Le entità
ripetute e i rimandi ER-01/02 non sono copie persistenti. `PK`, `CK`,
`FK`, `UQ` e `?` hanno la legenda delle viste relazionali; la targa
nullable non è presentata come candidata dell'intera relazione.

[Matrice sequenze](../verifiche/ticket-04/matrice-sequenze.md),
[matrice dati](../verifiche/ticket-04/matrice-dati.md),
[comandi e verifiche](../verifiche/ticket-04/README.md).
Rigenerazione delle sole tredici tavole del ticket 04:

```bash
python3 docs/relazione/verifiche/ticket-04/crea-diagrammi.py
T04_DRAWIO=/percorso/drawio python3 docs/relazione/verifiche/ticket-04/esporta-diagrammi.py
```

L'esportatore esegue il repair pass della skill e usa `-x -f pdf -e --crop
-b 10`. La rigenerazione sovrascrive solo queste tredici disposizioni:
conservare eventuali cambiamenti manuali nell'editor prima di rilanciarla.
