# Ticket 06 — Testing

13 novembre 2026. Repository `/home/federico/dev/Drivehub/DriveHub`, HEAD
`61ce18ae1d4e0458b63e970b793b0b3a634a3adf`, invariato; nessun commit.
[Specifica operativa ricevuta](specifica-ticket.md), indipendente da ticket
preesistenti o contenuti temporanei. Skill implement applicata con i vincoli
espliciti dell'utente; TDD/test applicativi/commit esclusi. Code-review:
due assi indipendenti, Standards e Spec, con contesti nuovi e separati.

## Risultato

- [PDF completo](../../relazione-drivehub.pdf), 105 pagine A4.
- [Estratto capitolo 4 e appendice C](ticket-06-per-revisione.pdf), 14 pagine,
  dalle pagine fisiche 64–68 e 97–105 del documento completo.
- [Testing LaTeX](../../capitoli/4_testing.tex),
  [matrice C LaTeX](../../appendici/c_verifiche.tex),
  [main](../../main.tex), [contenitore appendici](../../appendici/appendici.tex).
- [Matrice affermazioni/frammenti→fonti](matrice-fonti.md),
  [94 fonti con impronte](fonti.json), [tre frammenti esatti](frammenti.json),
  [riscontro storico](riscontro-storico.json).
- [Comandi e tentativi](comandi.md), [ambiente](ambiente.txt),
  [compilazione finale](compilazione.txt), [log TeX](latex-log.txt),
  [controlli documentali](controlli-documento.txt),
  [esito strutturato](esito-documentale.json),
  [osservazione visiva](controllo-visivo.md).
- [Delta documentale effettivo](delta-documentale.diff),
  [baseline iniziale](baseline-conservazione.json),
  [copie iniziali](prima/), [conservazione](conservazione.txt).
- [Revisioni indipendenti](revisione.md),
  [segnalazione separata sugli strumenti storici](difetti-applicativi.md).

Capitoli 1–4 e appendici A–C sono completi e **proposti per la revisione degli
autori**, non approvati. Capitolo 5 provvisorio; questa è una stesura progressiva.
Nessun nuovo diagramma: riusati riferimenti, schede e tavole esistenti.

## Esiti e portata

**Revisioni Standards e Spec concluse e conformi**, zero rilievi aperti:
[rapporti indipendenti e aggregazione](revisione.md).

Compilazione reale e controllo documentale conclusi con exit 0. Il PDF ha
144 collegamenti interni risolti e 84 font incorporati; nessun riferimento
irrisolto, box difettoso o carattere mancante nel log finale. Restano 96 avvisi
noti sulla versione PDF 1.7 delle figure rispetto all'impostazione interna 1.5;
output effettivo PDF 1.7, figure leggibili. Sono dichiarati, non soppressi.

Gli esiti applicativi citati sono storici: E1 9 novembre, 423/0/0/0 e mirato
separato 82/0/0/0; E2 3 novembre, 394/0/0/0 finale; E3 GUI della fase iniziale
del 3 novembre. C distingue tentativi falliti, commit/tree o manifest, ambiente,
comandi, prove selezionate e limiti. E1 non ha manifest runtime o XML per metodo;
la coerenza col tree corrente è ricostruita, non attestata da impronte di
esecuzione. E2 ha 28 input diversi da oggi e non include il nuovo test enum.
Nessun conteggio somma sessioni o equivale a copertura del codice.

## Conservazione e riproduzione documentale

La baseline è stata creata prima delle modifiche e non rigenerata: 2.235 file,
2.234 invariati più **una sola modifica di stato** in capitolo 3, confrontata
con l'intero testo precedente (tolto il riferimento a 4/C provvisori).
Software, test, SQL, dati, vecchia relazione, evidenze e manifest 01–05 intatti.
Anche il testo PDF 1–3/A–B è equivalente, salvo quella frase, esclusi piè di
pagina e sillabazione a fine riga. Le sei copie in prima rendono isolabile il
delta anche se la documentazione era già non tracciata. Il diff Git vuoto non
è una prova di conservazione.

Per riprodurre il PDF basta il contenuto del repository e Tectonic 0.17.0:
nessun frammento o input editoriale da /tmp. La prima compilazione richiede
rete per i pacchetti LaTeX. Nell'ambiente corrente, da docs/relazione:

```bash
XDG_CACHE_HOME=/tmp/drivehub-ticket06-tools/cache \
  /tmp/drivehub-ticket06-tools/tectonic --only-cached --untrusted \
  --keep-intermediates --keep-logs --outdir build main.tex
```

Per i controlli usare Python/PyMuPDF e [controlla-documento.py](controlla-documento.py),
che genera anche estratto e rendering. Percorsi e versioni effettivi sono in
comandi/ambiente; su un altro PC fornire gli strumenti e omettere only-cached
alla prima compilazione. Nessuna seconda distribuzione LaTeX verificata.

## Limiti

Nessun test applicativo, accesso DB, compilazione Java, harness, cattura della UI
corrente, verifica Windows o commit. I difetti 04–05 restano distinti; il nuovo
riscontro di incompatibilità riguarda l'harness storico SaleOutcome, lasciato
intatto. L'esistenza di un metodo, il rendering del PDF e una suite storica
riuscita non certificano l'intero requisito o il software corrente.
La revisione degli autori resta da svolgere.
