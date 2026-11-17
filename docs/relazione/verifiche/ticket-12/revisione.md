# Review indipendenti concluse — ticket 12

17 novembre 2026. Punto fisso: working tree iniziale del ticket 12, inclusi
non tracciati, PDF e storici, conservato in baseline.json/archivio editoriale.
Due subagenti a contesto iniziale separato; nessuna lettura reciproca dei
rapporti nelle review tecniche. Il candidato conclusivo è identificato da
candidato-review.json: PDF di 151 pagine ed estratto di 22 pagine, compilazioni heap2/3.

Correzione autonoma del principale durante la review: i192MB del POM sono
il massimo heap (-Xmx), non tutta la memoria JVM. Entrambi gli assi hanno
ricontrollato sorgente/POM e pagina107 conclusiva. Candidato precedente
preservato; le altre150pagine sono identiche nei pixel. Nessun rilievo
indipendente sostanziale o correzione applicativa richiesta.

I due rapporti restano autonomi e sono riportati integralmente sotto.
La chiusura tecnica non equivale all'approvazione degli autori.

## Standards

# Review Standards — ticket 12

17 novembre 2026. Review indipendente, senza consultare l'asse Spec.

**Esito: conforme nel perimetro esaminato. Zero violazioni documentate,
zero smell euristici da segnalare.** Il candidato è quello fissato da
[candidato-review.json](candidato-review.json): PDF completo di 151 pagine
con SHA-256 `be80f25106eafe5c7a05a7f428b3c805d75b5d67d908daed05c490a3d16ba239`.

Standards consultati: [AGENTS](../../../AGENTS.md), [istruzioni documentali
di dominio e tracker](../../../agents/), [piano](../../piano.md),
[indice](../../indice.md), [matrice](../../allineamento.md),
[specifica](../../tracker/spec.md), glossario, ADR 0001/0002 e istruzioni
TeX del README/LEGGIMI. Il delta mantiene linguaggio della concessionaria,
attribuzione delle evidenze alla versione esaminata, distinzione fra
riscontri statici, risultati storici e nuove verifiche documentali.
Nessun intervento sui diagrammi richiede l'attivazione del percorso draw.io.

Il punto fisso effettivo è il working tree iniziale, inclusi i non tracciati:
[baseline](baseline.json), archivio editoriale e [delta](delta-documentale.diff).
I confronti Git vuoti non sono stati usati come prova. Controllo autonomo:
2.804 file iniziali, nessuna rimozione, soltanto capitolo 4, C e PDF
modificati; 223 membri dell'archivio tutti conformi alle impronte iniziali.
Il delta corrisponde ai sorgenti attuali. Originali, fonti applicative,
diagrammi ed evidenze pregresse sono conservati.

Esaminati gli hunk e la baseline dei dodici smell: nessuna euristica
giustifica un rilievo; estratti letterali e matrici di tracciabilità hanno
una funzione documentale esplicita. Non trasformate in findings le verifiche
già automatizzate.

Osservate tutte le pagine fisiche 2–4, 106–118 e 143–151: codice,
didascalie, tabelle e rinvii leggibili, senza tagli o sovrapposizioni.
Ricontrollata la pagina 107 corretta: massimo heap, coerente con
`-Xmx192m` nel POM. I 25 rendering corrispondono al PDF corrente;
l'estratto riproduce le 22 pagine previste. Verificati autonomamente
101 pagine dei capitoli 1–3 identiche a pixel e testo di 5/A/B conservato
escludendo i piè di pagina. Restano i cinque messaggi TeX preesistenti
in C, senza difetti visivi osservati.

Limiti: nessuna esecuzione Java, Maven, JUnit, harness, UI o DB; nessun
checker storico eseguito. Compilazione valutata attraverso log e artefatti,
senza ripeterla. La review non sostituisce l'approvazione degli autori.

## Spec

# Review Spec — ticket 12

17 novembre 2026. Asse indipendente; nessun rapporto Standards consultato.
**Esito: PASS, nessun rilievo Spec residuo.** Non attribuisce approvazione
agli autori né completamento dell'integrazione finale.

Punto fisso: working tree iniziale in `baseline.json` (2.804 file, inclusi
non tracciati), con archivio editoriale verificato. I diff/log Git dal
commit `61ce18ae1d4e0458b63e970b793b0b3a634a3adf` sono vuoti:
il confronto pertinente è quello locale con archivio e delta documentale.
Candidato conclusivo: cinque impronte di `candidato-review.json` riscontrate;
PDF completo `be80f25106eafe5c7a05a7f428b3c805d75b5d67d908daed05c490a3d16ba239`
(151 pagine), estratto 4/C di 22 pagine.

## (a) Requisiti mancanti o parziali

Nessuno individuato. Rispetto a «profondità analoga all'originale,
conservando i contenuti validi» (ticket, righe 21–22), sono riconoscibili
quindici voci, sei temi Business Logic e due sottosezioni DAO/schema,
con preparazione, azione, oracoli e limiti. Le nove funzioni originali,
compreso il comando shell, hanno adattamenti motivati (righe 31–33).
Verificati direttamente 13 intervalli sorgente/LaTeX e la loro presenza
ordinata nel PDF; i tre estratti iniziali rimangono identici.

## (b) Ampliamenti estranei

Nessuno. Il confronto delle 2.804 impronte iniziali rileva modifiche solo
a capitolo 4, C e PDF. Software, test, harness, fonti originali e prove
pregresse rimangono integri, coerentemente col perimetro documentale
(spec, Implementation Decisions 1).

## (c) Requisiti apparentemente implementati ma errati

Nessun errore residuo individuato. Ricontrollati oracoli critici su
identità, prezzi, penale, Rent, rollback dopo scritture, nuove letture,
snapshot e gate/PID. Ricavati autonomamente E1 423/82 e E2 394/363,
incluso il tentativo con quattro errori; confermati sette marcatori E3
e 28 input E2 differenti. Classificazione e limiti rispettano ticket
righe 25–38. Correzione heap coerente con `-Xmx192m`: diff sorgente
di una riga, solo pagina 107 cambiata e riaperta; nessuna regressione
Spec osservata (righe 23–24).

Limiti: nessuna esecuzione Java/JUnit/UI/DB. Compilazione riscontrata nei
log; controllo visivo autonomo campionato sulle pagine 107, 110, 112,
147, integrato dal verbale completo. Persistono i cinque messaggi TeX
preesistenti dichiarati, senza nuovi box/glifi/rinvii irrisolti.

## Esito e limiti

Standards: 0 violazioni documentate / 0 smell azionabili. Spec: 0 rilievi
residui. Nessun nuovo Java/JUnit/GUI/DB o commit, invio o pubblicazione.
Persistono i cinque messaggi TeX preesistenti in C, con osservazione delle
pagine coinvolte; non dichiarati eliminati. Ticket 13 e approvazione autori
rimangono distinti. Gli stati amministrativi sono aggiornati dopo i due PASS;
nessuna ulteriore modifica a sorgenti LaTeX/PDF/estratto/log revisionati.

Ricontrolli separati degli stati amministrativi dopo i due esiti tecnici:
[Standards](review-standards-stati.md) e [Spec](review-spec-stati.md), entrambi
conformi. Le cinque impronte degli artefatti revisionati sono rimaste invariate.
I manifest di chiusura, successivi ai ricontrolli, sono verificati in
[finalizzazione.txt](finalizzazione.txt).
