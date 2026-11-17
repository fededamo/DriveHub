"""Censimento documentale corrente; non esegue checker o processi applicativi."""
from pathlib import Path
import hashlib
import json
import re
import xml.etree.ElementTree as ET
from urllib.parse import unquote

import pymupdf as fitz

OUT = Path(__file__).resolve().parent
DOC = OUT.parents[1]
ROOT = DOC.parents[1]


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(name, value):
    (OUT / name).write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n')


def expand(base, name, active):
    path = base / name
    assert path not in active, 'Input ripetuto/ciclico: ' + str(path)
    active.append(path)
    rows = []
    for number, line in enumerate(path.read_text().splitlines(), 1):
        if line.lstrip().startswith('%'):
            continue
        inputs = re.findall(r'\\input\{([^}]+)\}', line)
        if inputs:
            for target in inputs:
                rows.extend(expand(base, target + '.tex', active))
        else:
            rows.append((str(path.relative_to(ROOT)), number, line))
    return rows


def headings(rows):
    result = []
    counters = [0, 0, 0, 0]
    kinds = ['chapter', 'section', 'subsection', 'subsubsection']
    for offset, (path, line, text) in enumerate(rows):
        if re.match(r'\s*\\(?:chapter|section|subsection|subsubsection)\*?\{[^}]*$', text):
            text += ' ' + rows[offset + 1][2].strip()
        match = re.match(r'\s*\\(chapter|section|subsection|subsubsection)(\*?)(?:\[[^]]+\])?\{([^}]+)\}', text)
        case = re.match(r'\s*\\scheda\{([^}]+)\}\{([^}]+)\}', text)
        if case:
            kind, star, title = 'subsection', '', case[1] + ' --- ' + case[2]
        elif match and '#' not in text:
            kind, star, title = match.groups()
            title = title.removeprefix('\\raggedright ')
        else:
            continue
        level = kinds.index(kind)
        if star:
            identifier = '.'.join(map(str, counters[:level])) + '.a'
        else:
            counters[level] += 1
            counters[level + 1:] = [0] * (3 - level)
            identifier = '.'.join(map(str, counters[:level + 1]))
        result.append(dict(id=identifier, level=kind, star=bool(star), title=title,
                           source=path, line=line, offset=offset))
    for i, row in enumerate(result):
        level = kinds.index(row['level'])
        stop = next((x['offset'] for x in result[i + 1:] if kinds.index(x['level']) <= level), len(rows))
        content = rows[row['offset']:stop]
        row['content_sha256'] = hashlib.sha256('\n'.join(x[2] for x in content).encode()).hexdigest()
        row['intervals'] = []
        for path in dict.fromkeys(x[0] for x in content):
            numbers = [x[1] for x in content if x[0] == path]
            row['intervals'].append(dict(source=path, first=min(numbers), last=max(numbers)))
    return result


original_inputs, current_inputs = [], []
original_rows = expand(ROOT / 'Relazione_SWE', 'main.tex', original_inputs)
current_rows = expand(DOC, 'main.tex', current_inputs)
old = headings(original_rows)
new = [h for h in headings(current_rows) if int(h['id'].split('.')[0]) <= 4]
assert len(original_inputs) == 24 and len(old) == len(new) == 102, (len(original_inputs), len(old), len(new))
assert [(h['id'], h['level'], h['star']) for h in old] == [(h['id'], h['level'], h['star']) for h in new]
save('input-editoriali.json', {
    'original': [{'source': str(p.relative_to(ROOT)), 'sha256': sha(p)} for p in original_inputs],
    'current': [{'source': str(p.relative_to(ROOT)), 'sha256': sha(p)} for p in current_inputs],
    'excluded_original_inputs': ['capitoli/2c_architettura.tex', 'capitoli/2d_dati_flussi.tex']})
matrix = {}
for line in (DOC / 'allineamento.md').read_text().splitlines():
    match = re.match(r'\| \[(\d[\d.a]*) ([^]]+)\]\(([^)]+)\) \| (.*?) \| (.*?) \| (.*?) \|', line)
    if match:
        matrix[match[1]] = match[6]
