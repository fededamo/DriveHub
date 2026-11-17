# 12 — Riallineamento del testing

Status: done

Blocked by: None.

## What to build

Un capitolo di testing che mantenga le cinque sezioni e gli argomenti
specifici dell'originale: tecnologie, invarianti, autenticazione, prezzi e
sicurezza, vendite, noleggio/conto, transazioni/Observer, mapping e schema/demo.
Descrivere le prove attuali attraverso preparazione, azione, oracolo,
estratto significativo e limite, mantenendo corretta la classificazione.

Riferimenti: [specifica](../spec.md), [matrice, capitolo 4](../../allineamento.md#capitolo-4),
[censimento dei frammenti](../../allineamento.md#frammenti-ed-esempi-da-conservare-per-funzione)
e [indice](../../indice.md#4-testing).

## Acceptance criteria

- [x] Tutte le voci 4–4.5 hanno un argomento identificabile e profondità
  analoga all'originale, conservando i contenuti validi della prima stesura.
- [x] Ripristinata la sottosezione Tecnologie e strumenti, con dipendenze e
  profili reali: niente H2, Mockito, JaCoCo o ciclo Maven inesistenti.
- [x] Sei sottosezioni del Business Logic coprono gli argomenti originali,
  usando classi correnti; dove manca un test unitario equivalente, assenza
  esplicita e rinvio alla prova di integrazione, senza riclassificarla.
- [x] Mapping/DAO e schema/demo hanno sottosezioni proprie; mantenuti rollback
  dopo scritture, conflitti concorrenti e isolamento del DB di test con
  descrizione degli oracoli e dei limiti effettivi.
- [x] I nove frammenti originali di testing, compresi workflow e comando
  d'esecuzione, hanno esempi attuali o scostamenti motivati. Nessuna prova
  dedicata a errore del commit/rollback viene dichiarata senza riscontro.
- [x] Scenari funzionali e harness UI distinti dalla suite JUnit; il nome
  FullWorkflow non è presentato come prova di tutti i flussi completi.
- [x] Risultati storici associati a fonte, versione, ambiente e data; comandi
  attuali distinti dai comandi effettivamente eseguiti in passato. Nessuna
  nuova esecuzione o percentuale di copertura inventata.
- [x] Appendice C e rinvii restano coerenti con i casi principali ricollocati
  e le schede aggiuntive. La matrice estesa completa gli esempi nel corpo.
- [x] Baseline dei materiali, anche non tracciati; evidenze pregresse intatte.
  Verifica esatta degli estratti, compilazione reale e ispezione visiva delle
  pagine coinvolte; estratto e risultati proposti agli autori.

## Comments

16 novembre 2026: quarto capitolo nell'ordine di lavorazione; nessun prerequisito
tecnico negli altri riallineamenti. Le evidenze esistenti permettono di
documentare le prove senza rieseguirle. Non aggiungere test applicativi per
riempire un titolo storico; nessun accesso a DB operativi, commit o invio.

17 novembre 2026: concluso tecnicamente dopo compilazione completa, controlli
documentali e ispezione delle pagine coinvolte. Quindici voci, nove funzioni
originali e quindici blocchi censiti, tre estratti precedenti identici. PDF di 151
pagine, estratto 4/C di 22 pagine; capitoli 1–3 conservati su 101 pagine a testo e pixel;
software, tavole, originali e storici immutati. Review indipendenti Standards
e Spec conformi, nessun rilievo aperto; precisazione heap ricontrollata nel
PDF conclusivo. Cinque messaggi TeX preesistenti persistono in C alle pagine fisiche 145–149,
osservati e documentati. [Fonti, esiti e review](../../verifiche/ticket-12/README.md).
Nessuna approvazione degli autori, prova applicativa, DB, commit o invio;
il pacchetto e l'integrazione del ticket 13 restano da eseguire.
