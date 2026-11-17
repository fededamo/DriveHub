# Ticket 07 — Uso dell'AI

Status: done — capitolo proposto agli autori, non approvato.

13 novembre 2026, HEAD `61ce18ae1d4e0458b63e970b793b0b3a634a3adf`, invariato.
[Specifica operativa](specifica-ticket.md) ricevuta in conversazione; niente
dipendenze da ticket precedentemente pubblicati o da contenuti editoriali in /tmp.

## Consegna per gli autori

- [PDF completo](../../relazione-drivehub.pdf).
- [Estratto capitolo 5](capitolo-5-per-revisione.pdf).
- [Sorgenti LaTeX e figure in ZIP](sorgenti-relazione.zip), autonomi per la
  compilazione con Tectonic; [main](../../main.tex) e [capitolo 5](../../capitoli/5_uso_ai.tex).
- [Matrice affermazioni→fonti](matrice-fonti.md), [43 impronte delle fonti](fonti.json),
  [registri ZIP](registri-zip.json) e [cronologia Git](cronologia-git.txt).
- [Compilazione](compilazione.txt), [log TeX](latex-log.txt),
  [controlli](controlli-documento.txt), [risultato strutturato](esito-documentale.json),
  [osservazione visiva](controllo-visivo.md), [revisioni indipendenti](revisione.md).
- [Baseline iniziale](baseline-conservazione.json), [copie iniziali](prima/),
  [delta testuale](delta-documentale.diff), [inventario del delta](delta-files.json),
  [conservazione](conservazione.txt), [manifest finale](SHA256SUMS).

Le quattro sezioni distinguono dichiarazione degli autori, riscontri e lacune.
Pochi esempi rappresentativi, nessun catalogo dei diciotto ticket applicativi.
La dichiarazione sul lavoro manuale e sulla revisione con Codex resta quella
del piano; non si attribuisce l'origine di singole righe. Decisioni umane,
controlli automatici e review di agenti sono distinti. Non sono inventati prompt,
modelli, tempi, percentuali o approvazioni. Capitoli 1–5 e appendici A–C sono
completi e **proposti agli autori per revisione**, non approvati.

## Verifica e conservazione

**Review indipendenti concluse: Standards conforme, Spec conforme; zero rilievi
aperti su entrambi gli assi.** [Rapporti e aggregazione](revisione.md).
I 22 input congelati sono invariati dopo le review; la chiusura aggiorna soltanto
stato del ticket e resoconti/inventari amministrativi, senza modificare il PDF.

PDF completo: **107 pagine A4**; capitolo5 alle **69–71** fisiche, estratto di
tre pagine. **156 collegamenti interni risolti e86font incorporati**. Osservate
tutte le nuove pagine, indice/frontespizio e confini (11pagine complessive).
Nessun Underfull/Overfull, riferimento irrisolto o carattere mancante nel log
finale; restano96avvisi noti sulla versione delle figure PDF, output1.7.
SHA256: `1fcc18bb426426cdc9809a69ac04993c3ffeb384a5014e515719a12643e71f4e`.

PDF realmente compilato con Tectonic 0.17.0, senza input editoriali da /tmp.
Strumenti esistenti del ticket 06 verificati nuovamente: [ambiente](ambiente.txt),
[impronte](strumenti-sha256.txt), [comandi e tentativi](comandi.md).
Il PDF consegnato è identico a `build/main.pdf`. L'estratto è ricavato dal PDF
completo, non da una compilazione alternativa.

La baseline è stata acquisita prima delle modifiche: 2.291 file tracciati e non
tracciati non ignorati; nessun ignorato aggiuntivo nelle evidenze01–06. Non è
rigenerata. Le sette modifiche previste sono capitolo5, main, README, piano,
PDF e rimozione di due sole frasi di stato ormai inesatte nei capitoli3–4.
Le altre parti completate, vecchia relazione, software/test/SQL/dati ed evidenze
e manifest01–06 restano intatti. Il diff Git vuoto non dimostra la conservazione:
sono usate impronte, copie iniziali e confronto dell'intero testo pregresso PDF.

Sono nuovi soltanto i controlli documentali, la compilazione e l'osservazione
del PDF. Ricalcolo dei conteggi da log/XML storici senza eseguirne i comandi:
394/0/0/0 per R11 del3novembre e423/0/0/0 per enum del9novembre, separatamente.
Verificate tutte le24voci del manifest ZIP, con18ticket; estratti da6membri
con righe e impronte conservati qui. Le lacune runtime e di approvazione sono
esplicite nella matrice e nel capitolo.

## Riproduzione documentale

Dalla directory `docs/relazione`, nell'ambiente corrente:

```bash
XDG_CACHE_HOME=/tmp/drivehub-ticket06-tools/cache \
  /tmp/drivehub-ticket06-tools/tectonic --only-cached --untrusted \
  --keep-intermediates --keep-logs --outdir build main.tex
cp build/main.pdf relazione-drivehub.pdf
cp build/main.log verifiche/ticket-07/latex-log.txt
/tmp/drivehub-ticket06-tools/venv/bin/python verifiche/ticket-07/controlla-documento.py
```

Su un altro computer fornire Tectonic0.17.0 e Python/PyMuPDF e adattare i percorsi;
alla prima compilazione omettere `--only-cached` per il download dei pacchetti.
Lo ZIP dei sorgenti contiene anche le figure e le tavole draw.io modificabili.
Nessuna seconda distribuzione LaTeX verificata. I rendering del controllo sono
in `build/visual-ticket07/`, ignorati; il verbale è persistente e distinto dagli assert.

## Limiti

Nessun test applicativo, compilazione Java, DB, harness, UI corrente, nuovo
diagramma, rigenerazione di evidenze precedenti o commit. Nessun backup privato
consultato, né controllo Windows/screen reader o certificazione del software.
Il primo tentativo cached ha fallito per un font delle note mancante; il log è
conservato. Il recupero delle risorse e la ricompilazione sono riusciti.
Rimangono gli avvisi noti sulla versione delle figure PDF; dettagli nel risultato.
La revisione degli autori resta da svolgere.