assert len(matrix) == 102
coverage = []
for a, b in zip(old, new):
    chapter = int(a['id'].split('.')[0])
    ticket = chapter + 8
    coverage.append(dict(id=a['id'], original=a, current=b,
        treatment=matrix[a['id']],
        verification=f'docs/relazione/verifiche/ticket-{ticket:02}/confronto.md',
        technical_sources=f'docs/relazione/verifiche/ticket-{ticket:02}/fonti.json',
        limit='Riscontro statico S0 e conservazione dei contenuti verificati; gli esiti applicativi restano storici E1/E2/E3. Adattamenti e assenze motivati nella trattazione e nel confronto indicato.'))
save('corrispondenza-102.json', coverage)
lines = ['# Corrispondenza consolidata delle 102 voci', '',
    '17 novembre 2026. Input ricorsivi effettivi di entrambi i main; livelli e ordine confrontati. '
    'Gli intervalli completi e le impronte del contenuto espanso sono in [corrispondenza-102.json](corrispondenza-102.json). '
    'Il controllo strutturale sostiene il confronto sostanziale nei verbali 09–12, riletti e riscontrati nel ticket 13; non lo sostituisce. '
    'Capitoli 1–4 conservati byte per byte rispetto alla baseline 13. Fonti statiche, esempi, preparazione/azione/oracoli e limiti sono precisati nei confronti collegati e in [riscontri tecnici](fonti-tecniche.md).', '',
    '| Voce / livello | Origine → destinazione | Contenuto conservato, ricollocazione o adattamento | Fonte e limite |',
    '| --- | --- | --- | --- |']
for row in coverage:
    a, b = row['original'], row['current']
    ticket = int(row['id'].split('.')[0]) + 8
    lines.append(f"| {row['id']} / {a['level']}{'*' if a['star'] else ''} | `{a['source']}:{a['line']}` {a['title']} → `{b['source']}:{b['line']}` {b['title']} | {row['treatment']} | [Confronto {ticket:02}](../ticket-{ticket:02}/confronto.md), [fonti](../ticket-{ticket:02}/fonti.json). S0; prove e assenze nei limiti del relativo argomento, senza nuove esecuzioni. |")
(OUT / 'corrispondenza-102.md').write_text('\n'.join(lines) + '\n')

# Ricontrollo dei manifest di estratti, senza eseguire i vecchi checker.
blocks = {}
for chapter in [3, 4]:
    name = '3_implementazione.tex' if chapter == 3 else '4_testing.tex'
    text = (DOC / 'capitoli' / name).read_text()
    for match in re.finditer(r'\\begin\{lstlisting\}\[(.*?)\]\n(.*?)\\end\{lstlisting\}', text, re.S):
        label = re.search(r'label=\{?([\w:.-]+)', match[1])[1]
        assert label not in blocks
        blocks[label] = match[2]
fragments, functions = [], []
for ticket in [11, 12]:
    folder = DOC / f'verifiche/ticket-{ticket}'
    for fragment in json.loads((folder / 'frammenti.json').read_text()):
        source = ROOT / fragment['source']
        if fragment.get('kind') == 'commands':
            actual = blocks[fragment['label']].replace('\\\n', ' ').splitlines()
            actual = [re.sub(r'\s+', ' ', x).strip() for x in actual if x and not x.startswith('#')]
            assert actual == [c['text'] for c in fragment['commands']]
            for command in fragment['commands']:
                assert source.read_text().splitlines()[command['line'] - 1] == command['text']
        else:
            excerpt = ''.join(source.read_text().splitlines(True)[fragment['firstline'] - 1:fragment['lastline']])
            assert excerpt == blocks[fragment['label']] == fragment['text'], fragment['label']
            assert hashlib.sha256(excerpt.encode()).hexdigest() == fragment['sha256']
        fragments.append(dict(fragment, verified_in_ticket=13))
    for row in json.loads((folder / 'copertura-frammenti.json').read_text()):
        interval = row.get('intervallo_originale', [row.get('firstline'), row.get('lastline')])
        source = ROOT / row['originale']
        excerpt = '\n'.join(source.read_text().splitlines()[interval[0] - 1:interval[1]])
        assert '\\begin{lstlisting}' in excerpt and '\\end{lstlisting}' in excerpt
        assert set(row['estratti']) <= blocks.keys()
        functions.append(dict(row, original_interval=interval,
            original_excerpt=excerpt, original_sha256_13=hashlib.sha256(excerpt.encode()).hexdigest(),
            source_manifest=f'docs/relazione/verifiche/ticket-{ticket}/frammenti.json',
            limit='Il vecchio meccanismo non è ripristinato; interpretare gli estratti con preparazione, chiamate e riletture del corpo e con il limite dichiarato.'))
