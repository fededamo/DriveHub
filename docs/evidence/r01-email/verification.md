# Ticket 03 — cambio email sicuro

Verifica locale del 31 ottobre 2026, ticket 03 / R01 / rilievo #8, AC08 e parte
pertinente di AC13. Prerequisito 01 e precedente 02 controllati: entrambi
`State: closed`, tutti i criteri soddisfatti e prove Java 21/PostgreSQL presenti.
Nessun altro ticket lavorato; nessun commit.

## Versione e ambiente

- HEAD invariato: `c2a81122ee6be44760ded4160fde229bd33e837c`.
- Modifiche del ticket: `AuthService.java`, nuovo `AuthServiceIntegrationTest.java`
  e queste evidenze; piano e tracker esterni al progetto. I 31 file locali
  preesistenti, inclusi quelli dei ticket 01 e 02, confrontati con SHA-256 iniziali:
  tutti invariati. `Relazione_SWE` esclusa da ogni operazione.
- OpenJDK Linux 21.0.12.1, Maven 3.9.16, PostgreSQL 16.15; requisito POM 21
  invariato. Ripreso l'[ambiente R00](../../testing.md), cluster temporaneo
  `/tmp/drivehub-r00-pgdata`, solo `127.0.0.1:55432`, DB/ruolo `drivehub_test`.
  Sentinella sulla copia applicativa distinta `drivehub` dello stesso cluster;
  nessun collegamento al database applicativo preesistente.
- Avvio iniziale in sandbox fallito: `could not create IPv4 socket ... Operation
  not permitted`. Avvio e prove PostgreSQL eseguiti con escalation autorizzata.
  Credenziali soltanto nel file temporaneo privato R00, mai nelle evidenze.
- [Manifest SHA-256](sources.sha256) dei sorgenti, POM, schema e demo, relativo
  alla radice del progetto; verifica con `sha256sum -c docs/evidence/r01-email/sources.sha256`.

## Difetto e correzione

`changeEmail` autenticava l'utente e scriveva direttamente l'email, limitandosi a
trim/lowercase. Il CHECK SQL ammette spazi interni; il dominio li rifiuta in fase di
rilettura. Il dialog applicava `user.updateEmail` soltanto dopo il commit.

Il servizio ora usa l'utente già caricato da `verifyPasswordBeforeAction`, chiama
`User.updateEmail` e persiste `user.getEmail()` soltanto dopo la validazione.
La funzione privata restituisce lo stesso utente senza nuove query. Nessun nuovo
validatore, dipendenza, modifica API pubblica, schema, fixture o politica identità.
Normalizzazione e messaggio del dominio riutilizzati. UNIQUE nello stesso ruolo,
trigger fra ruoli e rollback restano responsabili del rifiuto dei duplicati.

## Riproduzione e verifiche

Confini già approvati: `AuthService.changeEmail` e `login` pubblici, con DAO reali
su PostgreSQL dedicato. Ogni caso ricrea la fixture attraverso `TestDatabase`;
nessun mock, Singleton applicativo o test che modifica dati su DB implicito.

Launcher locale: `python3 /tmp/drivehub-r00-maven.py <etichetta> <argomenti Maven>`;
carica configurazione privata senza stamparla e usa Java 21. I comandi Maven
equivalenti, dalla radice del progetto, sono:

| Etichetta | Argomenti Maven | Esito | Log |
|---|---|---|---|
| `ticket03-red` | `-Ppostgres-tests -Dtest=AuthServiceIntegrationTest#rejectedEmailPreservesStoredIdentityAndLogin test` | 3 failure attese, 0 errori/saltati: nessuna eccezione per spazi interni, sui tre ruoli | [RED](red.txt) |
| `ticket03-green` | stessi argomenti del RED, dopo la sola correzione | 3 passati, 0 failure/errori/saltati | [GREEN](green.txt) |
| `ticket03-targeted` | `-Ppostgres-tests -Dtest=AuthServiceIntegrationTest test` | 18 passati, 0 failure/errori/saltati | [Mirati](targeted.txt) |
| `ticket03-no-db` | `test` | 28 passati, 0 failure/errori/saltati; tag postgres escluso | [Senza DB](no-db.txt) |
| `ticket03-full` | `-Ppostgres-tests clean test` | 53 passati, 0 failure/errori/saltati; inclusi 25 PostgreSQL e sentinella | [Suite completa](full-suite.txt) |

La suite finale ricompila 62 sorgenti principali e 9 sorgenti di test con target 21.
Totali verificati anche negli XML Surefire: 53/0/0/0. Comprese configurazione
protetta, fixture ripetibili, scadenza, invarianti veicolo e prezzi dei ticket precedenti.

