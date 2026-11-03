# DriveHub

Vocabolario delle operazioni fra concessionaria e clienti. Acquisto e cessione indicano il punto di vista del cliente.

## Language

**Cliente**:
Persona che consulta il catalogo e richiede acquisti, cessioni, noleggi o test drive.

**Venditore**:
Addetto della concessionaria che valuta le cessioni e autorizza gli acquisti dei clienti.

**Manager**:
Responsabile del catalogo, delle basi di prezzo, degli sconti e della consultazione del registro economico.

**Acquisto del cliente**:
Operazione con cui un cliente compra un veicolo dalla concessionaria versando un acconto e poi il saldo.
_Avoid_: Acquisto senza specificare il punto di vista quando può indicare anche un'acquisizione della concessionaria.

**Cessione del cliente**:
Proposta con cui un cliente offre il proprio veicolo alla concessionaria; l'accettazione determina un pagamento al cliente.
_Avoid_: Vendita senza specificare chi vende.

**Prezzo consigliato**:
Valore ottenuto dalla base del modello applicando sconti e riduzioni per usato, anno e chilometri.

**Modalità del prezzo**:
Scelta persistente ed esplicita: automatica usa la base corrente del modello;
manuale usa la base scelta dal manager, anche se numericamente uguale a quella del modello.

**Base manuale**:
Valore scelto dal manager al posto della base del modello, su cui applicare la stessa formula del prezzo consigliato.
_Avoid_: Prezzo manuale finale.

**Prezzo effettivo**:
Importo risultante dalla formula applicata alla base selezionata, del modello o manuale.

**Totale concordato**:
Prezzo dell'acquisto fissato al versamento dell'acconto, che non cambia per successive variazioni di basi o sconti.

**Acconto**:
Pagamento iniziale pari al 20% del totale concordato.

**Saldo da pagare**:
Differenza tra totale concordato e acconto già versato.
_Avoid_: Saldo senza contesto quando può indicare il denaro disponibile sul conto.

**Conto dimostrativo**:
Disponibilità monetaria del cliente usata dall'applicazione per addebiti, accrediti e rimborsi.
_Avoid_: Account quando si intende il conto monetario anziché l'identità di accesso.

**Noleggio in corso**:
Noleggio il cui periodo comprende la data corrente e che impedisce di destinare il veicolo alla vendita.

**Test drive**:
Prenotazione di un giro di prova. Alla conferma di un noleggio le prenotazioni future del veicolo vengono annullate e quelle passate conservate.

**VIN**:
Numero di telaio che identifica un veicolo anche quando viene ceduto nuovamente alla concessionaria.
