# Review finale indipendente — ticket 10

16 novembre 2026. Skill `mattpocock-skills:code-review`, due revisori con
contesti iniziali separati e due assi mantenuti distinti. Punto fisso:
working tree iniziale di `baseline.json` e `baseline-editoriale.tar.gz`,
inclusi file non tracciati e PDF precedente. HEAD iniziale
`61ce18ae1d4e0458b63e970b793b0b3a634a3adf`; nessun commit successivo.
Il comando `git diff 61ce18ae1d4e0458b63e970b793b0b3a634a3adf...HEAD`
sarebbe vuoto: il confronto effettivo è `delta-documentale.diff` con
`delta-files.json`, prodotto confrontando le copie e le impronte iniziali.
Sono incluse le aggiunte e le modifiche non tracciate.

Le tre correzioni Spec sono state realizzate nei nativi con la skill drawio,
riesportate e incluse in una nuova compilazione completa in tre passaggi.
Entrambi i revisori hanno ricontrollato indipendentemente le correzioni e
le evidenze finali. I rapporti qui sotto sono riportati integralmente, salvo
il titolo, e rimangono anche file autonomi. Gli stati documentali sono
aggiornati dopo i due esiti di chiusura.

## Standards

### Prima review

[Rapporto autonomo](review-standards.md).

16 novembre 2026. Revisione indipendente, limitata all'asse Standards.
Punto fisso: working tree iniziale di `baseline.json`, 3.053 file,
HEAD `61ce18ae1d4e0458b63e970b793b0b3a634a3adf`. Il diff tra commit è
vuoto; sono stati esaminati il delta documentale dalla baseline e i nuovi
materiali del ticket, inclusi script, nativi, esportazioni ed evidenze.

## Regole applicate

- `docs/AGENTS.md:3`: piano e indice come riferimenti editoriali;
  `docs/relazione/piano.md:8`: materiali nell'area della relazione;
  righe 15–16: intervento soltanto documentale.
- `docs/agents/domain.md:7` e `:17`: glossario, ADR pertinenti,
  riscontro corrente e attribuzione delle verifiche alla versione esaminata.
  Consultati GLOSSARY, ADR 0001–0002, README, piano, indice e matrice.
- `docs/relazione/allineamento.md:29`: skill drawio, conservazione dei
  nativi e degli export; `:32`: basi degli autori; `:47`: verifica semantica,
  esportazione e osservazione del PDF documentate per ogni tavola.
- `docs/agents/issue-tracker.md:14`: chiusura dopo verifica dei criteri;
  `docs/relazione/LEGGIMI-consegna.md:5`: revisione degli autori ancora
  necessaria. Consultate anche le etichette triage. Nessun
  `CODING_STANDARDS.md` o `CONTRIBUTING.md` reperito; assente
  `docs/adr/README.md`, senza introdurre convenzioni sostitutive.

## Riscontri e rilievi

**Nessuna violazione documentata rilevata.** Il controllo indipendente
in sola lettura trova zero file iniziali mancanti e 18 modificati, tutti
nell'area editoriale autorizzata: originali, capitolo 1, software, schema,
dati e vecchie evidenze conservati. Archivio iniziale di 210 copie con
SHA-256 coerente; corrispondono anche gli hash delle undici coppie native/PDF.

`tavole.md`, `confronto.md` e `controllo-visivo.md` registrano basi,
adattamenti e limiti; `comandi.md` distingue tentativi, compilazione finale
e verifiche documentali dalle prove applicative storiche. La review precede
l'aggiornamento degli stati: nessuna falsa approvazione degli autori.
Gli ZIP e le istruzioni precedenti restano esplicitamente storici.

Euristica Fowler, distinta dagli standard: **nessun code smell concreto
da segnalare**, proporzionando il giudizio agli script documentali locali.
Sintassi dei quattro Python verificata mediante parsing AST; nessun
formatter prescritto, nessuna astrazione ulteriore necessaria.

**Sintesi: 0 rilievi Standards, 0 criticità aperte su questo asse.**

Non rieseguiti esportazione, compilazione, checker che scrive report o test
applicativi; esaminate le evidenze finali disponibili. Restano i cinque
messaggi TeX preesistenti in C, dichiarati e valutati nel verbale visivo.

### Ricontrollo delle correzioni

[Rapporto autonomo](review-standards-chiusura.md).

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

## Spec

### Prima review

[Rapporto autonomo](review-spec.md).

16 novembre 2026. Asse Spec della skill `mattpocock-skills:code-review`.
Confronto: working tree iniziale conservato in `baseline.json` e
`baseline-editoriale.tar.gz` → sorgenti correnti, tramite
`delta-documentale.diff` e aggiunte in `delta-files.json`. HEAD
`61ce18ae1d4e0458b63e970b793b0b3a634a3adf`; il confronto fra commit è vuoto.

**Esito iniziale: tre rilievi P2 sulle tavole**, comunicati prima delle
correzioni. Il presente rapporto conserva la prima osservazione; la loro
chiusura richiede riscontro di nativi ed esportazioni rigenerate.

