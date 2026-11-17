# Perimetro operativo delle review finali 13

Richiesta dell’utente: eseguire soltanto
`docs/relazione/tracker/issues/13-verifica-riallineamento.md` e completarlo.
Lo stato iniziale 09–12 è tecnicamente concluso, senza approvazione degli
autori. La review del 13 deve riguardare la nuova integrazione, PDF completo,
ZIP effettivo e prova di autonomia, non riusare le vecchie review.

Punto fisso: `baseline.json` e `baseline-editoriale.tar.gz`, acquisiti
prima delle modifiche, inclusi i non tracciati. HEAD
`61ce18ae1d4e0458b63e970b793b0b3a634a3adf`. `git diff BASE...HEAD` e log
commit sono vuoti; la review comprende `delta-documentale.diff`, i file
modificati e tutte le aggiunte `ticket-13/`. Non si richiede un commit.

Requisiti aggiuntivi espliciti dell’utente, da leggere insieme a ticket,
specifica, piano, indice, matrice, GLOSSARY, ADR pertinenti e istruzioni:

- Conservare originali, Relazione_SWE, sorgenti/figure/nativi, vecchi PDF/ZIP,
  manifest, log, XML, harness e checker; nuove evidenze solo nel ticket 13.
- Confrontare tutti i 24 input originali effettivamente inclusi e conservare
  la funzione espositiva di 102 voci con livelli/ordine, 23 frammenti
  (14+9, shell incluso), 18 schermate, 11 diagrammi, 6 template e 2 tabelle.
  Titoli e conteggi da soli non dimostrano profondità, esempi e oracoli.
- Sei schede complete in 2.2 nell’ordine UC-01/02/03/05/04/06; A aggiuntiva
  senza duplicazioni; B copre 11 package; C coerente con casi, assert e limiti.
- Capitolo 5 conservato e aggiornato per 09–13, distinguendo dichiarazioni
  umane, attività AI documentate, verifiche e limiti. Nessun prompt/modello,
  tempo, percentuale, attribuzione o approvazione inventati.
- Per ogni diagramma: base degli autori, adattamento, stile riconoscibile,
  nativo/export, coerenza corrente. Riusare i rifacimenti validi dei ticket
  conclusi; semplici viste della prima stesura non bastano. Skill draw.io
  obbligatoria per interventi; originali intatti. Nessuna nuova schermata UI.
- S0 distinto da E1/E2/E3. Leggere preparazione, chiamate, assert, ordine,
  riletture e transazioni sulle fonti; conservare versioni/date/ambienti,
  fallimenti, incompatibilità e lacune. Non sommare sessioni o inventare coverage.
- Toolchain persistente `.ticket11-tools` da verificare, `/tmp` non garantito.
  Compilare davvero fino a stabilità e osservare **tutte** le pagine incluse A3.
  Baseline 151 pagine: 1–3 a 5–105, 4 a 106–118, 5 a 119–121, A122–129,
  B130–142, C143–151. Cinque messaggi TeX preesistenti a 145–149: confronto
  documentato prima di attribuire miglioramenti o regressioni.
- ZIP aggiornato con inclusioni/esclusioni, estrazione pulita e vera
  ricompilazione senza input editoriali esterni; toolchain/font TeX ammessi
  come prerequisiti. Confronto contenuto/paginazione/resa, metadati distinti.
  Verificare l’archivio dopo l’ultima modifica e registrare impronte finali.
- Due subagenti a contesto iniziale separato: Standards e Spec. Rapporti
  separati fino alla chiusura, rilievi risolti e ricontrollati sugli artefatti
  conclusivi. Se cambiano gli input dello ZIP, ricrearlo e provarlo nuovamente.
- Aggiornare README, consegna, piano, indice, matrice e soli stati pertinenti.
  `done` soltanto dopo verifica dei criteri tecnici. Proporre agli autori
  senza invio, pubblicazione o approvazione presunta.

**Divieti:** nessuna modifica a software/test/harness/schema/dati; nessuna
build Java/Maven, JUnit, harness, avvio UI o accesso DB; nessun commit o invio.
Sono ammessi letture, checker documentali nuovi, operazioni draw.io, TeX,
rendering PDF e ZIP. I vecchi checker attestano le loro versioni: non vanno
rieseguiti sul documento avanzato o adattati. Le review possono leggere
le prove e fare verifiche documentali indipendenti entro questo perimetro.
