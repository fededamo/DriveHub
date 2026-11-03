# Review indipendente Spec — sessione18

**Esito: nessun rilievo azionabile dimostrato sul delta esaminato.**

Confronto con la fotografia locale iniziale18, non con HEAD. Patch congelata
`review.patch`, SHA256
`d8c1281839f3221e1e4b1c3d341c15120caf7555974b2fb806eaea301d34450b`.
Fonti: ticket18, specifica AC01–AC20 e Testing Decisions, registro esterno e
confini della verifica integrata. `Relazione_SWE` sempre esclusa.

Il requisito del ticket18 «collegati AC01–AC20 a ticket, comportamenti e nomi
effettivi delle prove» è soddisfatto dalla matrice: verificati contro sorgenti
JUnit e XML finali tutti i31 riferimenti `Classe.metodo` e i79 nomi di metodo
non qualificati presenti. Percorsi principali, alternativi, errori e prove GUI
sono distinti; i tre casi FullWorkflow sono correttamente limitati a login e
letture. Nessuna discrepanza di nomi rilevata.

Per «suite finale eseguita sullo stato integrato», gli XML finali totalizzano
394casi, zero failure/errori/salti; ripartizione47senzaDB/347PostgreSQL coerente.
I mirati riusciti riportano363casi; il tentativo precedente con4errori resta
separato. Riscontrati log riusciti per i sette harness JavaFX e adattamenti
documentati degli harness, senza eliminazione degli assert nelle patch lette.
Ispezione mirata delle prove di acquisto e cancellazione test drive: importi
osservabili, errore dopo scritture reali e riletture con nuovo contesto
concordano con i confini approvati. Non sono certificazioni mediante mock.

Per isolamento e tracciabilità, i fingerprint finali della sentinella sono
identici su13tabelle/7sequenze. Manifest e resoconto distinguono le due fasi,
configurazione privata e risultati storici16–17. L'assenza del controllo finale
sentinella fase1 è dichiarata e non conteggiata come successo. Documentazione,
render e limiti di osservazione sono riferiti separatamente alle prove attuali.

**Requisiti mancanti/parziali, implementati male o scope creep:** nessuno
dimostrato nel diff. Nessun intervento produttivo o refactor aggiuntivo; la
chiusura amministrativa successiva ai rapporti resta intenzionalmente pendente.

Non ho rieseguito Maven, PostgreSQL o GUI: questa review verifica prove e
sorgenti pertinenti. Resta il limite documentato della sentinella fase1; nessuna
garanzia aggiuntiva su altri ambienti o interleaving.
