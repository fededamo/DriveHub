# Revisione indipendente conclusa — ticket 04

**13 novembre 2026: Standards e Spec conformi, nessun rilievo aperto.**
Le due revisioni finali sono state concluse da revisori con contesti nuovi
e separati, senza consultare il rapporto dell'altro asse. Entrambi hanno
riscontrato i materiali e aperto/osservato il PDF definitivo.
L'approvazione degli autori è un passaggio distinto e non è già attribuita.

## Riferimento e delta effettivo

HEAD iniziale e finale: `61ce18ae1d4e0458b63e970b793b0b3a634a3adf`.
Nessun commit creato. Comandi fissati della skill code-review:

```bash
git diff 61ce18ae1d4e0458b63e970b793b0b3a634a3adf...HEAD
git log 61ce18ae1d4e0458b63e970b793b0b3a634a3adf..HEAD --oneline
```

Entrambi vuoti: `docs/relazione/` era già non tracciata. I revisori hanno
quindi esaminato il delta documentale reale: due nuovi testi, tredici
tavole draw.io/esportazioni, evidenze 04 e aggiornamenti necessari a main,
contenitore capitolo 2, due README e PDF. Le copie in [prima](prima/) e la
[baseline delle 2.116 impronte](baseline-conservazione.json) documentano
il confronto. Il diff Git vuoto non è presentato come prova di assenza
di lavoro. La specifica è [quella ricevuta dall'utente](specifica-ticket.md),
insieme alla [specifica generale](../../tracker/spec.md).

Versione finale esaminata da entrambi:
[PDF](../../relazione-drivehub.pdf), SHA-256
`0b25ad25962fb0886e408ac23d4a97a68eafe87e8fd8533b5647897fc7441a37`.
84 pagine A4, 108 collegamenti interni risolti e 71 font incorporati.
Le sezioni 2.7–2.8 occupano le pagine fisiche 32–53; il capitolo 2 le 7–53.

## Standards

**CONFORME — zero rilievi aperti.** Struttura, lingua, terminologia della
concessionaria, notazione, fonti e perimetro rispettano le istruzioni
documentali. Aggiornamenti degli esistenti limitati a integrazione e stato;
2.116 file protetti e 35 fonti riscontrati indipendentemente. Tredici tavole
osservate, otto sequenze riosservate dopo la correzione geometrica;
spazi SQL, handler e guillemets verificati nel PDF finale. Nessuna
euristica della baseline richiede un refactor applicativo.

[Rapporto indipendente Standards](revisione-standards.md).

## Spec

**CONFORME — zero rilievi aperti.** Nessun requisito mancante/parziale,
errore documentale residuo o ampliamento improprio del perimetro individuato.
I tre flussi sono coerenti con servizi, DAO, handler e dominio correnti;
connessioni autonome/riuso, lock, commit/rollback, errori, scadenze e
Observer sincrono sono distinti. Le tredici tabelle, quindici FK e quattro
enum sono riscontrati sul DDL. Tutte le tavole, indice e provvisori sono
stati osservati; controllata la conservazione dei 2.116 file.

[Rapporto indipendente Spec](revisione-spec.md).

## Rilievi risolti e verifiche interessate

I controlli preliminari avevano segnalato spazi SQL persi dalla macro
basata su `nolinkurl` e nome inesatto dell'handler della cessione.
Corretti localmente i due nuovi testi con `texttt` per espressioni con
spazi e SQ-03a/generatore/matrice con `buildSellMyCarTab`.
L'osservazione del principale ha portato anche a separare le intestazioni
delle sequenze, rendere principale il colore dell'UPDATE di SQ-01a e
correggere la resa T1 dei guillemets nei due nuovi testi.

Riesportate solo le tavole interessate, ricompilato e controllato il PDF;
la sola correzione dei guillemets ha richiesto il riscontro mirato delle
pagine32/52, senza modificare nuovamente le tavole. Le verifiche sono
registrate in [controlli di chiusura](controlli-chiusura.txt) e
[osservazione visiva](controllo-visivo.md). I due revisori finali hanno
verificato anche queste correzioni sul medesimo hash definitivo.

## Tentativi interrotti e limiti

I tentativi precedenti sono stati interrotti dal riavvio del server prima
della consegna dei rapporti conclusivi. Non sono qualificati come successi.
I rapporti collegati sopra provengono dalle due revisioni ora effettivamente
concluse. La compilazione e lo script completo sono stati eseguiti dal
principale; i revisori hanno letto i log, ricalcolato controlli e osservato
il PDF senza pretendere di aver eseguito una suite applicativa.

Nessun test applicativo, accesso DB, acquisizione UI, modifica al software,
SQL o vecchie evidenze, né commit. Restano i soli avvisi noti di versione
PDF Tectonic, i limiti delle verifiche statiche e la revisione degli autori.
Le [segnalazioni applicative](difetti-applicativi.md) restano separate:
non sono correzioni richieste o realizzate da questo ticket.

**Standards: nessun rilievo aperto. Spec: nessun rilievo aperto.**
