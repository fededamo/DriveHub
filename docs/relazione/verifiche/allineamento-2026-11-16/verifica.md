# Verifica della pianificazione del riallineamento

Data: 16 novembre 2026. Esito: controlli documentali superati; riallineamento dei capitoli ancora da eseguire.

## Perimetro

Prodotti [matrice](../../allineamento.md), [indice dettagliato](../../indice.md) e cinque ticket 09–13; aggiornati piano, specifica e README. Nessun sorgente LaTeX, diagramma, screenshot, PDF o ZIP modificato.

## Controlli eseguiti

- Seguiti gli input della relazione originale e confrontate le intestazioni con i sorgenti: **24 file attivi, 102 voci**, tutte presenti una sola volta nella matrice e nello stesso ordine. Esclusi i due capitoli non inclusi dal documento principale.
- Confrontati i numeri e l’ordine dell’indice proposto con quelli originali: tutte le voci numerate conservate; la sottosezione sul dominio resta non numerata.
- Censiti dai sorgenti **23 frammenti, 18 schermate, 11 diagrammi, 6 template completi e 2 tabelle argomentative**. Verificata la destinazione dei frammenti e la presenza dei gruppi di figure/tabelle nella matrice.
- Verificata l’esistenza delle etichette dei frammenti correnti richiamate dalla matrice e dei file collegati. Controllati anche gli ancoraggi Markdown e i riferimenti alle righe originali.
- Cinque ticket con stato ready-for-agent e criteri ancora da soddisfare. I quattro capitoli non hanno bloccanti tecnici; il ticket finale dipende esattamente da 09, 10, 11 e 12.
- Ispezionati i diff di piano, specifica e README rispetto alle copie precedenti e letti matrice, indice e criteri dei ticket per verificare conservazione del perimetro e distinzione fra proposta e documento attuale.
- Controllati newline finali, assenza di spazi a fine riga e collegamenti locali nei documenti della nuova pianificazione. I file sono non tracciati: un normale git diff non rappresenta queste modifiche.

## Conservazione

Prima delle modifiche è stata acquisita una baseline SHA-256 di **1206 file** pertinenti. Il confronto successivo rileva soltanto quattro documenti esistenti modificati e sette nuovi file di pianificazione/verifica; **1202 file preesistenti conservano la stessa impronta**.

| Area protetta | File preesistenti verificati invariati |
| --- | --- |
| `Relazione_SWE/` | 88 |
| `src/` | 93 |
| `docs/relazione/verifiche/` | 342 |

Anche tutti i LaTeX, le immagini, i diagrammi e gli artefatti della prima stesura restano invariati. I ticket e i verbali storici non sono stati aggiornati per simulare una verifica della proposta.

| Artefatto precedente conservato | SHA-256 invariata |
| --- | --- |
| `docs/relazione/relazione-drivehub.pdf` | `ffc261d6709bbdcde5d5acd119ea967aaf2ade927d3197568c7808da86d8f24b` |
| `docs/relazione/verifiche/tavole-autori/sorgenti-relazione.zip` | `5d7065226859fbb9c37124251e429a6e900588f3df6163a120efb12de1a1c231` |

Baseline e copie di lavoro sono temporanee in `/tmp/drivehub-relazione-allineamento-20261116-o_jv_hzr`. Gli script di estrazione/generazione/verifica usati in questa sessione sono in `/tmp`; non sono strumenti aggiunti al progetto né prerequisiti del futuro pacchetto LaTeX.

## Limiti

Il controllo dimostra copertura della pianificazione, validità dei rinvii e conservazione dei materiali, non l’avvenuto riallineamento dei testi. La correttezza di ogni affermazione e di ogni estratto sarà riscontrata durante i ticket di capitolo.

Non sono stati eseguiti compilazione LaTeX, controllo visivo di un nuovo PDF, esportazioni draw.io, applicazione Java, suite Maven o accessi al database. Non sono state usate review indipendenti per agenti o attribuite approvazioni degli autori. Nessun commit, invio o pubblicazione.

Il primo passo di stesura è il [ticket 09](../../tracker/issues/09-riallineamento-introduzione.md); i ticket 10–12 seguono nell’ordine dei capitoli, poi il 13 verifica il documento completo e il pacchetto.
