# Review Spec — ticket 10

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
