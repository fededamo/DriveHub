# Stato dell'aggiornamento locale

Il 9 novembre 2026 l'installazione locale è stata consolidata nel solo
contenitore **`drivehub-db`**, con database PostgreSQL `drivehub` sulla porta
locale **55432**. `run.bat` e `run.ps1` usano questo contenitore e conservano
il volume persistente `drivehub-quickstart-data`.

La copia più recente per creazione e struttura era `drivehub-quickstart-db`.
L'altro archivio conteneva una scadenza già elaborata: la riconciliazione ha
conservato quella penale, il saldo già rimborsato e la richiesta già rimossa.
La conversione ha poi portato richieste e registro al contratto finale, senza
invocare servizi di pagamento o applicare nuovamente il rimborso.

Risultato: **13 richieste, 41 operazioni**; identificativi, date, associazioni,
accordi e contatori verificati. I saldi finali coincidono con quelli della copia
in cui la scadenza era già stata gestita.

I backup originali di entrambi gli archivi e il backup finale sono conservati
privatamente fuori dal repository applicativo, sotto
`/home/federico/dev/Drivehub/.drivehub-backups/2026-11-09/`.
Sono stati provati con un ripristino reale su database temporanei separati.
Il volume del vecchio contenitore non è stato cancellato ed è inattivo.

Su richiesta, `convert-operation-semantics.sql` è stato rimosso completamente
anche dai test, insieme alle prove e fixture che ne dipendevano. Il codice e le
prove storiche restano nel commit `d31a1ce`; non sono componenti dell'app corrente.
I test del dominio, dei servizi pubblici, dei vincoli e dei flussi finali restano.

`schema.sql` inizializza o resetta un database sacrificabile; `demo.sql`
sostituisce i dati con esempi. Non usarli per aggiornare un database popolato.
Questa operazione locale non aggiorna automaticamente altre installazioni.
L'accesso ordinario può gestire le richieste scadute ancora pendenti: tali
movimenti applicativi sono distinti dalla conversione qui verificata.

[Controlli successivi, comandi ed esiti](evidence/db-consolidation-2026-11-09/verification.md).
