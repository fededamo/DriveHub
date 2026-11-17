# Limiti applicativi e strumenti storici

17 novembre 2026: sola lettura statica, nessuna riproduzione o patch.
Restano separati i riscontri04/05/06: importi zero ammessi da dominio/schema ma
rifiutati dai movimenti, unicità email non globale/concorrenziale e refreshUI
fallibile dopo commit nello stesso try. Nessun nuovo difetto produttivo dimostrato.

L'harness storico SaleOutcome usa ACQUISITION e-16000, incompatibili con
SaleType corrente e importi non negativi; il launcherR11 lo compila anche
quando seleziona altri controlli. Il limite06 è confermato leggendo le
righe47/144/150 e il launcher: nessuna compilazione tentata o modifica.
Destination ha un cast adattato soltanto nella copia storicaR11; patch e
manifest restano distinti dall'originale. La semanticaE3 non viene aggiornata
per analogia con il checkout corrente.

Lacune di prova, non nuovi bug: registrazione pubblica atomica cliente/conto,
PBKDF2isolato, guasti commit/rollback dedicati, distacco/logout/riapertura con
query pendente, tutti i workflow/gesti/UI/DAO, assenza globale di deadlock.
Il ticket 12 documenta questi confini senza colmarli con software o test nuovi.
