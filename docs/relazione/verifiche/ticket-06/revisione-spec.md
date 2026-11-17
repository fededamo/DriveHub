# Revisione indipendente Spec — ticket 06

**13 novembre 2026 — CONFORME: zero rilievi Spec aperti.**
Contesto nuovo e separato, senza consultare il rapporto Standards corrente.
Specifica: [richiesta operativa](specifica-ticket.md), punti 1–12; lette le
istruzioni documentali richiamate, specifica generale, indice, piano,
glossario, README, architettura, ADR 0001–0002, testing/installation e
revisioni/segnalazioni pertinenti 04–05.

Punto fisso verificato con `git rev-parse`:
`61ce18ae1d4e0458b63e970b793b0b3a634a3adf`, coincidente con HEAD.
`git diff 61ce18ae1d4e0458b63e970b793b0b3a634a3adf...HEAD` e
`git log 61ce18ae1d4e0458b63e970b793b0b3a634a3adf..HEAD --oneline`
sono vuoti. Esaminati delta documentale, copie iniziali e nuovi materiali 06:
la documentazione già non tracciata richiede questo confronto effettivo.

Non emergono requisiti mancanti/parziali del ticket documentale,
ampliamenti estranei o associazioni apparentemente implementate ma errate.

- **Punti 2–7:** cinque sezioni nell'ordine concordato e C dopo B.
  Matrice UC/alternative→metodi/assert→S0/E1–E3 con lacune esplicite.
  Campioni verificati direttamente: accordo 20.000/4.000/16.000 e nuove
  letture (`PurchaseAgreementIntegrationTest:37–78`); guasto dopo addebito
  (`:162–190`); eliminazioni effettive e sequenza diagnostica
  (`IdentityDeletionIntegrationTest:309–323`); gate/PID/timeout
  (`PurchaseConcurrencyIntegrationTest:293–354`); sentinella limitata al
  marchio (`DatabaseIsolationIntegrationTest:47–54`); quattro enum
  (`PostgresEnumIntegrationTest:16–53`). Verificato che TableLifecycle
  richiami davvero `cells`; lifecycle statico e GUI storica restano distinti.
- **Punto 8:** ricalcolati dai log E1 423/0/0/0 e dagli XML E2 394/0/0/0,
  senza sommarli; confermati 28 input E2 diversi. Diff src/POM fra tree E1 e
  HEAD vuoto; assenza di manifest runtime E1 dichiarata. SaleOutcome storico
  incompatibile, tentativi GUI falliti e limiti della sentinella sono espliciti.
- **Punti 9–12:** verificati autonomamente 2234 hash intatti, unica frase
  amministrativa nel capitolo 3 e tre frammenti Java/LaTeX esatti.
  Difetti applicativi separati, capitolo 5 provvisorio, proposta senza approvazione.

PDF aperto autonomamente: 105 pagine, identico a `build/main.pdf`;
estratto 4/C di 14 pagine. SHA-256:
`05aed422b71cf85a5532ff4fab49549d4605add90ad20fe8a397c6720ef844b2`.
Letti log di compilazione/controlli e verbale finale dell'osservazione
effettiva delle 14 nuove pagine da parte del principale; correzioni finali
RETURN NULL e perimetro AppStartup riscontrate nei sorgenti.

Nessun test applicativo, Java, accesso DB, harness o commit; verifiche
documentali esclusivamente read-only, salvo questo rapporto.
Non ripetuta personalmente l'osservazione di tutte le pagine: nessuna
certificazione generale del software o approvazione degli autori.
