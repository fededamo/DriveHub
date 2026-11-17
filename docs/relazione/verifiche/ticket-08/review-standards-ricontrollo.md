# Ricontrollo Standards — messaggi TeX del ticket 08

15 novembre 2026. Ricontrollo indipendente senza lettura del rapporto Spec.
**Esito: conforme con limite diagnostico esplicito; nessun rilievo Standards aperto.**

## Rettifica e riscontri

La frase «Nessun avviso rilevato nel log finale conservato» del rapporto
Standards iniziale era troppo ampia: nei due log sono presenti cinque
`ignored error: Infinite glue shrinkage found in box being split`.
Il presente ricontrollo rettifica quella conclusione; il rapporto iniziale
rimane conservato.

Esaminati `valutazione-messaggi-tex.md`, `diagnostica-tex.json`, filtro
aggiornato, comandi, README 08, controllo visivo e F13 della matrice finale.
Estratta tramite AST ed eseguita soltanto l'espressione del filtro corrente
sui due log: cinque messaggi ciascuno, identici alle liste diagnostiche.
Verificate impronte dello script e dei log; log ed esiti originari
`autonomia-pacchetto.json`/`esito-documentale.json` coincidono con il freeze.
Le modifiche ai cinque input congelati riguardano esclusivamente verbali,
matrice e controllore 08; sorgenti LaTeX e artefatti finali sono invariati.

## Norme ed euristiche

La correzione rispetta `docs/agents/domain.md` (attribuire i risultati alla
versione esaminata) e `piano.md`, «Criteri di lavoro» (distinguere storico,
nuove verifiche e limiti). Il campo storico `warnings_last_pass: []` viene
contestualizzato come risultato del filtro limitato, senza riscriverlo.
Il filtro attuale conserva e stampa i messaggi per la valutazione manuale.
Non sono introdotte soppressioni nei sorgenti né dichiarate cause o
risoluzioni non dimostrate. Nessuno smell applicabile richiede interventi.

Il principale documenta la nuova osservazione delle pagine fisiche 101–106.
Ho campionato personalmente 101 e 106: intestazioni, continuazioni e righe
risultano leggibili, senza tagli o sovrapposizioni rilevati.

## Artefatti e limiti

PDF SHA-256: `e046f9efc5b3a7411354bfdde71b288e0d748f01ebea8c2aa7b3534c5a8990d4`.
ZIP SHA-256: `9b450b53f3bf623b919f81edf63f7b288728bea92818f25b8da8011f6c93837c`.

Nessuna nuova compilazione o riesecuzione del confronto sull'estrazione,
non più disponibile. I cinque messaggi restano non risolti e con causa
non dimostrata; altre versioni LaTeX e stampa richiedono valutazione.
Chiusura amministrativa e revisione degli autori restano distinte.
