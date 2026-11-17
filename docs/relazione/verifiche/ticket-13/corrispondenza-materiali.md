# Corrispondenza dei materiali originali

La corrispondenza delle [102 voci](corrispondenza-102.md) include livelli, ordine, intervalli di contenuto e fonti. Questa vista rende leggibile il censimento dei materiali; i JSON conservano impronte, dettagli e limiti per elemento. Il confronto riguarda la funzione espositiva, non soltanto quantità o titoli.

## Sei template principali

| Originale | Destinazione completa | Trattamento |
| --- | --- | --- |
| Relazione_SWE/templates/test_drive.tex — Prenotare un test drive | 2.2.1: UC-01 --- Prenotare un test drive | **Ricollocare.** Scheda completa del test drive: attore, condizioni, fascia oraria effettiva, limite di tre prenotazioni, conflitti, cancellazione e prove pertinenti. |
| Relazione_SWE/templates/noleggio.tex — Noleggiare un veicolo | 2.2.2: UC-02 --- Noleggiare un veicolo | **Ricollocare.** Scheda completa del noleggio: estremi inclusi, costo ricalcolato, saldo, conflitti, annullamento dei test drive futuri e rollback. |
| Relazione_SWE/templates/acquisto.tex — Richiedere e completare un acquisto | 2.2.3: UC-03 --- Richiedere e completare una vendita al cliente | **Ricollocare.** UC-03: acconto del 20%, totale fissato, autorizzazione, saldo, rifiuto e scadenza con penale; sostituire le vecchie anomalie di prezzo con il comportamento corretto attuale. |
| Relazione_SWE/templates/cessione.tex — Proporre un veicolo al concessionario | 2.2.4: UC-05 --- Proporre un veicolo alla concessionaria | **Ricollocare.** UC-05 prima di UC-04 nell'esposizione, conservando gli identificativi: proposta, stima persistita, esito del venditore, storico e limite dell'accredito zero. |
| Relazione_SWE/templates/autorizzazione_acquisto.tex — Autorizzare una richiesta di acquisto | 2.2.5: UC-04 --- Autorizzare una richiesta di vendita al cliente | **Ricollocare.** UC-04: approvazione o rifiuto del venditore, scadenza e rimborso; distinguere autorizzazione e saldo senza rinumerare il caso d'uso. |
| Relazione_SWE/templates/sconto_veicolo.tex — Gestire lo sconto su un veicolo | 2.2.6: UC-06 --- Gestire lo sconto su un veicolo | **Ricollocare.** Scheda UC-06: sconti su modello e veicolo, intervallo 0–50, massimo non cumulato, inserimento/rimozione e accordi già fissati. |

Ogni scheda conserva descrizione, livello, attori, pre/postcondizioni, flusso, alternative ancorate ai passi e prove/portata. Ordine UC-01/02/03/05/04/06, osservato a pagine 13–20. [Intervalli e fonti](template-6.json). A contiene UC-07–11 e S01–04; B copre gli undici package, C esplicita oracoli selettivi e limiti.

## Diciotto funzioni delle schermate originali

