# Comandi, ambiente e confini

Directory di lavoro `/home/federico/dev/Drivehub/DriveHub`, salvo compilazione
in `docs/relazione`. Tutto il contenuto operativo viene dalla richiesta e dalle
fonti del repository; /tmp è solo destinazione ricreata di strumenti/cache.
Non sono stati invocati Maven test/package, Java/javac applicativi, psql, server
PostgreSQL, Xvfb, Robot o harness. Nessun commit/staging/push.

Letture: `rg`, `nl`, `sed`, `cat`, Python per JSON/XML/log/impronte e `git`
per stato, cronologia e confronti. `git diff -- src pom.xml` vuoto;
review storica confrontata con `git diff d4d82c1815e1fe2d107331fb4f2211b2bc3ca850 HEAD -- src pom.xml`, vuoto.
Questi sono confronti di contenuti, non nuove prove applicative.

Strumenti iniziali assenti: Tectonic, pdflatex, latexmk e renderer PDF nel PATH;
PyMuPDF non importabile. Sono stati predisposti senza installazione di sistema:

```bash
mkdir -p /tmp/drivehub-ticket06-tools
curl -fL --retry 2 -o /tmp/drivehub-ticket06-tools/tectonic.tar.gz \
  https://github.com/tectonic-typesetting/tectonic/releases/download/tectonic%400.17.0/tectonic-0.17.0-x86_64-unknown-linux-gnu.tar.gz
tar -xzf /tmp/drivehub-ticket06-tools/tectonic.tar.gz -C /tmp/drivehub-ticket06-tools
/home/federico/.local/bin/uv venv --python /usr/bin/python3 /tmp/drivehub-ticket06-tools/venv
/home/federico/.local/bin/uv pip install \
  --python /tmp/drivehub-ticket06-tools/venv/bin/python \
  --cache-dir /tmp/drivehub-ticket06-tools/uv-cache pymupdf
```

Download e installazione temporanea conclusi con exit 0. Versioni verificate
[ambiente.txt](ambiente.txt), archivio e binario [impronte](strumenti-sha256.txt).
La prima compilazione ha riempito una cache nuova, non riusato vecchi contenuti
privati. I download hanno richiesto rete fuori sandbox, autorizzata dal controllo
automatico; nessun rifiuto di approvazione.

Comando con recupero risorse da `docs/relazione`; per i passaggi cached è stato
aggiunto `--only-cached`, con un nome log distinto per ogni tentativo:

```bash
XDG_CACHE_HOME=/tmp/drivehub-ticket06-tools/cache \
  /tmp/drivehub-ticket06-tools/tectonic --untrusted \
  --keep-intermediates --keep-logs --outdir build main.tex \
  > verifiche/ticket-06/compilazione-risorse.txt 2>&1
cp build/main.pdf relazione-drivehub.pdf
cp build/main.log verifiche/ticket-06/latex-log.txt
```

- `compilazione-baseline.txt`: compilazione reale della stesura iniziale per
  predisporre strumenti/cache, exit 0; PDF iniziale conservato in `prima/`.
- `compilazione-prima-stesura.txt`: prima 4/C, exit 0, con box difettosi dovuti
  a identificatori lunghi e giustificazione delle celle. Non versione consegnata.
- `compilazione-impaginazione.txt`: corrette celle con raggedright e interruzioni
  locali degli identificatori, ma download font impedito dalla rete sandbox,
  errore xdvipdfmx. Il tentativo Python precedente usava per errore un percorso
  relativo alla radice dalla directory LaTeX (`FileNotFoundError`); la modifica
  è stata poi applicata dalla radice. Nessun file applicativo toccato.
- `compilazione-cache-font-mancante.txt`: tentativo con `--only-cached`, exit 1,
  cmmi10.pfb mancante. Non dichiarato successo.
- `compilazione-risorse.txt`: recupero font con rete e compilazione, exit 0.
  Dopo la sintesi editoriale è stata eseguita una nuova compilazione cached;
  il suo log è conservato in `compilazione-prima-revisione-visiva.txt`.
  `compilazione.txt` registra la vera compilazione finale con `--only-cached`,
  dopo le precisazioni SQL/ingresso pubblico, exit 0; `latex-log.txt` ne copia il log TeX. Rimangono solo gli avvisi noti
  sulla versione PDF delle figure (conteggio nel risultato documentale).

Verifica effettiva dalla radice:

```bash
/tmp/drivehub-ticket06-tools/venv/bin/python \
  docs/relazione/verifiche/ticket-06/controlla-documento.py \
  > docs/relazione/verifiche/ticket-06/controlli-documento.txt 2>&1
```

Il primo controllo riferimenti trattava `Future.get` e l'estensione `.log` come
metodi locali: raffinato il filtro distinguendo API standard ed estensioni,
senza modificare l'applicazione o indebolire controlli semantici. Il primo errore
è conservato in `controlli-primo-riferimenti.txt`.
Il controllo verifica baseline, fonti, frammenti e testo PDF, indice, font,
riferimenti e log; legge registrazioni storiche e genera estratto/rendering.
Il [verbale visivo](controllo-visivo.md) è separato dagli assert.

I comandi **applicativi da riprodurre, non eseguiti qui**, sono nel capitolo 4.5
e in docs/testing.md/installation.md: senza DB `mvn test`, con prerequisiti e
sentinella `mvn -Ppostgres-tests clean test`, quindi harness R08 su display
isolato in sequenza. Il runner R11 ha dipendenze storiche e harness incompatibili:
non è una ricetta portabile sul checkout corrente.
