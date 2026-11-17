"""Verifiche esclusivamente documentali del ticket 11; nessun Java o DB."""
from pathlib import Path
import difflib
import hashlib
import json
import math
import re
import tarfile
import unicodedata
from decimal import Decimal, ROUND_HALF_UP

import pymupdf as fitz
from PIL import Image, ImageDraw

HERE = Path(__file__).resolve().parent
DOC = HERE.parents[1]
ROOT = DOC.parents[1]


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write(name, value):
    (HERE / name).write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n')


def expand(base, name):
    text = (base / name).read_text()
    return re.sub(r'\\input\{([^}]+)\}', lambda m: expand(base, m[1] + '.tex'), text)


def compact(text):
    return re.sub(r'\s+', '', unicodedata.normalize('NFKC', text)).replace('\u00ad', '')


baseline = json.loads((HERE / 'baseline.json').read_text())
changed = []
for name, row in baseline['files'].items():
    path = ROOT / name
    assert path.is_file(), ('file iniziale mancante', name)
    if sha(path) != row['sha256']:
        changed.append(name)
allowed = {'docs/relazione/' + name for name in [
    'capitoli/3_implementazione.tex', 'relazione-drivehub.pdf', 'README.md',
    'piano.md', 'indice.md', 'allineamento.md',
    'tracker/issues/11-riallineamento-implementazione.md']}
assert set(changed) <= allowed, set(changed) - allowed
current = {str(p.relative_to(ROOT)) for directory in ['src', 'docs', 'Relazione_SWE']
           for p in (ROOT / directory).rglob('*') if p.is_file()}
added = sorted(current - set(baseline['files']))
assert all(n.startswith(('docs/relazione/verifiche/ticket-11/',
                         'docs/relazione/build/ticket11/')) for n in added), added
archive = HERE / 'baseline-editoriale.tar.gz'
assert sha(archive) == (HERE / 'baseline-editoriale.sha256').read_text().split()[0]
delta = []
with tarfile.open(archive) as tar:
    for name in changed:
        if name.endswith(('.tex', '.md')):
            old = tar.extractfile(name).read().decode()
            delta.extend(difflib.unified_diff(old.splitlines(True),
                         (ROOT / name).read_text().splitlines(True),
                         fromfile='baseline/' + name, tofile='attuale/' + name))
    initial = fitz.open(stream=tar.extractfile('docs/relazione/relazione-drivehub.pdf').read(),
                        filetype='pdf')
    initial_chapter = tar.extractfile('docs/relazione/capitoli/3_implementazione.tex').read().decode()
(HERE / 'delta-documentale.diff').write_text(''.join(delta))
write('delta-files.json', {'head': baseline['head'], 'time': baseline['time'],
                          'changed': changed, 'added': added, 'deleted': []})

original = expand(ROOT / 'Relazione_SWE', 'capitoli/3_implementazione.tex')
chapter = expand(DOC, 'capitoli/3_implementazione.tex')
money = lambda value: Decimal(str(value)).quantize(Decimal('.01'), rounding=ROUND_HALF_UP)
used = money(Decimal('40000') * Decimal('0.9') * Decimal('0.80') *
             Decimal(str(math.pow(.95, 2))) * Decimal(str(math.pow(.95, .5))))
rental = money(20000 * .001 * ((1 - math.pow(.95, 3)) / .05))
assert used == Decimal('25333.87') and rental == Decimal('57.05')
assert '25.333{,}87' in chapter and '25.333,87' in chapter
assert money(30000 * .9) == Decimal('27000.00')
assert money(27000 * .2) == Decimal('5400.00')
assert Decimal('27000') - Decimal('5400') == Decimal('21600')
assert money(4000 * .2) == Decimal('800.00')
write('esempi-importi.json', {'prezzo_stima_usato': str(used), 'noleggio_tre_giorni': str(rental),
      'base_manuale_effettivo': '27000.00', 'acconto': '5400.00', 'residuo': '21600.00',
      'penale_su_acconto_4000': '800.00', 'rimborso': '3200.00',
      'method': 'Riproduzione numerica documentale delle formule lette, senza eseguire Java o DB.'})
pattern = r'\\(chapter|section|subsection|subsubsection)(?:\[[^\n]*?\])?\{([^\n]+?)\}(?=\\label|\n)'
old_headings = re.findall(pattern, original)
new_headings = re.findall(pattern, chapter)
assert len(old_headings) == len(new_headings) == 35, (len(old_headings), len(new_headings))
assert [k for k, _ in old_headings] == [k for k, _ in new_headings]
assert chapter.count('\\setcounter{secnumdepth}{3}') == 1
write('copertura-strutturale.json', [dict(originale=a, attuale=b)
                                   for a, b in zip(old_headings, new_headings)])

