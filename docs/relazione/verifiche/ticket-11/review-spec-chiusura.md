# Ricontrollo Spec — ticket 11

17 novembre 2026. **PASS: zero rilievi residui nel candidato esaminato.**
Rapporto separato dalla [prima review](review-spec.md), senza lettura del
rapporto Standards. Resta il punto fisso working tree iniziale, non il
solo diff Git vuoto.

Impronte SHA-256 ricalcolate e coincidenti con `candidato-review.json`:

- PDF completo: `3e08ead9770f414bdf7ed988725bb28d0eae813a79a50491225cdb2572490195`.
- Capitolo sorgente: `6b6a947f0aa4c2581c44aa44baacedea8b52f8321c030f54c28fedeecd072302`.
- Estratto: `acb349fa9682f8b326cfff2a0060867585097938d599c7d4a6a322f920f0432d`.

Entrambi i rilievi sono risolti nel sorgente e nel PDF. A pagina fisica
83, `AbstractUser` è descritta come astratta con costruttore protetto,
in accordo con `AbstractUser.java:11` e `:21`. A pagina 99 è recuperata
la spiegazione della traduzione SQLSTATE 23503, della conservazione della
causa SQL e del limite della tassonomia DAO, confermata da
`PostgresUserDao.java:236`. Aperte entrambe le pagine rigenerate: testo
completo e leggibile, senza tagli o sovrapposizioni.

Resta soddisfatta la richiesta del ticket di conservare contenuti e
funzione espositiva: 35 voci, cinque gruppi dominio, otto servizi,
quattro flussi completi, formule ed esempi, tre argomenti UI, cinque DAO
e sei temi dei pattern. Le 14 corrispondenze originali hanno esempi
correnti o adattamenti motivati; nessuna Strategy, FXML o UnitOfWork
inventata. Il riscontro semantico della prima review resta valido.

Ricontrollati in memoria tutti i 27 estratti rispetto a intervalli dei
sorgenti e testo PDF; le 23 pagine dell'estratto coincidono con 83–105
del completo. Ricalcolati i sette risultati monetari, inclusi 25.333,87
e 57,05 euro. Verificati 79 rinvii risolti e assenza di `??`.
Letti i tre log `compilazione-review-*`: 143 pagine, soli cinque messaggi
TeX storici in C; nessun Overfull/Underfull o rinvio indefinito finale.

Ricontrollate 3.277 impronte: cambiano soltanto capitolo 3 e PDF.
Confrontate direttamente col PDF archiviato le 78 pagine dei capitoli
1/2, identiche per testo e pixel, e le 38 successive di capitoli 4/5 e
appendici, identiche nel testo tolto il numero finale. Nessuna anticipazione
dei ticket 12/13 o approvazione degli autori.

Non eseguiti compilazione, checker scrivente, test applicativi, UI o DB.
Stati e riepiloghi amministrativi finali restano da aggiornare; il PASS
riguarda il candidato identificato sopra e non chiude il ticket.
