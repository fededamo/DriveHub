# Review Spec — ticket 12

Verdetto: **0 rilievi** sul diff di sessione `review.patch` (SHA256 `cc5a9ebc798368e98d841ea8c4bb7f5a7efc139dec5cca0483ddacfa80fd0368`).

- Requisiti mancanti o parziali: nessuno rilevato per il ticket 12. Gli stati ammessi, il blocco dei noleggi che comprendono oggi e il rimborso integrale atomico corrispondono a US37–40 (`TRACKER_DRIVEHUB/revisioni/spec.md:61–64`), AC07 (`:156`), D08–D09 (`PIANO_REVISIONI_DRIVEHUB.md:134–135`) e criteri del ticket (`TRACKER_DRIVEHUB/revisioni/issues/12-destinazione-veicolo-e-rimborsi.md:18–23`). Riscontro: `RentalService.java:136–177`, `PostgresRentDao.java:150–175` e `VehicleDestinationIntegrationTest.java:47–366`.
- Scope creep: nessuno rilevato. Nessuna nuova penale; restano il lock del veicolo e i conti ordinati per cliente. Le verifiche di concorrenza comprendono prenotazione prima/dopo il cambio, rollback dopo scritture effettive e due cambi concorrenti, con connessioni distinte e attese osservate in PostgreSQL.
- Implementazioni apparentemente presenti ma errate: nessuna rilevata. I test di errore verificano cancellazioni e accrediti effettivi prima del rollback; la selezione dei futuri esclude oggi, protetto dal controllo inclusivo del noleggio in corso.

Le evidenze persistenti distinguono baseline 297/297, mirati 278/278 e suite finale 330/330; documentano inoltre GUI/tastiera e cinque schermate (`verification.md:17–19,63–83,108–130`). La chiusura del ticket 12 e di AC07 è coerente con la review. R06 può risultare completata dopo la chiusura di 12, essendo 08 e 11 già chiusi; AC13 resta trasversale.

Non ho eseguito test DB né ripetuto la prova GUI; valutazione del codice, dei test e delle evidenze persistenti. Nessuna modifica al codice o ai test e nessun commit.
