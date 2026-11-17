# Integrazione delle tre tavole degli autori

15 novembre 2026. Richiesta: includere le tavole realizzate dagli autori,
correggendole con la skill draw.io e mantenendone stile e struttura.
Le copie corrette sono integrate nella relazione, affiancate ai dettagli
precedenti. Gli originali in `docs/` e i 64 file delle 32 tavole precedenti
sono conservati senza modifiche, con impronte verificate.

| Originale | Copia corretta | Pagina PDF / numero stampato |
| --- | --- | --- |
| `Users SWE.drawio` | [Casi d'uso](../../diagrammi/casi-uso-autori.drawio) | 8 / 5 |
| `Page Navigation Diagram.drawio` | [Navigazione](../../diagrammi/navigazione-autori.drawio) | 24 / 21 |
| `SWE ER.drawio` | [ER e legenda](../../diagrammi/database-er-autori.drawio) | 49–50 / 46–47 |

Sono conservati forme, identificativi, famiglie di caratteri, associazioni,
generalizzazioni e disposizione d'insieme. Aggiustamenti locali di dimensioni
e collegamenti evitano sovrapposizioni. Le quattro pagine delle tavole sono
A3 orizzontali, con didascalie e numerazione; il resto del PDF è A4.

## Correzioni

- Navigazione: LoginView al posto della Welcome, registrazione solo cliente
  con ingresso diretto alla sua dashboard, quattro/due/cinque tab effettive;
  gestione account e ricarica qualificate come comandi o dialoghi.
- Casi d'uso: registrazione collegata al cliente, rimossa la consultazione
  delle prenotazioni del venditore; aggiunte identità, ricarica e riferimenti
  UC coerenti con le schede. Conservati attori, generalizzazioni e include.
- ER: aggiunti `price_mode`, `agreed_total` e i tipi di richiesta/registro;
  tipi enumerati nativi e unicità della richiesta per veicolo aggiornati.
  Il controllo indipendente ha individuato anche il collegamento Client–Request
  ancorato alla riga interna salesman: ora termina sull'entità Request.
  La gerarchia User resta concettuale; PK e email sono qualificate per ruolo,
  distinguendo le tre tabelle dalla vista `user`. Corrette legenda e note.
- Il capitolo 5 attribuisce agli autori le tavole originali e a Codex soltanto
  correzioni e integrazione. Il generatore non ricava diagrammi dal codice.

## Strumenti e verifiche eseguite

Gli XML sono copie modificate degli originali mediante
[aggiorna-tavole.py](aggiorna-tavole.py), con controlli strutturali e repair pass
draw.io risultato neutro. Non rilanciare il generatore dopo modifiche manuali
alle copie senza prima conservarle: riparte dagli originali.

Il download Desktop da GitHub/SourceForge non è riuscito. Il caricamento nella
pagina pubblica è stato respinto dalla revisione automatica prima di eseguire
l'azione. Alternativa completata: codice pubblico del viewer ufficiale scaricato
in `/tmp`, eseguito esclusivamente su 127.0.0.1 con CSP `connect-src 'self'`,
frame esterni vietati e browser limitato al dominio 127.0.0.1. Nessun diagramma
è stato caricato nella pagina pubblica. Versioni e impronta del renderer:
[strumenti.json](strumenti.json). Stampa vettoriale Chromium in A3; XML completo
incorporato nel Subject dei PDF, incluse entrambe le pagine ER.

La compilazione usa XeLaTeX/TeX Live 2025 estratto in `/tmp`, senza installazione
di sistema. Il [log finale](compilazione-finale.txt) termina con exit 0:
113 pagine, nessun riferimento indefinito, Warning, Overfull, Underfull o
carattere mancante. Restano cinque messaggi `Infinite glue shrinkage` già
presenti nell'appendice C: osservate le pagine PDF 107–112, senza tagli o
sovrapposizioni rilevati. Non sono dichiarati risolti o soppressi.

[controlla.py](controlla.py) verifica originali e dettagli conservati, estremi
dei collegamenti, 13 tabelle/62 attributi/15 relazioni da FK del DDL corrente,
35 coppie XML/PDF coerenti, presenza nel PDF, quattro pagine A3, font incorporati,
link e 101 membri dello ZIP identici ai sorgenti correnti. [Esito](esito.json).

Il [pacchetto corrente](sorgenti-relazione.zip) è stato estratto da zero e
compilato in tre passaggi, con input editoriali autonomi. Testi e pixel delle
113 pagine coincidono con il PDF distribuito: [confronto](pacchetto.json).
Osservate anche le quattro tavole nel PDF finale, le pagine introduttive
interessate e il paragrafo aggiunto al capitolo 5. Non effettuata una nuova
ispezione visiva di tutte le altre pagine immutate.

Nessuna compilazione Java, prova applicativa, accesso DB, commit, invio o
pubblicazione. Windows, macOS e stampa fisica non verificati. Le evidenze e il
pacchetto del ticket 08 restano storici e non vengono sovrascritti.

Per ripetere i controlli documentali con PyMuPDF disponibile:

```bash
python docs/relazione/verifiche/tavole-autori/controlla.py
```

Le [differenze del documento](modifiche-documento.diff) sono rispetto al
pacchetto finale del ticket 08; le nuove tavole sono nei sei file `*-autori`.
