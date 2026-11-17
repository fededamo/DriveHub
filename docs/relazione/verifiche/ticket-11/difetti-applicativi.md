# Limiti applicativi riscontrati — nessuna patch

16 novembre 2026. Solo letture dei sorgenti correnti, nessuna riproduzione
applicativa, DB o UI. I riscontri del [ticket 05](../ticket-05/difetti-applicativi.md)
restano intatti e non vengono presentati come nuove prove.

- **Movimenti zero:** Request/Sale e formule ammettono zero, BankAccount
  deposit/withdraw richiedono importi positivi. I servizi chiamano comunque
  quei movimenti: una cessione zero è proponibile ma l'accettazione fallisce;
  lo stesso confine riguarda acconti, residui, costi o rimborsi arrotondati
  a zero. Fonti: BankAccount; PurchaseService 85–93/165–168/208–215;
  SaleToDealershipService 126–136; RentalService 105–115.
- **Identità ed email:** sequenza comune e PK locali non impediscono ID
  manualmente coincidenti fra ruoli. La vista per solo ID prende la prima
  riga; LOWER in ricerca non coincide con UNIQUE/trigger esatti. I trigger
  escludono anche l'ID uguale al nuovo. Non è dimostrata unicità globale
  case insensitive sotto ogni scrittura/concorrente. Fonti: schema 147–235,
  PostgresUserDao 25–81 e resolveTable.
- **Esito e refresh:** la UI può intercettare nello stesso try un guasto
  di rilettura successivo al commit e mostrare un errore dopo il successo
  economico. Fonti: ClientDashboardView handlePurchaseAction e
  handleCompletePurchaseAction. Il commit non viene annullato dal refresh.

Altri confini espliciti, senza audit esaustivo: letture autonome del listino,
assenza di autorizzazione generale della sessione nei servizi, lock specifici
senza garanzia globale di assenza di deadlock, cancellazione Task che non
garantisce l'arresto immediato JDBC. Un guasto commit/rollback/close non è
stato simulato. Nessun intervento al software è incluso nel ticket 11.