Casi della classe di integrazione:

- `rejectedEmailPreservesStoredIdentityAndLogin`: spazi interni rifiutati dal
  dominio; successivo login rilegge email, ID, nome, cognome, hash e ruolo invariati,
  per Cliente, Venditore e Manager (3 casi). Stesso test prima e dopo la correzione.
- `validEmailIsNormalizedPersistedAndUsedForLogin`: spazi esterni/tab e maiuscole
  normalizzati, login col nuovo indirizzo rilegge identità/credenziali corrette;
  precedente indirizzo non più utilizzabile (3 casi).
- `duplicateEmailPreservesBothIdentitiesAndLogins`: indirizzo esistente con spazi
  esterni e maiuscole rifiutato; entrambe le identità rilette e accessi invariati.
  Due UNIQUE nello stesso ruolo e tre trigger fra ruoli (5 casi).
- `wrongPasswordPreservesStoredIdentityAndLogin`: password errata rifiutata prima
  della modifica sui tre ruoli, dati precedenti/accesso invariati (3 casi).
- `malformedEmailPreservesStoredIdentityAndLogin`: null, vuoto, soli spazi/tab,
  indirizzo senza chiocciola rifiutati senza modifiche (4 casi).

## Chiamanti controllati

- `AccountManagementDialog`: unico chiamante applicativo di `changeEmail`;
  inoltra ID, ruolo visualizzato e password, mostra errore e non aggiorna la
  sessione se il servizio fallisce. Dopo successo il dominio normalizza anche
  l'utente di sessione. Non serve modificare il dialog.
- Dashboard Cliente, Venditore, Manager e `App`: passaggio del servizio e della
  stessa identità; ruoli `Cliente`, `Venditore`, `Manager` accettati da `resolveTable`.
- `LoginView`, `AuthService.login/registerClient`, interfaccia `User`,
  `AbstractUser`, factory Client/Salesman/Manager: regola email condivisa.
- `UserDao`/`PostgresUserDao`: ricerca per ID/email, mapping dominio, unico
  chiamante applicativo dell'update DAO nel servizio, parametri JDBC e routing
  dei tre ruoli preservati. Controllati UNIQUE, CHECK, trigger e vista `user`.
- Tutti i chiamanti di `verifyPasswordBeforeAction`: `changeEmail`,
  `changePassword`, `deleteAccount`. Gli ultimi due continuano a ignorare il
  risultato; verifica password ed errori invariati. Nessun cambio dei loro flussi.

## Chiusura e limiti

Revisione finale `code-review` applicata al diff locale del solo ticket, fissato in
`/tmp/drivehub-ticket03-review.diff`: `git diff HEAD -- src/main/java/it/unifi/drivehub/service/AuthService.java`
più `git diff --no-index /dev/null src/test/java/it/unifi/drivehub/AuthServiceIntegrationTest.java`.
Adattamento al divieto esplicito di commit; nessun commit da confrontare.
Due agenti con contesti nuovi, valutazioni indipendenti:

- **Standards: 0 rilievi.** Consultati `AGENTS.md`, glossario, `docs/testing.md`,
  regole di dominio e convenzioni dei test. Nessuna violazione documentata o smell
  concreto; riusati validazione, normalizzazione, gestione password e duplicati.
- **Spec: 0 rilievi confermati.** Validazione prima della transazione, rifiuto con
  dato/accesso invariati, successo riletto, duplicati, password errata e chiamanti
  coerenti; nessuno scope creep. Log RED/GREEN/mirati controllati dal revisore.
  La suite completa è stata verificata dal responsabile dopo le revisioni.

`git diff --check` e controllo indice superati nel perimetro senza relazione;
manifest sorgenti e conservazione dei file preesistenti verificati, nessuna
password privata nei log. Cluster temporaneo arrestato con
`pg_ctl -D /tmp/drivehub-r00-pgdata -m fast -w stop` (esito positivo).
Tutti i 4 criteri del ticket soddisfatti: **ticket 03 chiuso, R01 completata,
rilievo #8 chiuso; #13 resta verificato soltanto per i ticket lavorati**.

UI grafica non eseguita, nessuna UI modificata; controllati staticamente chiamanti
e gestione degli errori. Warning Maven JavaFX preesistente. Non sono certificati
difetti degli altri ticket né nuove politiche/concorrenza dell'identità. Ambiente
temporaneo da ricreare se `/tmp` viene rimossa. Nessun commit.
Prossimo passo soltanto in una richiesta successiva: ticket 04 / R02. Nessun lavoro
sul ticket successivo in questa sessione.
