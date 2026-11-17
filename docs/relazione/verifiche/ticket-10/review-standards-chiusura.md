# Review Standards — ricontrollo conclusivo del ticket 10

16 novembre 2026. Asse Standards soltanto; il rapporto iniziale resta
conservato. Ricontrollate le correzioni successive alla review Spec,
i due script aggiornati e le evidenze finali, senza rigenerarle.
Punto fisso invariato: working tree di 3.053 file in `baseline.json`,
HEAD `61ce18ae1d4e0458b63e970b793b0b3a634a3adf`.

Regole: `docs/relazione/piano.md:8` e `:15` limitano area e perimetro
documentale; `docs/agents/domain.md:17` richiede riscontro sui sorgenti;
`docs/relazione/allineamento.md:29`, `:43` e `:47` richiedono nativi,
export aggiornati, correttezza e controllo visivo. Restano applicabili
le altre fonti censite nella review iniziale.

Le due callback DAO hanno ora riquadri e firme distinti, coerenti con
`DatabaseManager.java:54` e `:59`. La sequenza usa
`estimateVehicleValue`, riscontrato in `SaleToDealershipService.java:63`
e `PriceCalculatorService.java:43`. Nel package gli archi `t10-dep-3/5`
hanno parent comune corretto e punti di raccordo esterni ai nodi estranei.
Osservati indipendentemente i rendering finali delle pagine fisiche
41, 47 e 65: correzioni visibili, senza le precedenti ambiguità.

L'esportatore continua a leggere soltanto i nativi e a verificarne
l'impronta invariata; profilo separato per tavola e
`--disable-dev-shm-usage` non ampliano i materiali modificati.
Parsing AST riuscito per entrambi gli script aggiornati.
Il trasferimento della toolchain dalla tmpfs, i tentativi falliti e
la ripresa sono dichiarati in `comandi.md:130`, senza attribuirli
a prove applicative.

Controllo indipendente in sola lettura: zero file iniziali mancanti,
18 modificati coerenti con `delta-files.json`, nessuna modifica a
originali, capitolo 1, software, schema, dati o verifiche precedenti.
Archivio iniziale con SHA-256 coerente; tutte le undici coppie native/PDF
hanno hash aggiornati, allegati identici ai nativi e log di export riusciti.
I tre log finali attestano 129 pagine; l'esito documentale registra
estratto di 96 pagine, 60 riferimenti e capitolo 1 identico.

**Esito: 0 rilievi Standards aperti.** Nessun nuovo code smell concreto
secondo l'euristica Fowler proporzionata a questi script documentali.
Stati e approvazione degli autori non anticipati dalla review.

Non rieseguiti export, compilazione, checker che scrive report o prove
applicative/UI/DB. Restano cinque avvisi TeX preesistenti dichiarati in C;
la toolchain temporanea non costituisce una distribuzione autonoma.
