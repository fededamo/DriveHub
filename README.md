# DriveHub - Progetto di Ingegneria del Software

Avanzamento delle revisioni, decisioni e verifiche: [registro di revisione esterno al progetto](../PIANO_REVISIONI_DRIVEHUB.md). Terminologia: [glossario](GLOSSARY.md).

Applicazione desktop **JavaFX**, con **JDK21**, **Maven3.9+** e persistenza su
**PostgreSQL16**. Entità di dominio, servizi applicativi e DAO JDBC separano le
responsabilità; pattern e compromessi sono descritti sulla base dei sorgenti.

## 1. Preparazione e architettura

Per una nuova installazione seguire [installazione e avvio](docs/installation.md):
strumenti, ruolo/database, `application.properties`, schema e demo facoltativa.
`schema.sql` e `demo.sql` sono **distruttivi** su un database popolato e non fanno
parte dell'avvio ordinario. Dopo la preparazione, dalla radice del progetto:

```bash
mvn javafx:run
```

[Responsabilità, dipendenze e flussi](docs/architecture.md) distingue factory
statiche da Factory Method GoF, Observer/Visitor/Singleton da DAO/DI e composition
root, e motiva SOLID e l'ispirazione DDD con esempi e limiti. `AppContext`
costruisce i servizi; restano accessi globali negli overload delle viste.
La direzione delle dipendenze non coincide con il ritorno dei dati dal DB alla UI.
I [diagrammi correnti con sorgenti, render e riscontri](docs/diagrams/README.md)
descrivono classi, package, sequenze, stati, casi d'uso e navigazione.
L'indice distingue gli originali storici conservati dalle viste usate.

---

## 2. Regole di Business e Formule di Calcolo

### 2.1 Calcolo Prezzo Veicolo a Cascata

Il manager sceglie in **Veicoli** la modalità automatica (base del modello) oppure
una **base manuale**, poi salva con «Applica base alla selezione». La stessa scelta
vale quando aggiunge un veicolo. Riattivando «Automatico» e salvando torna al consigliato.
La modalità è persistita esplicitamente in `vehicle.price_mode`; `vehicle.price`
contiene una base, mai il risultato della formula. Le basi manuali vengono arrotondate
ai centesimi (HALF_UP) prima del calcolo, coerentemente con PostgreSQL. Una base manuale uguale a quella
del modello resta manuale anche se la base del modello cambia.

Il **prezzo consigliato** applica la formula alla base del modello; il **prezzo effettivo**
la applica una sola volta alla base selezionata. Ad esempio 10.000 € manuali e sconto
10%, senza altre riduzioni, danno 9.000 €. Catalogo, acquisto e tariffa di noleggio
usano il prezzo effettivo. La stima iniziale della cessione resta nella richiesta e
non imposta una base manuale: un nuovo veicolo usa l'automatico; uno già presente
conserva la scelta commerciale del manager. Vedere [ADR 0001](docs/adr/0001-base-manuale-e-prezzo-concordato.md).

All'avvio dell'acquisto il prezzo effettivo diventa il **totale concordato**, salvato
in `request.agreed_total` nella stessa transazione dell'acconto del 20% e della
prenotazione del veicolo. Dopo l'approvazione, «Paga saldo concordato e completa
acquisto» addebita il totale persistito meno l'acconto già versato e registra la
vendita per quel totale. Su 20.000 € e 4.000 € versati restano 16.000 €, anche dopo
cambi di base, modalità, sconti o anno. Cliente e venditore vedono i tre importi
nelle richieste rilette; la stima delle cessioni resta separata.
Schema, fixture e verifiche: [testing.md](docs/testing.md#totale-concordato-allacconto--ticket-06).


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
* Calcolo costo complessivo per $n$ giorni inclusivi di inizio e fine (serie geometrica):
  $$\text{totale} = (\text{prezzo\_attuale} \times 0.001) \times \frac{1 - 0.95^n}{1 - 0.95}$$
* Addebito $100\%$ immediato sul conto all'atto della conferma.

### 2.3 Gestione Concorrenza su Database
* **Acquisto**: prenotazione atomica da `SALE` a `BLOCKED`. Saldo, rifiuto e scadenza rileggono e bloccano la richiesta nella stessa transazione dei movimenti del conto e della transizione da `BLOCKED`: una richiesta genera un solo esito economico. Anche l'approvazione usa il lock della richiesta. [Protocollo e prove PostgreSQL](docs/testing.md#richieste-di-acquisto-elaborate-una-sola-volta--ticket-07).
* **Noleggio e Test Drive**: lock veicolo/cliente e disponibilità riletta nella transazione; conto protetto per gli addebiti, vincolo PostgreSQL contro periodi di noleggio sovrapposti. Conferma noleggio e cancellazione test drive futuri sono atomiche. Cambio destinazione e cancellazione identità conservano rimborsi e storico secondo i [flussi verificati](docs/architecture.md#flussi-economici-e-transazionali).

---

## 3. Demo facoltativa

Solo dopo il caricamento di `demo.sql`, la password delle identità della fixture
è **`password`**: cliente `mario.rossi@email.it`, venditore
`paolo.costa@drivehub.it`, manager `vittorio.manager@drivehub.it`.
Non è la password di tutti gli utenti registrati. Date e operazioni demo sono
storiche; le scadenze vengono elaborate all'accesso del cliente.

La registrazione pubblica crea nuovi clienti con conto dimostrativo a zero;
l'identificativo generato non certifica un IBAN bancario reale. Schema vuoto,
limiti e precauzioni della demo: [installazione](docs/installation.md).

## 4. Verifiche

```bash
# Compila e produce il JAR, senza eseguire test
mvn -DskipTests clean package
# Solo prove senza database
mvn test
# Suite completa: richiede configurazioni esplicite di test e sentinella
mvn -Ppostgres-tests clean test
```

Preparazione del PostgreSQL dedicato, sei variabili richieste, comandi mirati e
confini delle verifiche grafiche: [Build e test isolati](docs/testing.md).
Nessun test usa implicitamente il DB applicativo: il reset è consentito solo
su `drivehub_test` con ruolo dedicato e privilegi controllati.

Le [evidenze17](docs/evidence/r09-diagrams/verification.md) registrano baseline
locale, diff, render e nuove prove con DB sacrificabili e sentinella distinta.
Le [evidenze16](docs/evidence/r09-instructions/verification.md) conservano la
verifica d'installazione; tutti i risultati01–16 restano riferiti a quelle sessioni.
