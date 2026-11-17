# Review Spec — ticket 09

16 novembre 2026. Review indipendente del candidato, distinta dall'asse
Standards. Riferimento: working tree acquisito in `baseline.json`, HEAD
`61ce18ae1d4e0458b63e970b793b0b3a634a3adf`, archivio editoriale e delta
comprensivo dei materiali non tracciati.

**Esito Spec: conforme; zero rilievi.**

- **Requisiti mancanti o parziali:** nessuno rilevato rispetto al ticket,
  righe 9–12 e 19–29. Confrontati i 17 blocchi originali con il capitolo
  corrente: conservati percorso 1–1.3, contesto, catalogo condiviso, tre
  ruoli, tecnologie e limiti. `confronto.md` motiva puntualmente gli
  adattamenti, compresa la consultazione delle prenotazioni ora assente
  dall'area venditore. Il riordino in
  `capitoli/1_introduzione.tex:37` precede correttamente i processi esclusi
  a riga 38; nessuna riga del capitolo è stata aggiunta o eliminata.
- **Ampliamento del perimetro:** nessuno rilevato. I sei file preesistenti
  modificati sono capitolo, PDF e quattro documenti di stato. Nuove prove,
  estratto e strumenti restano nell'area ticket 09. Software, SQL, dati,
  vecchia relazione, diagrammi ed evidenze precedenti sono conservati.
  Gli obblighi sui diagrammi restano ai ticket successivi: nessuna tavola
  appartiene all'introduzione originale.
- **Adempimenti apparentemente corretti ma errati:** nessuno rilevato.
  I requisiti del ticket, righe 21–24, corrispondono a POM, viste, servizi,
  schema, script di avvio, glossario e ADR: UI in Java, PostgreSQL,
  registrazione cliente, inizializzazione esterna e accordo economico
  persistito risultano coerenti; nessun ripristino di FXML, H2 o staff.

Rieseguito con successo `verifica-documenti.py`: 2.430 file censiti,
112/113 pagine identiche, unica variazione alla pagina fisica 6,
62 riferimenti risolti. Verificate autonomamente le impronte delle 29 fonti
e delle 217 copie archiviate. Esaminati i log dei tre passaggi finali e
i rendering delle pagine 4–6: testo completo e leggibile. I cinque messaggi
TeX ereditati dell'appendice C sono dichiarati, con pagine invariate.

Chiusura e stati finali seguono le review; non costituiscono un requisito
omesso del candidato. Resta la revisione degli autori, senza approvazione
umana implicita (ticket, righe 30–31).

Nessuna nuova compilazione o prova applicativa eseguita da questa review;
nessuna attestazione sul comportamento dinamico del software.

## Ricontrollo di chiusura

16 novembre 2026. **Conforme; zero rilievi.** Riletti delta e diciture finali:
ticket `done`, sei criteri selezionati, stati coerenti e collegamenti locali
risolti. Il settimo file modificato è il ticket; LaTeX e PDF conservano il
candidato verificato. Chiusura tecnica, lavoro futuro e approvazione degli
autori rimangono distinti; invariati limiti TeX e assenza di prove applicative.
Non ripetute compilazione o verifiche già superate.
