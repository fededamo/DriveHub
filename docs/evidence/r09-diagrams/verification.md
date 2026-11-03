# Ticket17 — diagrammi ed evidenze della versione locale corrente

Sessione3novembre2026, soltanto17/AC14. Letti AGENTS, piano, glossario, ADR0001,
testing, installation, architecture ed evidenze16; [01–16 chiusi](prerequisites.json),
senza criteri aperti. Relazione_SWE esclusa da letture, copia, ricerche,
generazioni e verifiche. Nessun commit/staging, refactor o anticipo del18.

## Versione e perimetro verificato

HEAD iniziale `c2a81122ee6be44760ded4160fde229bd33e837c` **non è la versione
completa verificata**. [Baseline](baseline.json):1507file iniziali, compresi
modificati/non tracciati e ignorati; progetto, tracker e piano esterni, esclusi
`.git`, `target` e relazione. Copia byte per byte in
`/tmp/drivehub-r17-baseline/tree`, diff iniziale daHEAD in
[baseline-head.patch](baseline-head.patch), [stato](baseline-status.txt) e
[diff indice](baseline-index.patch). Nessun file preesistente rimosso.

L'installazione runtime è una copia di questa fotografia, non un checkoutHEAD.
Solo `application.properties` sostituito nella copia con credenziali private del
DB sacrificabile; sorgenti Java, test, POM, schema/demo e harness del repository
restano invariati. [Identità degli input](runtime-identity.json) e
[manifest della suite finale](full-inputs.sha256.json) delimitano esattamente la
versione eseguita. Il diff17 è confrontato con la baseline locale:
[patch di review](review.patch), [file della review](review-files.sha256.json).
I documenti di chiusura e la conservazione finale sono registrati separatamente.

## Diagrammi correnti e storico

[Indice dei diagrammi usati](../../diagrams/README.md): **18sorgenti PlantUML e
18SVG**, cinque viste classi, un package, sei sequenze, due casi d'uso, tre
navigazioni e una macchina a stati del veicolo. Non sostituiscono il codice con
un progetto ideale: utenti per sottotipi, DAO con Connection, servizi concreti,
totale/acconto persistiti, tipi/segni operazioni, lock, Observer prima del commit,
scadenza all'ingresso, rimborsi e Task correnti. Precisazioni teoriche16 mantenute:
factory statiche ≠ Factory Method GoF; DAO/DI/composition root distinti; nessuna
pretesa di SOLID/DDD puri, snapshot unico listino o garanzia globale di concorrenza.

Ogni vista è collegata a sorgenti/test reali; i package sono dipendenze del
codice, le sequenze scambi/risultati, la navigazione scene/tab/dialoghi.
Nessun riferimento corrente a classi inesistenti usato come componente.
I nomi della vecchia versione compaiono solo nelle esclusioni esplicative.

[Inventario storico](historical-artifacts.json): **37artefatti visivi conservati
identici**, inclusi17puml originali,8draw.io,3mockup e9screenshot. Identificati
come storici nell'indice, senza spostarli o rigenerarli. ER escluso dalla selezione
usata17. Due vecchi `source-manifest.json`:150voci ciascuno,149percorsi mancanti
e hash POM diverso; appartengono a `it.unifi.ing.drivehub`, non al codice corrente.
[Indice evidenze](../README.md): risultati01–16 restano delle rispettive sessioni,
in particolare **394/394del16 è storico**.

## Ambiente e risultati osservati17

[Comandi, tentativi e limiti](commands.md), [ambiente](environment.txt),
[versioni](versions.txt). Ricontrollati strumenti16 presenti: Java/javac21.0.12.1,
Maven3.9.16 sullo stesso JDK, PostgreSQL16.15, Xvfb. Nuovo cluster solo
**127.0.0.1:55435**, ruoli/DB non amministrativi distinti e sentinella; display
isolato91. Nessuna configurazione privata pubblicata o DB preesistente usato.