fragments = []
for match in re.finditer(r'% Fonte: (\S+):(\d+)-(\d+)\n(.*?)\\begin\{lstlisting\}\[(.*?)\]\n(.*?)\\end\{lstlisting\}',
                        chapter, re.S):
    source, first, last, _, options, text = match.groups()
    first, last = int(first), int(last)
    literal = ''.join((ROOT / source).read_text().splitlines(True)[first - 1:last])
    assert literal == text, ('estratto diverso dal sorgente', source, first, last)
    assert re.search(r'firstnumber=' + str(first) + r'(?=,|\])', options)
    label = re.search(r'label=(lst:[\w-]+)', options)[1]
    fragments.append(dict(label=label, source=source, firstline=first, lastline=last,
                          sha256=hashlib.sha256(text.encode()).hexdigest(), text=text))
assert len(fragments) == chapter.count('\\begin{lstlisting}') == 27
assert len({r['label'] for r in fragments}) == 27
old_blocks = {re.search(r'label=(lst:[\w-]+)', options)[1]: body
              for options, body in re.findall(
                  r'\\begin\{lstlisting\}\[(.*?)\]\n(.*?)\\end\{lstlisting\}', initial_chapter, re.S)}
new_blocks = {row['label']: row['text'] for row in fragments}
assert len(old_blocks) == 13 and old_blocks.keys() <= new_blocks.keys()
completed = {label for label in old_blocks if old_blocks[label] != new_blocks[label]}
assert completed == {'lst:update-condizionale', 'lst:mapping'}
assert all(new_blocks[label] == old_blocks[label] + '    }\n' for label in completed)
write('frammenti.json', fragments)
coverage = json.loads((HERE / 'copertura-frammenti.json').read_text())
assert len(coverage) == original.count('\\begin{lstlisting}') == 14
labels = {r['label'] for r in fragments}
assert all(set(row['estratti']) <= labels and row['adattamento'] for row in coverage)

pdf_path = DOC / 'relazione-drivehub.pdf'
assert pdf_path.read_bytes() == (DOC / 'build/ticket11/main.pdf').read_bytes()
pdf = fitz.open(pdf_path)


def area(document, title, following):
    toc = document.get_toc()
    return (next(p - 1 for level, t, p in toc if level == 1 and t == title),
            next(p - 1 for level, t, p in toc if level == 1 and t == following))


start, stop = area(pdf, 'Implementazione', 'Testing')
locations = []
for index, row in enumerate(fragments, 1):
    marker = f'Frammento 3.{index}:'
    found = [(i, pdf[i].get_text()) for i in range(start, stop)
             if marker in pdf[i].get_text()]
    assert len(found) == 1, (marker, 'didascalia mancante o duplicata')
    page, text = found[0]
    before = text.split(marker)[0]
    starts = list(re.finditer(r'(?m)^' + str(row['firstline']) + r'\s*$', before))
    assert starts, (marker, 'inizio separato dalla didascalia')
    code = re.sub(r'(?m)^\d+\s*$', '', before[starts[-1].start():])
    assert compact(code) == compact(row['text']), (marker, 'testo PDF diverso', code)
    locations.append(dict(label=row['label'], fragment=f'3.{index}', page=page + 1,
                          source=row['source'], lines=[row['firstline'], row['lastline']]))
write('pagine-frammenti.json', locations)

conservation = []
for title, following in [('Introduzione', 'Analisi dei requisiti e progettazione'),
                          ('Analisi dei requisiti e progettazione', 'Implementazione')]:
    old_start, old_stop = area(initial, title, following)
    new_start, new_stop = area(pdf, title, following)
    assert old_stop - old_start == new_stop - new_start
    for a, b in zip(range(old_start, old_stop), range(new_start, new_stop)):
        assert initial[a].get_text() == pdf[b].get_text(), ('testo diverso', title, a, b)
        assert initial[a].get_pixmap().samples == pdf[b].get_pixmap().samples, ('pixel diversi', title, a, b)
    conservation.append(dict(chapter=title, old_pages=[old_start + 1, old_stop],
                             new_pages=[new_start + 1, new_stop], text_identical=True, pixels_identical=True))
write('conservazione-pdf.json', conservation)
other_pages = []
titles = [title for level, title, _ in pdf.get_toc() if level == 1]
for title in titles[3:]:
    following = titles[titles.index(title) + 1] if title != titles[-1] else None
    if following:
        old_start, old_stop = area(initial, title, following)
        new_start, new_stop = area(pdf, title, following)
    else:
        old_start = next(p - 1 for l, t, p in initial.get_toc() if l == 1 and t == title)
        new_start = next(p - 1 for l, t, p in pdf.get_toc() if l == 1 and t == title)
        old_stop, new_stop = len(initial), len(pdf)
    assert old_stop - old_start == new_stop - new_start, title
    for a, b in zip(range(old_start, old_stop), range(new_start, new_stop)):
        old_lines, new_lines = initial[a].get_text().splitlines(), pdf[b].get_text().splitlines()
        if old_lines and old_lines[-1].isdigit():
            old_lines.pop()
        if new_lines and new_lines[-1].isdigit():
            new_lines.pop()
        assert old_lines == new_lines, ('contenuto estraneo diverso', title, a + 1, b + 1)
        other_pages.append(b + 1)
