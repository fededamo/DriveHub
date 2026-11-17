# Review indipendente — Standards

17 novembre 2026. Prima review sul candidato PDF SHA-256
`b87ffa38ce1ffe7a6569ffb7d8e48a10497582e5a617136e3715c096eacd8236`.
Punto fisso: working tree iniziale conservato in `baseline.json` e
`baseline-editoriale.tar.gz`; HEAD `61ce18ae1d4e0458b63e970b793b0b3a634a3adf`.
Diff e log Git vuoti riscontrati; esaminati il delta documentale effettivo
e i nuovi materiali 11. Nessun rapporto dell'asse Spec consultato.

## Violazioni documentate

- **Media — affermazione tecnica errata**, `capitoli/3_implementazione.tex:40`:
  «AbstractUser, nonostante il nome, non è dichiarata astratta» contraddice
  `src/main/java/it/unifi/drivehub/domain/user/AbstractUser.java:11`,
  `public abstract class AbstractUser implements User`.
  Viola `docs/agents/domain.md`, regola «Riscontrare le affermazioni
  sull'implementazione nel codice e nello schema correnti», e i criteri di
  lavoro di `piano.md`. Correggere la descrizione della classe e rigenerare
  il PDF. Rilievo comunicato e accolto; la correzione annunciata richiede
  ancora il ricontrollo del candidato finale.

## Euristiche Fowler

Nessun nuovo smell rilevante nel delta o nello script documentale:
responsabilità unica, helper limitati e controlli coerenti con il compito.
I listati applicativi autentici sono materiale citato; questa review non
propone refactor del software. Le euristiche non sono violazioni normative.

## Verifiche effettive e limiti

Letti istruzioni editoriali, piano, indice, matrice, glossario, README,
istruzioni di consegna e due ADR; nessun CONTRIBUTING/CODING_STANDARDS
individuato nel contesto fornito. Letti capitolo, delta, checker, verbali,
log e sorgenti tecnici mirati.

Controlli indipendenti in sola lettura: 3.277 file iniziali presenti, sole
due modifiche previste; 223 copie archiviate e 72 fonti autentiche; diff
esatto; 27 estratti corrispondenti a sorgenti e PDF; 13 precedenti conservati
(11 identici, due completati dalla graffa). PDF 143 pagine, estratto 23;
78 pagine dei capitoli 1/2 identiche anche nei pixel, altre 38 invariate nel
testo salvo numero finale; 79 rinvii, 224 link e 106 font verificati.
Osservati nove provini, corrispondenti ai 35 rendering del PDF.

Tre log finali letti: cinque messaggi TeX preesistenti in C, senza difetti
visibili o nuovi box segnalati. Nessuna ricompilazione, esecuzione del
checker scrivente, prova applicativa, DB o UI; nessuna approvazione degli
autori presunta. Esito iniziale: **un rilievo Standards aperto**.
