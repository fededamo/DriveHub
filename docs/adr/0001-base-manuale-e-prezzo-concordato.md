# Base manuale nella formula e totale concordato all'acconto

Decisione accettata dall'utente il 31 ottobre 2026, domande Q4, Q7 e Q8 del [registro di revisione esterno al progetto](../../../PIANO_REVISIONI_DRIVEHUB.md). La concessionaria deve offrire sia il prezzo consigliato sia una base manuale scelta dal manager: il manuale sostituisce la base del modello nella stessa formula, non rappresenta il totale finale. Questo conserva gli effetti di sconti e svalutazioni anche sui valori inseriti manualmente, a differenza dell'alternativa di un prezzo finale fisso.

Il totale dell'acquisto viene però fissato quando il cliente versa l'acconto, indipendentemente dalla base scelta. Basi e sconti successivi valgono per nuovi accordi, mentre il saldo usa il totale già concordato: su 20.000 € con acconto di 4.000 €, restano 16.000 €. Servono quindi scelta della base e totale concordato distinti, aggiornando dominio, persistenza, fixture, interfaccia e test.

La raccomandazione dell'audit #17 di eliminare il prezzo manuale è superata dal requisito esplicito. Scelta della base implementata e verificata nel ticket 05; totale concordato persistito e usato al saldo nel ticket 06. Le altre garanzie di R04 restano ai ticket 07 e 08.
