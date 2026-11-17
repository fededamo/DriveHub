# Fonti tecniche e confini

17 novembre 2026, HEAD `61ce18ae1d4e0458b63e970b793b0b3a634a3adf`.
Lettura statica: nessuna esecuzione Java, Maven, JUnit, GUI o SQL.
Gli intervalli sono del sorgente originale corrente, non di un riassunto.
[Impronte delle fonti](fonti.json), [estratti letterali e omissioni](frammenti.json).

| Argomento | Preparazione/chiamata/oracolo effettivi e fonte | Limite |
| --- | --- | --- |
| 4.1/4.1.1 | POM19–24,41–64,112–142; sei classi senzaDB e17postgres con setup letto, TestDatabase31–106, ConfigurationTest | Nomi/tag spiegano selezione, il corpo spiega classificazione; nessun altro ciclo Maven. |
| 4.2.1 | DomainInvariants32–80: mutazione rifiutata e tutti attributi conservati;105–138: sconti/anni;143–177: orari/conto/date. BookingRules14–28:16+microsecondo e estremi inclusivi | Il conto asserisce200 prima del rifiuto, non dopo: il testo evita l'asserzione inesistente. Sconto veicolo ha25/60, non gli stessi estremi del modello. |
| 4.2.2 | AuthServiceIntegration19–104: reset, login, changeEmail, helper nuova lettura e sei attributi. AuthService33–76: registrazione separata, hash prima del callback | Cinque coppie duplicate sono modificaemail, non registrazione. Assente equivalente unitario e prova dedicata registro/conto atomici. |
| 4.2.3 | PriceCalculatorServiceTest21–81,93–166: basi, massimo/deprezzamenti/centesimi/geometrica; PriceCalculatorService26–111: formule. PBKDF2 classa corrente16–86, AppContext costruzione, login/credenziali AuthService | Non robustezza numerica esaustiva o benchmark; double nelle formule. Nessun vecchio pbkdf2RoundTrip isolato. |
| 4.2.4 | RequestAgreement17–70; SaleType16–72 in memoria; PurchaseAgreement26–78,87–116,159–190; PurchaseConcurrency35–85,172–179,267–285; SaleOutcome32–75,261–287,313–365; SaleProposal81–112 e fixture | Rifiuto100% distinto da scadenza20% dell'acconto.0,03 viene impostato SQL mantenendo saldo46.000. Zero nel dominio/SQL non successo pagamento. |
| 4.2.5 | TestDriveCancellation25–35,85–116,150–181: reset, prepareOperation, confirm, nuova lettura Rent/date/saldo; BookingConcurrency31–58,127–174: gare e riletture; VehicleDestination153–215: rimborso effettivo osservato prima del conto mancante | Non semplice oggetto restituito. Noleggio non crea automaticamente Sale; non sistema bancario. |
| 4.2.6 | DatabaseManager66–106; TestDriveCancellation40–80: trigger dopo addebito/operazione/cancellazioni e riletture;121–147: confine microsecondo; IdentityDeletion309–323: eliminazioni reali e sequenza diagnostica | Contratto causa/soppressa/ordine S0. GuastoSQL nel callback diverso da commit/rollback programmati su mock assenti. |
| 4.3.1 | PriceSelection25–30,61–69; CatalogService178–198, PostgresVehicleDao save/findById; PurchaseAgreement62–78 e helperrequest; OperationQueryService31–52; PostgresRequestDao147–168,214–226; PostgresSaleDao94–134; SaleOutcome353–365; PurchaseAgreement199–220; PostgresEnum16–53 | Nuovo AppContext non nuova classe UnitOfWork; DAO lettura autonoma apre Connection dopo commit. NULL del totale PURCHASE ha assert nella prova SQL; timestamp non tutto asserito per precisione. Selezione non tuttiDAO. |
| 4.3.2 | TestDatabase31–106; DemoSqlValidation16–49; DatabaseIsolation16–54; schema.sql e demo.sql; PurchaseConcurrency289–355 |13 tabelle e sequenze confrontate dopo reload, non migrazione. Sentinella JUnit solo marchio; attesePID reali, timeout/cleanup, non universalità deadlock. |
| 4.4 | FullWorkflow26–115: login/letture e scadenza; CatalogListing27–81: nuove righe/prezzi; PurchaseAgreement37–78: BLOCKED/SOLD sullo stesso scenario | Non tutti flussiE2E o GUI. Nessuna prova pubblica registrazione o vecchie classi Layering/FXML. |
| Harness | TableLifecycle94–155; ReactiveTables79–109, cells chiamato in TableLifecycle139/142/144/152/154; SlowModels34–68; Agreement amounts/checkDialog; Identity130–167; AppStartup main; R11 launcher/patch/manifest/log | E3storico, helper Reactive riusato non main aggiunto ai7esiti. SaleOutcome47/144/150 incompatibile ACQUISITION/-16000. Non aggiornato né compilato. Listener AsyncLoad distacco soloS0. |
| 4.5/C | docs/testing (comandi correnti), E1verification40–46 e full-suite33–180/targeted52–68; E2full.log/XML e manifest; E3log/input/patch | [Riscontro storico nuovo](riscontro-storico.json) ricavato dalle fonti, non copia di un esito06. Assenza XMLmetodo/runtime manifest E1 conservata. |

C rimane la matrice estesa verso UC01–06 in2.2, UC07–11/S01–04 inA e storieS.
La nota su SaleTypeTest distingue ora il veicolo nullo asserito dal venditore
nullo della factory. Le altre associazioni e le lacune06 sono conservate.
Capitoli2/3, A/B, glossario e ADR0001/0002 sono riferimenti immutati.

I frammenti nuovi mostrano intervalli integrali senza interpolazioni: le parti
esterne omesse sono descritte nel manifest e nel corpo. La notifica a0,02 resta
letterale, incluso€: il suo solo listato usa Latin Modern Mono OpenType, già
presente nella distribuzione, perché il font T1 originale non aveva il glifo.
Nel confronto PDF sono normalizzati spazi, legature e il meno tipografico;
il confronto LaTeX/sorgenti resta byte per byte sul testo UTF-8.