assert len(blocks) == len(fragments) == 42 and len(functions) == 23
save('frammenti-correnti.json', fragments)
save('corrispondenza-23.json', functions)

# Le copie valide sono riusate: si confronta il nativo con quello incorporato nell'export.
diagrams = []
for native in sorted((DOC / 'diagrammi').glob('*.drawio')):
    tree = ET.parse(native)
    for graph in tree.iter('root'):
        ids = [c.get('id') for c in graph]
        assert len(ids) == len(set(ids))
        for cell in graph:
            for field in ['source', 'target', 'parent']:
                assert cell.get(field) is None or cell.get(field) in ids
            if cell.get('edge') == '1':
                assert cell.find('mxGeometry') is not None
    export = Path(str(native) + '.pdf')
    pdf = fitz.open(export)
    if native.name in pdf.embfile_names():
        embedded = pdf.embfile_get(native.name)
        method = 'allegato nativo'
        assert embedded == native.read_bytes(), native.name
    else:
        embedded = unquote(pdf.metadata['subject']).replace(r'\(', '(').replace(r'\)', ')')
        method = 'Subject XML URL-encoded'
        assert ET.canonicalize(embedded, strip_text=True) == ET.canonicalize(native.read_text(), strip_text=True), native.name
    diagrams.append(dict(native=str(native.relative_to(ROOT)), source_sha256=sha(native),
        export=str(export.relative_to(ROOT)), export_sha256=sha(export),
        pages=len(pdf), native_matches_export=True, embedding=method))
assert len(diagrams) == 43
save('diagrammi-43.json', diagrams)

ui_images = [
    ['01'], ['01'], ['02'], ['03', '05'], ['08'], ['04', '06', '08'], ['07'], ['09'], ['09'],
    ['10'], ['10'], ['11'], [], ['09'], ['14', '15'], ['12', '13', '15'], ['16'], ['09']]
ui_limits = [
    'Welcome separata assente: ingresso e accesso sono LoginView.',
    'Credenziali e ruolo autenticato, nessun selettore pubblico.',
    'Solo cliente: personale predisposto, non registrabile dal form.',
    'Due cataloghi; filtri storici assenti, caricamento/Riprova selettivi.',
    'Richieste e saldo; registro completo del manager distinto, annullamento autonomo assente.',
    'Dialoghi e operazioni personali; annullamento del solo test drive, non del noleggio.',
    'Preventivo facoltativo, stima persistita; invio senza accredito, zero non accreditabile.',
    'Header e dialogo comune; prelievo autonomo e scheda bancaria separata assenti.',
    'Dialogo comune nel contesto cliente; riferimenti storici e saldo positivo non equivalgono a protezione totale.',
    'PURCHASE nella tabella comune, non tab o filtro dedicato.',
    'SALE nella stessa tabella; autorizzazione distinta da incasso del saldo.',
    'Sola destinazione SALE/RENT; noleggio in corso impedisce il passaggio e futuri rimborsati.',
    'Panoramica prenotazioni venditore assente; nessuna schermata sostitutiva inventata.',
    'Rinvio al dialogo acquisito dal cliente; apertura staff riscontrata staticamente, nessuna acquisizione dedicata.',
    'Basi nella tab Veicoli, sconti nella tab Sconti; accordi fissati invariati.',
    'Marchi e Modelli in due tab, Sconti separata; massimo non cumulato.',
    'SALE/PURCHASE/PENALTY non negativi; noleggi non automaticamente righe di Sale.',
    'Rinvio al dialogo cliente; manager senza ricarica, nessuna schermata dedicata attestata.']
