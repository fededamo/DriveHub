# Revisione indipendente Spec — ticket 07

Perimetro: `session.patch` e `files.txt` di questa directory, rispetto alla baseline dedicata `/tmp/drivehub-ticket07-baseline/repo`; nessun confronto con `HEAD` e nessun commit della sessione. Consultati ticket 07, AC06/AC13 e US23–26, glossario, ADR 0001, documentazione dei test, codice e prove pertinenti.

**(a) Requisiti mancanti o parziali:** nessuno individuato nel perimetro del ticket. Saldo, rifiuto e scadenza rileggono la richiesta mediante il medesimo `findByIdForUpdate`, sulla connessione della transazione. Tipo, approvazione, scadenza e stato `BLOCKED` sono ricontrollati; le scritture senza effetto provocano rollback. Il controllo scadenze usa gli oggetti iniziali solo per ottenere gli ID, poi decide su dati riletti sotto lock. Soddisfatti nell'implementazione gli AC «protezione condivisa della richiesta riletta nella transazione» e «non usa come autorità un oggetto letto prima della transazione».

Le prove PostgreSQL aggiunte coprono connessioni distinte e sincronizzazione osservabile per saldo/saldo, saldo/rifiuto e saldo/scadenza; verificano consumazione unica, acconto 4.000 €, penale 800 €, rimborso 3.200 €, ripetizioni, approvazione incompleta, richiesta scaduta, stato incoerente, conto mancante, errori e scritture saltate. Le riletture tramite nuovi contesti controllano conto, richiesta, veicolo e registro, come richiesto dagli AC sul rollback. `TestDatabase` conserva il confine isolato approvato.

**(b) Scope creep:** nessuno individuato. Il lock nell'approvazione impedisce che una sessione obsoleta sovrascriva una richiesta consumata. I controlli sugli esiti dei DAO servono gli AC; nessuna nuova regola economica, dipendenza o comando di annullamento. L'Observer transazionale rimane al ticket 08.

**(c) Implementazioni apparentemente errate:** nessuna individuata. Il totale concordato e il protocollo del conto restano preservati; il messaggio di scadenza usa il rimborso realmente calcolato nella transazione e viene restituito dopo il commit.

Non ho rieseguito test: la suite completa era già in corso. Questo rapporto valuta codice e copertura, senza certificare un nuovo esito di esecuzione; il ticket 06 resta aperto in verifica finale.