| Funzione originale | Destinazione | Immagini correnti e limite |
| --- | --- | --- |
| 2.3.1.1 — Schermata iniziale (00-benvenuto.png) | 2.3.1.1, docs/relazione/capitoli/2_funzioni.tex:123 | 01-accesso.png. Welcome separata assente: ingresso e accesso sono LoginView. |
| 2.3.1.2 — Accesso (01-accesso.png) | 2.3.1.2, docs/relazione/capitoli/2_funzioni.tex:130 | 01-accesso.png. Credenziali e ruolo autenticato, nessun selettore pubblico. |
| 2.3.1.3 — Registrazione (02-registrazione.png) | 2.3.1.3, docs/relazione/capitoli/2_funzioni.tex:137 | 02-registrazione.png. Solo cliente: personale predisposto, non registrabile dal form. |
| 2.3.2.1 — Catalogo veicoli (customer-catalogo.png) | 2.3.2.1, docs/relazione/capitoli/2_funzioni.tex:152 | 03-cliente-catalogo.png, 05-cliente-noleggio.png. Due cataloghi; filtri storici assenti, caricamento/Riprova selettivi. |
| 2.3.2.2 — Acquisti e richieste (customer-acquisti.png) | 2.3.2.2, docs/relazione/capitoli/2_funzioni.tex:165 | 08-cliente-operazioni.png. Richieste e saldo; registro completo del manager distinto, annullamento autonomo assente. |
| 2.3.2.3 — Noleggi e test drive (customer-noleggi-test-drive.png) | 2.3.2.3, docs/relazione/capitoli/2_funzioni.tex:177 | 04-cliente-dialogo-test-drive.png, 06-cliente-dialogo-noleggio.png, 08-cliente-operazioni.png. Dialoghi e operazioni personali; annullamento del solo test drive, non del noleggio. |
| 2.3.2.4 — Proposta di vendita del proprio veicolo (customer-cessione.png) | 2.3.2.4, docs/relazione/capitoli/2_funzioni.tex:193 | 07-cliente-cessione.png. Preventivo facoltativo, stima persistita; invio senza accredito, zero non accreditabile. |
| 2.3.2.5 — Conto dimostrativo (customer-conto.png) | 2.3.2.5, docs/relazione/capitoli/2_funzioni.tex:203 | 09-cliente-identita.png. Header e dialogo comune; prelievo autonomo e scheda bancaria separata assenti. |
| 2.3.2.6 — Profilo (customer-profilo.png) | 2.3.2.6, docs/relazione/capitoli/2_funzioni.tex:212 | 09-cliente-identita.png. Dialogo comune nel contesto cliente; riferimenti storici e saldo positivo non equivalgono a protezione totale. |
| 2.3.3.1 — Veicoli in attesa (salesman-proposte.png) | 2.3.3.1, docs/relazione/capitoli/2_funzioni.tex:226 | 10-venditore-richieste.png. PURCHASE nella tabella comune, non tab o filtro dedicato. |
| 2.3.3.2 — Richieste di acquisto (salesman-richieste.png) | 2.3.3.2, docs/relazione/capitoli/2_funzioni.tex:236 | 10-venditore-richieste.png. SALE nella stessa tabella; autorizzazione distinta da incasso del saldo. |
| 2.3.3.3 — Stato dei veicoli (salesman-stato-veicoli.png) | 2.3.3.3, docs/relazione/capitoli/2_funzioni.tex:245 | 11-venditore-inventario.png. Sola destinazione SALE/RENT; noleggio in corso impedisce il passaggio e futuri rimborsati. |
| 2.3.3.4 — Panoramica prenotazioni (salesman-panoramica.png) | 2.3.3.4, docs/relazione/capitoli/2_funzioni.tex:254 | Nessuna immagine corrente disponibile. Panoramica prenotazioni venditore assente; nessuna schermata sostitutiva inventata. |
| 2.3.3.5 — Profilo (salesman-profilo.png) | 2.3.3.5, docs/relazione/capitoli/2_funzioni.tex:261 | 09-cliente-identita.png. Rinvio al dialogo acquisito dal cliente; apertura staff riscontrata staticamente, nessuna acquisizione dedicata. |
| 2.3.4.1 — Parco veicoli (manager-parco-veicoli.png) | 2.3.4.1, docs/relazione/capitoli/2_funzioni.tex:278 | 14-manager-veicoli.png, 15-manager-sconti.png. Basi nella tab Veicoli, sconti nella tab Sconti; accordi fissati invariati. |
| 2.3.4.2 — Marche e modelli (manager-marche-modelli.png) | 2.3.4.2, docs/relazione/capitoli/2_funzioni.tex:292 | 12-manager-marchi.png, 13-manager-modelli.png, 15-manager-sconti.png. Marchi e Modelli in due tab, Sconti separata; massimo non cumulato. |
| 2.3.4.3 — Registro delle operazioni (manager-vendite.png) | 2.3.4.3, docs/relazione/capitoli/2_funzioni.tex:305 | 16-manager-registro.png. SALE/PURCHASE/PENALTY non negativi; noleggi non automaticamente righe di Sale. |
| 2.3.4.4 — Profilo (manager-profilo.png) | 2.3.4.4, docs/relazione/capitoli/2_funzioni.tex:315 | 09-cliente-identita.png. Rinvio al dialogo cliente; manager senza ricarica, nessuna schermata dedicata attestata. |

[Provenienza e impronte individuali](schermate-18.json). Le sedici immagini disponibili sono del ticket 02 (12 novembre, dati demo, PostgreSQL temporaneo/Xvfb); nessuna acquisizione o avvio nel ticket 13. Le immagini condivise non trasformano diciotto funzioni in diciotto schermate correnti; le assenze sono spiegate nel corpo.

## Undici diagrammi originali

[Tavole, basi degli autori, adattamenti, fonti, nativi/export e collocazioni](tavole.md); [hash uguali al ticket 10](confronto-export-10.json), [43 coppie incluse quelle di dettaglio](diagrammi-43.json), [osservazione di ogni foglio A3](controllo-visivo.md). I rifacimenti del 10 sono riusati, non sostituiti dalle sole viste della prima stesura.

## Due tabelle argomentative

| Originale | Destinazione | Funzione conservata e adattamento |
| --- | --- | --- |
| Relazione_SWE/capitoli/3c_prezzi_sicurezza.tex:21–31 (tab:prezzi) | docs/relazione/capitoli/3_implementazione.tex:631–643 (tab:impl-importi) | Origine degli importi: listino/base, acconto, saldo, noleggio e stima; momento vincolante. Anomalia saldo/listino risolta dall’accordo persistito; base manuale nella formula e geometrica corrente. Limite: Formula didattica, double e arrotondamenti; nessuna stima di mercato, zero nel dato diverso da movimento consentito. |
| Relazione_SWE/capitoli/3d_dao.tex:60–71 (tab:garanzie) | docs/relazione/capitoli/3_implementazione.tex:1143–1157 (tab:impl-garanzie) | Regola, meccanismo e limite delle garanzie transazionali e dei vincoli. Connection/callback, lock, UNIQUE/EXCLUDE e Observer transazionale sostituiscono UnitOfWork e vecchie lacune. Limite: Letture autonome, PK/email locali e assenza di autorizzazione universale, retry/outbox o prova dedicata commit/rollback. |

[Fonti per tabella](tabelle-2.json): i dettagli originari superati non diventano garanzie attuali. Gli esempi discussi restano in 3.3 e 3.4, pagine 92–105; le due tabelle sono alle pagine fisiche 93 e 102.

## Ventitré frammenti originali

[Corrispondenza individuale con testo originale e destinazioni](corrispondenza-23.json): quattordici nel capitolo 3, nove nel 4 incluso il comando shell. [42 blocchi correnti](frammenti-correnti.json): 27 in 3 e 15 in 4, comandi compresi; [presenza completa nel PDF](pagine-frammenti.json). I confronti analitici [11](../ticket-11/confronto.md) e [12](../ticket-12/confronto.md) motivano responsabilità, preparazione, assert, ordine e riletture. Nel ticket 13 i due capitoli sono conservati letteralmente, non riscritti per raggiungere i conteggi.
