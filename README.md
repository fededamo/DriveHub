# DriveHub - Progetto di Ingegneria del Software

Applicazione Desktop JavaFX con persistenza su **PostgreSQL 16**, sviluppata secondo i paradigmi del **Domain-Driven Design (DDD)**, i principi **SOLID** e i **Design Pattern GoF**.

---

## 1. Principi e Paradigmi Architetturali

### 1.1 Flusso a Strati e Persistenza On-Demand
Il software adotta una rigida architettura a strati:
$$\text{DAO (PostgreSQL)} \longrightarrow \text{Domain (Entità pure)} \longrightarrow \text{Business (Services)} \longrightarrow \text{Presentation (JavaFX)}$$

* **Single Source of Truth**: il database PostgreSQL è l'unica sorgente permanente di verità. La memoria RAM opera in modalità **On-Demand** (i dati vengono prelevati per il tempo necessario all'elaborazione e poi rilasciati).
* **Aggiornamento Interfaccia Posticipato**: l'interfaccia utente si aggiorna **solo dopo** il successo definitivo e il commit della transazione su database, garantendo rollback immediato in caso di fallimento o violazione di vincoli.

### 1.2 Design Pattern GoF Implementati
1. **Factory Method**: creazione protetta delle entità (`Vehicle.create()`, `Client.create()`, `Rent.create()`, ecc.) per verificare a priori tutti gli invarianti di dominio prima dell'istanziazione.
2. **Observer**: disaccoppiamento degli eventi di transizione stato del veicolo. Quando un veicolo viene venduto (`SOLD`) o noleggiato (`RENT`), l'evento notifica `TestDriveCancellationObserver` che cancella automaticamente tutti i test drive futuri su quel veicolo (priorità commerciale di vendita/noleggio).
3. **Data Access Object (DAO)**: separazione netta tra logica applicativa e persistenza relazionale JDBC, con gestione esplicita delle transazioni atomiche (`DatabaseManager.executeInTransaction`).
4. **Composition Root & Dependency Injection**: classe `AppContext` che assembla e inietta tutte le dipendenze al bootstrap del sistema senza accoppiamenti rigidi.

### 1.3 Rispetto dei Principi SOLID
* **Single Responsibility (SRP)**: classi separate e focalizzate (`PriceCalculatorService`, `PurchaseService`, `RentalService`, `PostgresVehicleDao`, ecc.).
* **Open-Closed (OCP) & Liskov Substitution (LSP)**: gerarchia utenti basata sull'interfaccia polimorfica `User` implementata da `Client`, `Salesman`, `Manager`. **Nessuna enumerazione discriminante sui ruoli**, garantendo estendibilità e comportamenti dedicati.
* **Interface Segregation (ISP)**: interfacce DAO granulari e dedicate per ogni aggregato del dominio.
* **Dependency Inversion (DIP)**: i servizi dipendono da astrazioni (`UserDao`, `VehicleDao`, `PasswordHasher`), non da implementazioni concrete.

---

## 2. Regole di Business e Formule di Calcolo

### 2.1 Calcolo Prezzo Veicolo a Cascata
Gli sconti non si sommano, ma agiscono in cascata sulla cifra progressivamente ridotta:
1. **Sconto Promozionale**: $\max(\text{scontoVeicolo}, \text{scontoModello})$:
   $$\text{prezzo} = \text{prezzo\_base} \times (1 - \text{sconto})$$
2. **Sconto Usato ($20\%$)**: se il veicolo ha targa valorizzata ($\text{targa} \neq \text{null}$):
   $$\text{prezzo} = \text{prezzo} \times 0.80$$
3. **Ammortamento Anno ($5\%$ annuo rispetto all'anno corrente)**:
   $$\text{prezzo} = \text{prezzo} \times (0.95)^{(\text{anno\_corrente} - \text{anno\_veicolo})}$$
4. **Ammortamento Chilometri ($5\%$ ogni $20.000\text{ km}$)**:
   $$\text{prezzo} = \text{prezzo} \times (0.95)^{(\text{km} / 20000)}$$

### 2.2 Tariffe Noleggio
* Costo base giornaliero: $0.1\%$ del valore attuale del veicolo ($\text{prezzo\_veicolo} / 1000$).
* Calcolo costo complessivo per $n$ giorni (serie geometrica):
  $$\text{totale} = (\text{prezzo\_attuale} \times 0.001) \times \frac{1 - 0.95^n}{1 - 0.95}$$
* Addebito $100\%$ immediato sul conto all'atto della conferma.

### 2.3 Gestione Concorrenza su Database
* **Acquisto**: aggiornamento atomico con clausola `WHERE id = ? AND state = 'SALE'::vehicle_state` per evitare sovrapposizioni o race condition tra clienti.
* **Noleggio e Test Drive**: verifica di disponibilità e inserimento nella stessa transazione con lock.

---

## 3. Account Demo Precaricati

Tutti gli account esistenti nel database hanno password: **`password`**

* **Cliente**: `mario.rossi@email.it`
* **Venditore**: `paolo.costa@drivehub.it`
* **Manager**: `vittorio.manager@drivehub.it`

È inoltre possibile registrare in tempo reale nuovi clienti dalla schermata iniziale: il sistema provvederà a generare in automatico un IBAN italiano standard e un conto corrente a saldo zero.

---

## 4. Avvio ed Esecuzione

Prerequisiti: **JDK 21** e **Maven 3.9+**.

### Avvio dell'Applicazione
```bash
mvn javafx:run
```
Oppure tramite doppio clic sul file `run.bat`.

### Esecuzione dei Test Unitari e di Integrazione
```bash
mvn test
```

