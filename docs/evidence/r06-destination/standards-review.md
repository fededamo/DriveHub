# Review Standards — ticket 12

**Verdetto: 0 violazioni documentate; 0 smell azionabili.**

Revisione indipendente limitata al diff della sessione contenuto in `review.patch`, SHA256 `cc5a9ebc798368e98d841ea8c4bb7f5a7efc139dec5cca0483ddacfa80fd0368`, e ai file connessi necessari. Non è stato usato un diff da HEAD, che includerebbe i ticket precedenti.

Fonti: istruzioni utente, `DriveHub/AGENTS.md`, `docs/agents/domain.md`, `GLOSSARY.md`, ADR 0001, `docs/testing.md`, `pom.xml` e registro operativo pertinente. Nessun ulteriore standard di codifica è stato presunto.

Le modifiche mantengono le responsabilità esistenti: il servizio decide le transizioni e coordina rimborsi e stato nella stessa transazione; il DAO conserva SQL e verifica l'esito delle cancellazioni. Riutilizzati disponibilità inclusiva, lock veicolo, protocollo dei conti e gestione delle eccezioni già presenti. Interfacce pubbliche, schema, Observer e regole di prezzo restano coerenti con il perimetro.

I nuovi test osservano servizi pubblici e PostgreSQL reale; fixture e guasti SQL sono confinati alle prove. Gli helper delle gare prevedono timeout e cleanup. Il controllo JavaFX riutilizza gli harness precedenti e verifica anche errori e tastiera.

Valutati gli smell richiesti: Mysterious Name, Duplicated Code, Feature Envy, Data Clumps, Primitive Obsession, Repeated Switches, Shotgun Surgery, Divergent Change, Speculative Generality, Message Chains, Middle Man e Refused Bequest. Nessun caso richiede una modifica concreta nel diff: le ripetizioni locali seguono convenzioni esistenti e non giustificano un'astrazione aggiuntiva.

Limiti: review statica; nessun test DB o GUI rieseguito e nessuna certificazione indipendente dei conteggi riportati nelle evidenze. Nessuna modifica al codice, test o documenti preesistenti; nessun commit. Relazione_SWE esclusa integralmente.
