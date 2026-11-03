# Spec — revisione indipendente

Revisore `review_spec08`, contesto nuovo, 2 novembre 2026.

Revisione SPEC del solo ticket 08: **zero rilievi** per requisiti
mancanti/parziali, scope creep o implementazione errata.

Il requisito «nella stessa transazione dell'operazione economica» è rispettato:
PurchaseService.java:182 e RentalService.java:104 pubblicano prima del commit
passando la connessione corrente. TestDriveCancellationObserver.java:23 usa quella
connessione; VehicleEventPublisher.java:28 propaga gli errori. Observer conserva
la responsabilità della politica commerciale, con evento coerente anche senza
transizione di stato nel noleggio. Composizione e chiamanti risultano aggiornati.

La cancellazione comprende tutti i futuri del veicolo e preserva lo storico.
Le prove pubbliche PostgreSQL verificano rollback integrale, successo, altri
veicoli, futuri fuori periodo, assenza di futuri e confine −1/0/+1 microsecondo
(TestDriveCancellationIntegrationTest.java:40, :85, :121, :141).
Nessun intervento anticipa ticket 11, altre transizioni o prestazioni.

Verificati log RED con due failure attese, GREEN 2/2, casi 8/8, mirati 117/117 e
XML finali/archiviati: **171 test, zero failure/errori/skip**. La patch preserva
il comportamento economico e i controlli precedenti 04–07.

SHA256 patch revisionata:
`52afb2fc12e7df429001b9c6a7cb78250ff672861b7324d47805513055a95f6c`

Revisione in sola lettura, senza rieseguire suite o accedere al DB;
Relazione_SWE esclusa. Nessuna vista modificata: JavaFX/tastiera non ricontrollati.
