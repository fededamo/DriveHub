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