1. **Due callback rappresentate come un solo tipo UML.** Nella prima versione
   di `diagrammi/classi-dao-insieme.drawio:16`, il nodo `callbacks` è una
   singola «interface callback annidate», con due nomi nel compartimento
   attributi e `execute(Connection): void / T`. Il codice definisce invece
   due interfacce distinte (`src/main/java/it/unifi/drivehub/dao/DatabaseManager.java:54–60`).
   Separare tipi e firme, come già fa CL-08b. Requisito: «Aggiornare classi,
   attributi, relazioni» (`allineamento.md:43–46`; ticket `10:35–39`).

2. **Freccia verso un metodo del collaboratore sbagliato.**
   `diagrammi/sequenza-cessione-insieme.drawio:127`, `msg8`, invoca
   `calculateEstimate(...)` sul Calcolatore. Il metodo appartiene a
   `SaleToDealershipService.java:55`; la chiamata effettiva al calcolatore è
   `estimateVehicleValue(...)` (`SaleToDealershipService.java:64`,
   `PriceCalculatorService.java:43`). Correggere l'etichetta. Requisito:
   «flussi e transazioni sui sorgenti» (`allineamento.md:43–46`).

3. **Dipendenze corrette nel nativo, ambigue nell'esportazione.**
   `diagrammi/package-autori.drawio:94–102`, archi `t10-dep-3`/`t10-dep-5`:
   nella pagina fisica 41 la verticale condivisa attraversa i nomi
   `domain.user` e `dao.postgres`, facendo apparire dipendenze intermedie
   inesistenti. Instradare gli archi fuori dai riquadri estranei. Requisito:
   correttezza e leggibilità dell'esportazione (`allineamento.md:47–51`).

Nessun altro requisito mancante riscontrato: confronto sostanziale con i
tredici input originali attivi; 47 voci, sei schede nell'ordine richiesto,
nove aggiuntive, diciotto funzioni UI, undici tavole, quattro gruppi di
classi e dettagli B, tre sequenze, tredici relazioni e 62 attributi.
Prezzi, accordo, identità, enum, concorrenza e limiti restano trattati.
Nessuno sconfinamento applicativo; cap. 4/C modificano soltanto rinvii.

Letti codice/schema e registri; osservate le dodici pagine delle undici
tavole. Nessuna nuova compilazione o prova applicativa/DB/UI eseguita.
Ticket 11–13 e approvazione degli autori restano separati.

### Ricontrollo delle correzioni

[Rapporto autonomo](review-spec-chiusura.md).

16 novembre 2026. **Spec: PASS; zero rilievi aperti.**
Ricontrollo indipendente delle tre correzioni segnalate in
`review-spec.md`, conservato senza modifiche. Requisiti applicati:
correttezza rispetto ai sorgenti e leggibilità dell'esportazione
(`allineamento.md:43–51`; ticket `10:35–46`).

1. **Callback DAO: chiuso.**
   `diagrammi/classi-dao-insieme.drawio:16–37` contiene due interfacce
   separate, `TransactionConsumer` e `TransactionFunction<T>`, con
   `execute(Connection): void` e `execute(Connection): T` e dipendenze
   distinte da `DatabaseManager`. Corrispondono a
   `src/main/java/it/unifi/drivehub/dao/DatabaseManager.java:54–60`.
   Nomi, firme e connettori sono leggibili nella pagina fisica 47.

2. **Metodo della sequenza: chiuso.**
   `diagrammi/sequenza-cessione-insieme.drawio:127–132` indica ora
   `estimateVehicleValue(...)` sulla freccia dal servizio al calcolatore,
   coerentemente con `SaleToDealershipService.java:64` e
   `PriceCalculatorService.java:43`. Il PDF, pagina fisica 65, contiene
   la nuova etichetta e non contiene la precedente `calculateEstimate`.

3. **Routing dei package: chiuso.**
   `diagrammi/package-autori.drawio:101–128` instrada separatamente
   avvio → `dao.postgres` e `service` → contratti DAO. Nella pagina
   fisica 41 le frecce raggiungono i bersagli effettivi senza attraversare
   i nomi `domain.user` o `dao.postgres`; sono riconoscibili anche le
   dipendenze interne ai contenitori.

Osservate nuovamente le tre pagine rigenerate. Verificato in memoria con
PyMuPDF/Pillow che i rendering osservati coincidano pixel per pixel con
le pagine del PDF corrente e che il testo coincida con le corrispondenti
pagine dell'estratto. PDF completo: 129 pagine; estratto: 96.

SHA-256 degli artefatti esaminati:

- `relazione-drivehub.pdf`: `590e7a4be631e7f93f46c701f37cc6d4959dc77c962efd97bc977da4bd0c7e03`.
- `capitolo-2-appendici-A-B.pdf`: `9883e0f08226d43e8c516b630281bf8e5347f4015360e08a64ee0f435eef0b48`.

Nessuna nuova compilazione o prova applicativa, UI o DB eseguita.
Questo ricontrollo non anticipa i ticket 11–13, la verifica del successivo
ZIP o l'approvazione degli autori.

Sintesi per asse: **Standards 0 rilievi aperti; Spec 0 aperti, con 3 P2 iniziali risolti**.

La chiusura tecnica non costituisce approvazione degli autori. Restano i
cinque messaggi TeX preesistenti in C, osservati e dichiarati nel verbale
visivo. Nessun test applicativo, UI o DB eseguito in questa attività.