ui = [row for row in coverage if row['id'].startswith('2.3.') and row['original']['level'] == 'subsubsection']
assert len(ui) == 18
screenshots = []
for row, numbers, limit in zip(ui, ui_images, ui_limits):
    original = row['original']
    content = '\n'.join(x[2] for x in original_rows[original['offset']:]
                        [:next((h['offset'] - original['offset'] for h in old if h['offset'] > original['offset']), len(original_rows))])
    image = re.search(r'\\(?:uiscreen|uiauthscreen)\{([^}]+)\}', content)
    assert image, row['id']
    source = ROOT / 'Relazione_SWE/images/mockups' / image[1]
    assert source.is_file(), source
    current = [p for number in numbers for p in (DOC / 'screenshot').glob(number + '-*.png')]
    assert len(current) == len(numbers)
    screenshots.append(dict(id=row['id'], function=original['title'],
        original=str(source.relative_to(ROOT)), original_sha256=sha(source),
        destination=row['current'], current_images=[dict(source=str(p.relative_to(ROOT)), sha256=sha(p)) for p in current],
        treatment=row['treatment'], limit=limit,
        provenance='Immagini attuali acquisite il 12 novembre 2026 nel ticket documentale 02, PostgreSQL temporaneo/Xvfb, dati demo e date adattate; nessun nuovo avvio o screenshot nel ticket 13.'))
save('schermate-18.json', screenshots)
templates = [row for row in coverage if row['id'].startswith('2.2.')]
assert len(templates) == 6
save('template-6.json', [dict(id=row['id'], original=row['original'], destination=row['current'],
    treatment=row['treatment'], source='capitoli/2_template.tex e servizi/test citati nei sei paragrafi Prove e portata',
    limit='Attori UI non autorizzazione universale; alternative numerate e oracoli selettivi, non ogni ramo certificato.') for row in templates])
save('tabelle-2.json', [
    dict(original='Relazione_SWE/capitoli/3c_prezzi_sicurezza.tex:21–31', original_label='tab:prezzi',
         destination='docs/relazione/capitoli/3_implementazione.tex:631–643', current_label='tab:impl-importi',
         function='Origine degli importi: listino/base, acconto, saldo, noleggio e stima; momento vincolante.',
         treatment='Anomalia saldo/listino risolta dall’accordo persistito; base manuale nella formula e geometrica corrente.',
         sources=['service/PriceCalculatorService.java:26–111', 'service/PurchaseService.java:65–183', 'service/SaleToDealershipService.java:55–182', 'docs/adr/0001-base-manuale-e-prezzo-concordato.md'],
         limit='Formula didattica, double e arrotondamenti; nessuna stima di mercato, zero nel dato diverso da movimento consentito.'),
    dict(original='Relazione_SWE/capitoli/3d_dao.tex:60–71', original_label='tab:garanzie',
         destination='docs/relazione/capitoli/3_implementazione.tex:1143–1157', current_label='tab:impl-garanzie',
         function='Regola, meccanismo e limite delle garanzie transazionali e dei vincoli.',
         treatment='Connection/callback, lock, UNIQUE/EXCLUDE e Observer transazionale sostituiscono UnitOfWork e vecchie lacune.',
         sources=['dao/DatabaseManager.java:66–106', 'event/VehicleEventPublisher.java:28–32', 'src/main/resources/db/schema.sql:79–96', 'src/main/resources/db/schema.sql:147–310', 'dao/postgres/PostgresUserDao.java:215–291'],
         limit='Letture autonome, PK/email locali e assenza di autorizzazione universale, retry/outbox o prova dedicata commit/rollback.')])
print('PASS: 24 input originali, 102 voci/livelli/ordine, 23 funzioni originali, 42 blocchi correnti, 43 coppie native/export.')
