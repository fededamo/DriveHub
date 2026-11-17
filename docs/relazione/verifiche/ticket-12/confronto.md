# Confronto originale / prima stesura / ticket 12

17 novembre 2026. Punto fisso: [baseline iniziale](baseline.json) e
[copia editoriale](baseline-editoriale.tar.gz), inclusi non tracciati e PDF di 143 pagine.
Originale: `Relazione_SWE/capitoli/4_testing.tex`, incluso dal main originale,
senza altri input interni. Corrente: `capitoli/4_testing.tex`, anch'esso autonomo;
C entra dal contenitore appendici. Nessuna modifica ai sorgenti degli autori.
Il [delta](delta-documentale.diff) confronta realmente i file locali iniziali,
non il diffGit vuoto. [Quindici voci verificate](copertura-strutturale.json).

| Originale | Trattamento attuale | Testo valido conservato o recupero |
| --- | --- | --- |
| 4 Testing | Conservato e riallineato | Impostazione per preparazione/azione/oracolo/limite; aggiornato solo riscontroS0 al17novembre. |
| 4.1 Strategia | Conservato | Rischio economico, tre famiglie di verifica, funzionale/strutturale, conteggi non coverage. |
| 4.1.1 Tecnologie | Ripristinato | Spostati POM/profilo/Surefire in sottosezione autonoma; aggiunti dipendenze reali, setup e assenze H2/Mockito/JaCoCo/Failsafe. |
| 4.2 Business Logic | Riorganizzato | Sei temi con titoli distinti. I rinvii PostgreSQL non riclassificano i test come unitari. |
| 4.2.1 Invarianti | Recuperata sottosezione | Riutilizzati targa/stato, date incluse e microsecondo. Aggiunti preparazione/oracoli specifici per conto/sconti/anni e limite dell'assert mancante dopo prelievo rifiutato. |
| 4.2.2 AuthServiceTest | Adattato a identità | Nessun equivalente unitario. Esempio duplicato sul cambioemail; helper rilegge identità/login; assenza registrazione pubblica atomica e differenza hash prima callback esplicite. |
| 4.2.3 PricingAndSecurityTest | Adattato a prezzi/sicurezza | Conservato paragrafo prezzi valido; aggiunti assert discriminanti, deprezzamenti e57,05 corrente, vecchio PBKDF2isolato assente e prove reali/limiti. |
| 4.2.4 SalesServiceTest | Adattato a richieste/vendite | Conservati RequestAgreement e SaleType/limitezero; aggiunti accordo persistito, cessione, rifiuto integrale distinto da scadenza/penale, esempio0,03. |
| 4.2.5 Rental/BankAccountServiceTest | Adattato a noleggio/conto | Locale separato dal ramo rental con nuova lettura Rent/date/saldo. Gare, addebiti/rimborsi/destinazione rimangono PostgreSQL. |
| 4.2.6 TransactionAndObserverTest | Adattato e assenza dichiarata | MetodoDatabaseManager S0 per causa/soppressa/ordine; trigger dopo scritture reali, rollback/Observer e limite commit/rollback dedicati mancanti. |
| 4.3 Persistenza | Conservato e riorganizzato | Restano rollback, gare, isolamento; specificata portata selettiva. |
| 4.3.1 PostgresDaoIntegrationTest | Autonomo DAO/mapping | Conservati riletture/tipi/null/enum; recuperato esempio nuovo contesto dopo commit e distinti DAO/Connection, oggetto restituito e nuovaquery. Nessuna UnitOfWork inventata. |
| 4.3.2 DatabaseBootstrapIntegrationTest | Autonomo schema/demo | Conservati TestDatabase/Demo/isolamento e sentinella limitata, rollback reale, sequenze diagnostiche, gatePID/timeout/cleanup. Nuovo estratto32 SALE/8 PENALTY e snapshot13 tabelle/sequenze; bootstrapJava assente. |
| 4.4 Funzionale/presentazione | Conservato ed esteso | Riutilizzati tutti i paragrafi UI e loro limiti; recuperati registrazione/accesso, catalogo/sconti e BLOCKED/SOLD nella prova reale. FullWorkflow non ogniworkflow; incompatibilitàE3 e AppStartup soltanto ingresso espliciti. |
| 4.5 Esecuzione | Conservato e precisato | Istruzioni correnti separate da comando storicoE1; risultatiE1/E2/E3 non sommati, S0 nonruntime. Vecchio57 attribuito alla dichiarazione del capitolo originale, senza ricostruire runtime. |

[Copertura dei nove frammenti originali](copertura-frammenti.json): ogni riga
ha intervallo originale, funzione, nuovi estratti e adattamento motivato.
Il [censimento di tutti i15 blocchi](frammenti.json) include13 estratti letterali
(Java e comandoE1) e2 raccolte di comandi documentati. Le tre prove iniziali
RequestAgreement17–24, IdentityDeletion318–323 e PurchaseConcurrency301–308
rimangono identiche. Ogni nuovo estratto ha omissioni esterne esplicite:
nessuna ellissi interna o modifica agli assert per agevolare il confronto.
L'estratto rental è l'interno del ramo, con preparazione/confirm/nuovo contesto
spiegati nel corpo; i due estratti BLOCKED/SOLD sono lo stesso metodo, con
passaggi intermedi descritti. Il comando storico non è una nuova esecuzione.

C conserva le associazioni UC/alternative, S0/E1/E2/E3 e lacune06, aggiornando
solo data del riscontro, portata dell'assert SaleType e riferimento alle nuove
verifiche 12. Nessuna riscrittura di schede2.2/A o di capitoli1–3/B/5.
Nessuna tavola creata, corretta o esportata: riuso nativi/export e identificativi
esistenti, senza interventi draw.io in questo ticket. I confronti e il PDF non
costituiscono approvazione degli autori o integrazione del ticket 13.
