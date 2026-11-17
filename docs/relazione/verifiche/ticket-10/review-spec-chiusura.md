# Review Spec — chiusura dei rilievi del ticket 10

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
