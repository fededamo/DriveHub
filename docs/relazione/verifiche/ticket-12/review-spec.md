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