| Verifica nuova | Esito effettivo |
|---|---|
| Compilazione Java21 e mirati su DB reale | **79/79**, zero failure/errori/salti; [log](targeted.log), [XML](targeted-surefire/), [summary](targeted-summary.json) |
| TableLifecycleUiCheck14 riusato | **UI CHECK SUCCESS**: tre dashboard, tab/tastiera, prezzi, caricamento su lock PostgreSQL, errore/riprova e risultato/errore obsoleto, celle senza query; [log](table-lifecycle.log) |
| AppStartupUiCheck15 riusato | **APP STARTUP SUCCESS**: App.start, LoginView con due tab, tema e Tab; [log](startup-check.log) |
| Suite completa finale `mvn -Ppostgres-tests clean test` | **394/394**,47senzaDB/347PostgreSQL, zero failure/errori/salti; [log nuovo](full.log), [XML](full-surefire/), [summary](full-summary.json), [gruppi](full-groups.json) |
| PlantUML1.2026.2, SVG e PNG dei soli18diagrammi correnti | Render finali **exit0**; [comandi](render.log), [hash/dimensioni e ispezione](render-inventory.json) |
| Sentinella dopo mirati, GUI e suite | **READ ONLY**,13tabelle/7sequenze identiche [prima](before-suite-application.sha256.json)/[dopo](after-suite-application.sha256.json); [privilegi/esito](after-suite-environment.txt) |
| Arresto servizi creati17 | PostgreSQL/Xvfb arrestati, listener55435/socket91 assenti; [esito](shutdown.txt) |

Questo394/394è una **nuova esecuzione17**, distinta da quella16; nessuna percentuale
di copertura dedotta. Primo mirato fallito per server perso fra sandbox, poi79/79;
primo avvio finale interrotto143 senza riepilogo, poi nuova suite completa exit0.
Conservati log/summary falliti o incompleti senza presentarli come successi.

Ispezionati18PNG dei diagrammi a risoluzione naturale/zoom e8schermate UI:
[accesso](startup-login.png), [manager](manager-prices.png), [caricamento](loading.png),
[riprova](retry.png), [cliente vendita](client-sale.png), [noleggio](client-rent.png),
[listino aggiornato](client-refreshed.png), [inventario venditore](salesman-inventory.png).
Corrette le prime impaginazioni e rigenerati solo i render correnti. Le firme
abbreviate rimandano ai sorgenti; i render SVG rimangono zoomabili.

## Review, conservazione e limiti

Review indipendenti sulla [patch congelata](review.patch), SHA256
`1d07fb5bca5373935e98d7ea833d334aade090683d0ebe4f1290a6fe7c6317cc`,
130file: [Standards](standards-review.md) **0violazioni/0smell azionabili**;
[Spec iniziale](spec-review.md) **1P2** sui ritorni dai dialoghi cliente.
Corretto soltanto quel diagramma: test drive torna alla tab vendita/noleggio
chiamante, ricarica conserva la tab, annullamento distinto da comando/esito
dopo chiusura anche in errore. [Delta corretto](review-correction.patch), SHA256
`cc93bd7778f40c56e2e55022e05d2ebda70fecaabb3bec69fa0b86fde29d5abc`,
[4hash](review-correction-files.sha256.json), nuovi render exit0 e ispezione.
[Follow-up Standards](standards-followup.md) **0violazioni/0smell** e
[follow-up Spec](spec-followup.md) **P2risolto,0residui**. Rapporti iniziali
conservati; nessun input runtime cambiato dopo le prove.

[Conservazione](preservation.json):1507file iniziali presenti,1502identici,
5aggiornamenti intenzionali (README, architecture, testing, piano, ticket17),
zeroeliminazioni. Codice/POM/test/schema/demo, evidenze01–16, originali storici
e ticket18 intatti; HEAD/indice invariati. [Controlli finali](final-checks.json)
su riferimenti, XML, hash/render, preservazione e assenza di segreti privati.
[Diff completo della sessione](session.patch) e [hash finali](session-files.sha256.json)
distinti dalla review iniziale; [delta documentale di chiusura](closing-docs.patch)
non è una nuova prova runtime. [Hash degli archivi](delivery.json).
Ricontrollo indipendente del solo delta di chiusura:
[Standards](standards-closing-review.md), [Spec](spec-closing-review.md).

**Ticket17 chiuso,5/5criteri verificati; AC14/#14 e R09 completati,18/20rilievi
chiusi.** R11/ticket18 e AC13 trasversale restano aperti; nessuna chiusura integrata
anticipata. Nessun commit/staging.

Nessun nuovo test della documentazione o modifica a codice/POM/test/DB/UI.
Nessuna rigenerazione degli artefatti storici o grafi Graphify: nessun grafo
esistente, letture mirate sufficienti. Le verifiche dei link non leggono la relazione.

Limiti: Linux/WSL/Xvfb e rendering software, non altri OS/display, screen reader
o stampa/A4. Non tutti i gesti/dialoghi di navigazione riesercitati nella GUI:
parte della corrispondenza deriva dall'ispezione dei sorgenti; test di servizio
non equivalgono a test UI. Nessuna nuova prova di installer, migrazione completa,
prestazioni universali o assenza globale di deadlock. Temporanei non garantiti
nelle sessioni future. Verifica integrata e tracciabilità complessiva18 restano aperte.
