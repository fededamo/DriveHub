# Ricontrollo indipendente — Standards

17 novembre 2026. Candidato PDF SHA-256
`3e08ead9770f414bdf7ed988725bb28d0eae813a79a50491225cdb2572490195`;
estratto SHA-256
`acb349fa9682f8b326cfff2a0060867585097938d599c7d4a6a322f920f0432d`.
Punto fisso invariato: working tree iniziale del ticket 11, inclusi i
non tracciati; confronto effettivo con baseline, archivio e delta.
Il rapporto iniziale resta conservato senza modifiche.

## Rilievo iniziale e correzione

**Chiuso** il rilievo medio sulla descrizione di AbstractUser.
`capitoli/3_implementazione.tex:40` dichiara ora classe astratta e
costruttore protetto, coerentemente con
`src/main/java/it/unifi/drivehub/domain/user/AbstractUser.java:11,21`.
La frase è presente anche nella pagina fisica 83 del PDF.
È così rispettata la regola di `docs/agents/domain.md:17` sul riscontro
delle affermazioni nel codice corrente.

Verificata anche la precisazione in `3_implementazione.tex:1003–1006`:
SQLSTATE 23503 sulla cancellazione diventa IllegalStateException con causa;
gli altri errori mantengono la causa SQL, come in
`PostgresUserDao.java:236–240`. La descrizione non attribuisce una tassonomia
uniforme assente. Pagina fisica 99 riaperta e leggibile.

## Verifiche del candidato corretto

Controlli indipendenti in sola lettura confermano hash di candidato,
sorgente ed estratto, delta esatto, 3.277 file iniziali presenti e sole
modifiche a capitolo 3/PDF. Riconfrontati i 27 estratti con sorgenti e PDF;
estratto di 23 pagine coerente con le pagine 83–105 del documento di 143.

Confrontate tutte le 143 pagine con la copia del candidato precedente
identificata dall'impronta del rapporto iniziale: cambiano testo e pixel
soltanto delle pagine 83 e 99; le altre 141 sono identiche.
Nuovamente confermate 78 pagine dei capitoli 1/2 identiche alla baseline
anche nei pixel e 38 pagine successive invariate nel testo salvo numero.
I rendering riaperti corrispondono al nuovo PDF.

Letti i tre log `compilazione-review-*`: documento di 143 pagine, cinque
messaggi TeX preesistenti in C, nessun nuovo box o rinvio irrisolto.
Nessuna nuova violazione documentata o euristica Fowler rilevante.

**Esito Standards: conforme, zero rilievi aperti.** Nessuna ricompilazione,
esecuzione del checker scrivente, prova applicativa, accesso DB/UI o modifica
agli altri materiali effettuata dal reviewer. Restano i limiti applicativi
dichiarati; questo esito non presume l'approvazione degli autori.
