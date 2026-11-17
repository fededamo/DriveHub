# Ticket 05 — Implementazione

Status: done

Specifica operativa: richiesta dell'utente del 13 novembre 2026, conservata
qui per rendere il lavoro e la revisione indipendenti da tracker esterni e
materiali temporanei. Riferimento generale: [tracker/spec.md](../../tracker/spec.md).
Ticket 01–04 conclusi; capitoli 1–2 e appendici A–B completi e proposti
per revisione, senza approvazione già attribuita agli autori.

1. Leggere docs/AGENTS.md e le istruzioni richiamate; spec, indice, piano,
   GLOSSARY e README della relazione; architecture, ADR 0001–0002 e
   verifiche/revisioni 03–04.
2. Completare il capitolo 3 conservando titoli e ordine: 3.1 Domain Model;
   3.2 Business Layer; 3.3 Presentation Layer; 3.4 Persistenza: livello DAO
   e mapping oggetti/tabelle; 3.5 Idiomi e Design Pattern.
3. Riscontrare sui sorgenti correnti le affermazioni, con frammenti brevi,
   esatti e commentati che spieghino scelte, responsabilità e limiti;
   evitare cataloghi di metodi e duplicazioni del capitolo 2.
4. Coprire invarianti, gerarchie, mutabilità e riferimenti per ID; servizi,
   prezzi/accordi, operazioni economiche, identità e conto; transazioni,
   riuso effettivo della Connection, letture autonome, rollback, errori
   e Observer sincrono.
5. Coprire navigazione JavaFX, AsyncLoad/Task, aggiornamenti, errori e ciclo
   di vita; mapping JDBC, nullabilità e quattro enum PostgreSQL.
6. Distinguere motivatamente pattern GoF, factory statiche, DAO, DI,
   Singleton e navigazione nello stile Visitor; non dedurre garanzie o
   pattern dai nomi.
7. Riutilizzare diagrammi e riferimenti esistenti; eventuali nuove tavole
   esclusivamente draw.io.
8. Registrare in questa directory matrice affermazioni/frammenti → sorgenti,
   comandi, ambiente, risultati e limiti. Verificare gli strumenti, compilare
   realmente il PDF e osservare codice, impaginazione, indice e riferimenti.
9. Concludere con code-review due revisioni indipendenti Standards e Spec,
   correggendo i rilievi prima della chiusura.
10. Preservare software, SQL, dati, vecchia relazione, parti completate ed
    evidenze/manifest 01–04. Nessun commit, test applicativo o accesso DB;
    eventuali difetti segnalati separatamente.
11. Capitoli 4–5 e appendice C restano provvisori. Aggiornare lo stato e
    consegnare PDF, LaTeX e verifiche; proporre capitolo 3 agli autori.

La prescrizione generica della skill implement su commit, typecheck e suite
è superata dai vincoli espliciti di questa richiesta documentale.

## Comments

13 novembre 2026: contenuto, compilazione reale, osservazione visiva,
conservazione e revisioni Standards/Spec concluse. Un rilievo minore di
matrice corretto e verificato, nessuno aperto. Capitolo 3 proposto agli
autori, senza attribuire approvazione; capitoli 4–5/C ancora provvisori.