write('conservazione-altre-parti.json', {'pages_checked': len(other_pages), 'physical_pages': other_pages,
      'comparison': 'Testo pagina per pagina, rimossa soltanto l’ultima riga numerica del piè di pagina.',
      'identical': True, 'old_excerpts_preserved': 13,
      'old_excerpts_identical': 11, 'old_excerpts_completed': 2})

log = (DOC / 'build/ticket11/main.log').read_text(errors='replace')
for marker in ['undefined', 'Rerun to get', 'Overfull', 'Underfull', 'Missing character', 'Negative labelwidth']:
    assert marker not in log, marker
assert log.count('Infinite glue shrinkage') == 5
all_tex = '\n'.join(p.read_text() for folder in ['capitoli', 'appendici']
                    for p in (DOC / folder).glob('*.tex'))
refs = set(re.findall(r'\\(?:ref|pageref)\{([^}]+)\}', all_tex))
aux = (DOC / 'build/ticket11/main.aux').read_text()
resolved = set(re.findall(r'\\newlabel\{([^}]+)\}', aux))
assert refs <= resolved, refs - resolved
links = 0
fonts = set()
for i, page in enumerate(pdf):
    assert '??' not in page.get_text(), i + 1
    for link in page.get_links():
        if link['kind'] in (fitz.LINK_GOTO, fitz.LINK_NAMED):
            assert 0 <= link['page'] < len(pdf), (i, link)
            links += 1
    fonts.update(f[0] for f in page.get_fonts())
assert all(pdf.extract_font(font)[3] for font in fonts)
extract = fitz.open()
extract.insert_pdf(pdf, from_page=start, to_page=stop - 1)
extract.set_metadata({'title': 'DriveHub — Ticket 11, capitolo 3 per revisione degli autori'})
extract_path = HERE / 'capitolo-3-per-revisione.pdf'
unchanged = False
if extract_path.exists():
    with fitz.open(extract_path) as existing:
        unchanged = (len(existing) == len(extract) and existing.metadata['title'] == extract.metadata['title']
                     and all(existing[i].get_text() == extract[i].get_text()
                             and existing[i].get_pixmap().samples == extract[i].get_pixmap().samples
                             for i in range(len(extract))))
if not unchanged:
    temporary = HERE / 'capitolo-3-temporaneo.pdf'
    extract.save(temporary)
    temporary.replace(extract_path)
assert all(extract[i].get_text() == pdf[start + i].get_text() for i in range(len(extract)))

# Rendering dal PDF corrente: l'osservazione umana/assistita resta nel verbale separato.
c_start = next(p - 1 for l, t, p in pdf.get_toc() if l == 1 and t == 'Matrice delle verifiche')
inspected = sorted(set(range(1, pdf.get_toc()[0][2] - 1)) |
                   set(range(start, stop)) | set(range(c_start, len(pdf))))
(HERE / 'pagine').mkdir(exist_ok=True)
(HERE / 'provini').mkdir(exist_ok=True)
for i in inspected:
    pdf[i].get_pixmap(matrix=fitz.Matrix(1.5, 1.5)).save(HERE / 'pagine' / f'pagina-{i + 1:03}.png')
for batch in range(0, len(inspected), 4):
    sheet = Image.new('RGB', (1500, 2200), '#d0d0d0')
    draw = ImageDraw.Draw(sheet)
    for pos, index in enumerate(inspected[batch:batch + 4]):
        with Image.open(HERE / 'pagine' / f'pagina-{index + 1:03}.png') as im:
            im.thumbnail((730, 1050))
            x, y = (pos % 2) * 750 + 10, (pos // 2) * 1100 + 30
            sheet.paste(im, (x, y))
            draw.text((x, y - 20), f'PDF pagina {index + 1}', fill='black')
    sheet.save(HERE / 'provini' / f'provino-{batch // 4 + 1:02}.png')
write('pagine.json', {'pdf_pages': len(pdf), 'chapter3': [start + 1, stop],
                      'appendix_c': [c_start + 1, len(pdf)],
                      'rendered_pages': [i + 1 for i in inspected]})
write('esito-documentale.json', {'baseline_files': len(baseline['files']),
      'preserved': len(baseline['files']) - len(changed), 'changed': changed,
      'structural_entries': 35, 'original_fragments': 14, 'literal_excerpts': 27,
      'resolved_refs': len(refs), 'internal_links': links, 'embedded_fonts': len(fonts),
      'pdf_pages': len(pdf), 'chapter3': [start + 1, stop], 'extract_pages': len(extract),
      'tex_infinite_glue': 5, 'tex_overfull': 0, 'tex_underfull': 0})
print(f'PASS: 35 voci, 14 corrispondenze, 27 estratti esatti anche nel PDF; {len(pdf)} pagine.')
print(f'Capitolo 3: {start + 1}–{stop}; {len(refs)} rinvii risolti; capitoli 1 e 2 identici a testo e pixel.')
print(f'Conservati {len(baseline["files"]) - len(changed)} file iniziali; nessuna cancellazione.')
print('Cinque messaggi Infinite glue preesistenti in C; nessun Overfull/Underfull.')
