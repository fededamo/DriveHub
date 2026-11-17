# Verifica della precisazione sui diagrammi

Data: 16 novembre 2026. Esito: vincoli operativi aggiornati e controlli documentali superati. Il rifacimento delle tavole è ancora da eseguire.

## Richiesta recepita

Rifare la vecchia relazione mantenendo tutti i contenuti, riadattati esattamente all’implementazione corrente. Per ogni intervento sui diagrammi usare sempre la skill `drawio:drawio`, partire dai disegni degli autori e rifare/riadattare tutte le tavole originali.

## Modifiche e controlli

- Aggiunti alla [matrice](../../allineamento.md#diagrammi-vincoli-operativi) vincoli univoci e tabella delle undici tavole originali con base e collocazione.
- Verificati i quattro file sorgente effettivi: SWE ER, Users SWE, Page Navigation Diagram e Package diagram. Il diagramma di navigazione e quello dei package sono distinti.
- Aggiornati specifica, indice, piano, ticket 10/13 e README; le istruzioni locali richiamano la regola per tutti gli interventi, anche su tavole preesistenti.
- Eliminata la possibilità di considerare sufficiente il semplice riuso delle tavole della prima riscrittura; i quattro class diagram e le tre sequenze originali conservano un corrispondente nel corpo.
- Conservate tutte le 102 voci della matrice nello stesso ordine. Verificate esistenza dei riferimenti ai diagrammi originali e delle basi degli autori, collegamenti locali, ancoraggi e formattazione dei documenti aggiornati.

## Conservazione e limiti

Baseline SHA-256 di 1215 file in `/tmp/drivehub-vincoli-drawio-np5hx8vv`; confronto successivo: nove documenti operativi modificati, questo solo verbale aggiunto e 1206 file preesistenti invariati. Gli originali degli autori, la vecchia relazione, tutti i diagrammi, LaTeX, PDF, ZIP, software e verifiche precedenti conservano la stessa impronta.

Letture e modifiche riguardano i requisiti documentali. Nessun diagramma ridisegnato o esportato in questo passaggio, nessuna compilazione LaTeX o Java, esecuzione di test, accesso DB o commit. Le verifiche storiche non sono state riscritte per attestare questi nuovi vincoli.
