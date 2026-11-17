# Ticket documentale 06 — Testing

Specifica operativa ricevuta dall'utente il 13 novembre 2026, indipendente
sia dai ticket già pubblicati sia dai contenuti temporanei. Repository:
`/home/federico/dev/Drivehub/DriveHub`. Baseline Git:
`61ce18ae1d4e0458b63e970b793b0b3a634a3adf`.

1. Leggere docs/AGENTS e le istruzioni richiamate; spec, indice, piano,
   glossario e README della relazione; architettura, ADR 0001–0002,
   testing e installation; verifiche, revisioni e segnalazioni documentali
   04–05; POM, test, harness ed evidenze effettivamente usate.
2. Completare capitolo 4 mantenendo ordine e titoli: 4.1 Strategia di verifica;
   4.2 Livello 1: Business Logic; 4.3 Livello 2: Persistence Layer (DAO);
   4.4 Verifiche funzionali e della presentazione; 4.5 Esecuzione della suite
   e portata dei risultati. Completare appendice C «Matrice delle verifiche»
   nella posizione concordata. Corpo sintetico e argomentato, tracciabilità
   estesa in C, senza cataloghi di metodi nel corpo o duplicazioni di 2–3.
3. Riscontrare sui sorgenti correnti test unitari, integrazione PostgreSQL
   e harness JavaFX, classificandoli dall'implementazione, non dal nome/tag.
   Spiegare suite, selezione Maven, prerequisiti, fixture, isolamento e
   sentinella; distinguere comandi senza DB e profilo PostgreSQL.
4. Scegliere esempi di invarianti, prezzi e accordi, operazioni economiche,
   identità/conto, mapping JDBC, nullabilità e quattro enum. Usare pochi
   frammenti brevi, esatti e commentati quando chiariscono cosa si dimostra.
5. Illustrare rollback dopo scritture effettive e conflitti concorrenti:
   sincronizzazione, connessioni distinte, timeout e limiti delle asserzioni.
6. Descrivere prove JavaFX su caricamenti, risultati superati, errori/riprova,
   aggiornamenti e ciclo di vita, senza reattività/accessibilità universali.
   Distinguere verifiche funzionali e strutturali; conteggi e copertura del
   codice non sono equivalenti.
7. In C collegare requisiti, casi d'uso e alternative concordati a file/metodi,
   asserzioni, evidenze, versione/data, risultati e limiti. Esplicitare
   coperture parziali, lacune e associazioni non dimostrate: un test esistente
   non certifica da solo un requisito.
8. Per lo storico verificare provenienza, commit, ambiente, comando, insieme
   di prove, riusciti/falliti/saltati e differenze rispetto ai sorgenti
   attuali. Non sommare sessioni, rinominarle come nuove esecuzioni o inventare
   percentuali, durate o risultati mancanti. Separare i difetti già noti.
9. Solo documentazione: nessun commit, modifica al software/test, nuova
   esecuzione di test applicativi, accesso DB o avvio harness. I comandi
   applicativi sono documentati, non eseguiti. Questi vincoli sostituiscono
   quelli generici della skill implement su TDD/esecuzioni/commit.
10. Riusare diagrammi e riferimenti. Nuove tavole solo draw.io se necessarie.
    Registrare qui specifica, matrice affermazioni/frammenti→fonti/evidenze,
    baseline di conservazione, comandi, ambiente, risultati e limiti.
11. Verificare strumenti, compilare realmente PDF e osservare tutte le nuove
    pagine, codice, tabelle multipagina, impaginazione, indice e riferimenti.
    Concludere con code-review Standards e Spec indipendenti, in contesti
    separati; correggere e ricontrollare i rilievi. Isolare il delta iniziale
    anche per documentazione non tracciata: diff Git vuoto non prova conservazione.
12. Preservare software, SQL, dati, vecchia relazione, contenuti completati ed
    evidenze/manifest 01–05. Segnalare nuovi difetti separatamente. Capitolo 5
    provvisorio. Aggiornare lo stato e consegnare PDF completo, estratto 4/C,
    sorgenti LaTeX e verifiche. Proporre agli autori senza attribuire approvazione.

Stato ricevuto: documentali 01–05 conclusi; 1–3/A–B completi e proposti agli
   autori, non approvati. Numeri dei vecchi ticket applicativi distinti.
Riferimenti editoriali: [specifica generale](../../tracker/spec.md),
[indice](../../indice.md), [piano](../../piano.md).
