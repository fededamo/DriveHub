# Spec — review indipendente ticket 06

Review sulla patch fissata della sola sessione del 31 ottobre 2026, rispetto alla
baseline locale comprendente i ticket precedenti: **0 rilievi**.

- Requisiti mancanti/parziali: nessuno. Totale e acconto salvati insieme all'addebito;
  saldo e vendita dagli importi persistiti. DAO, schema, fixture, UI e documentazione
  allineati.
- Scope creep: nessuno. CSS limitato alle due tabelle modificate; ticket 07/08/14
  esclusi dal diff del 06.
- Implementazione errata: nessuna individuata. Formula e base esplicita del 05,
  controlli e transazioni del 04 e distinzione delle cessioni preservati.

Esaminati ticket 01–05 chiusi, dominio/servizi pubblici, prove PostgreSQL di cambio
listino, nuova sessione, anno precedente, centesimi, fondi insufficienti e rollback.
Il revisore non ha eseguito test, usato database o modificato file.

## Conferma sul codice corrente — 2 novembre 2026

Dopo le modifiche del ticket 07, autorizzato e revisionato separatamente:
**SPEC 0 rilievi, confermato valido per chiudere il ticket 06.**

PurchaseService continua a salvare totale/acconto nella stessa transazione e usa
soltanto gli importi persistiti al completamento. PostgresRequestDao conserva
agreed_total in inserimento, rilettura e approvazione. Le modifiche del 07 sono
compatibili con l'accordo del 06. Evidenze 07: 163/163, inclusi 7 test di dominio
+ 12 PostgreSQL del 06, zero failure/errori/salti. Nessun nuovo rischio individuato.
Verifica mirata read-only, nessun nuovo test o accesso al database.
