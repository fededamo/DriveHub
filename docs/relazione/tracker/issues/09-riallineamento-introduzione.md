# 09 — Riallineamento dell'introduzione

Status: done

Blocked by: None.

## What to build

Un capitolo introduttivo che mantenga descrizione, stack e semplificazioni
dell'originale, con contenuto attuale e profondità proporzionata. Riusare il
testo valido della prima stesura: questo capitolo richiede soprattutto una
verifica di corrispondenza, non una riscrittura senza motivo.

Riferimenti: [specifica](../spec.md), [matrice, capitolo 1](../../allineamento.md#capitolo-1)
e [indice](../../indice.md#1-introduzione).

## Acceptance criteria

- [x] Tutte le voci 1–1.3 e i loro argomenti hanno un riscontro esplicito:
  contesto, tre ruoli, catalogo comune, tecnologie e limiti del prototipo.
- [x] Le descrizioni di UI, PostgreSQL, avvio e registrazione corrispondono
  alle fonti attuali; nessun ripristino di H2, FXML o registrazione staff.
- [x] Conservati punto di vista della concessionaria e contenuti validi;
  eventuali differenze dall'originale sono motivate per la relativa voce.
- [x] Conservata prima delle modifiche una baseline del materiale coinvolto,
  compresi i file non tracciati; vecchia relazione e verifiche restano intatte.
- [x] Verificati sorgenti, rinvii e compilazione reale; controllate visivamente
  le pagine del capitolo e registrati esiti, data e limiti. Anche se il testo
  non richiede cambiamenti, il confronto deve dimostrarlo.
- [x] Aggiornati i soli stati documentali pertinenti e proposto il capitolo
  agli autori, senza attribuire approvazione umana o nuove prove applicative.

## Comments

16 novembre 2026: ticket della nuova fase autorizzata dagli autori. Primo
nell'ordine di lavorazione; il completamento tecnico non blocca gli altri
capitoli per una dipendenza inesistente. Nessun commit o invio previsto.

16 novembre 2026 — conclusione tecnica del ticket 09. Confrontati tutti i
17 blocchi dell'originale e riscontrate 29 fonti attuali; conservato il testo
valido, con il solo riordino di ambiente e processi aziendali in 1.3.
Baseline di 2.430 file, inclusi i non tracciati, e 217 copie editoriali
acquisite prima delle modifiche; originali ed evidenze precedenti intatti.

Compilazione integrale XeLaTeX in tre passaggi finali riusciti, PDF di 113
pagine ed estratto di tre pagine. Osservati capitolo 1, indice e pagine dei
cinque messaggi TeX residui, preesistenti in appendice C; 62 riferimenti
risolti. Solo la pagina fisica 6 cambia rispetto alla baseline.
[Confronto, fonti, comandi e limiti](../../verifiche/ticket-09/README.md).

[Review indipendenti Standards e Spec](../../verifiche/ticket-09/revisione.md)
concluse con zero rilievi. Capitolo proposto agli autori per revisione:
nessuna loro approvazione attribuita. I ticket 10–13 e il rifacimento degli
undici diagrammi restano da eseguire. Nessun test applicativo, modifica di
software/schema/dati, commit, invio o pubblicazione.
