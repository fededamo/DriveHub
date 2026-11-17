"""Controlli documentali del ticket 05; nessun avvio Java, test applicativo o DB."""
from hashlib import sha256
from pathlib import Path
import json
import re
import unicodedata
import pymupdf

HERE = Path(__file__).resolve().parent
DOC = HERE.parents[1]
REPO = DOC.parents[1]
VIS = DOC / 'build/visual-ticket05'
VIS.mkdir(parents=True, exist_ok=True)


def normalizza(text):
    return ' '.join(unicodedata.normalize('NFKC', text).split()).replace('’', "'")


def compatto(text):
    return re.sub(r'\s+', '', normalizza(text)).replace('\u00ad', '')


def intervallo(pdf, title, following):
    toc = pdf.get_toc()
    return (next(p - 1 for _, t, p in toc if t == title),
            next(p - 1 for _, t, p in toc if t == following))


def testo(pdf, start, stop):
    result = []
    for page in list(pdf)[start:stop]:
        lines = page.get_text().splitlines()
        if lines and lines[-1].strip().isdigit():
            lines.pop()
        result.extend(lines)
    # Il caricamento di listings cambia una sillabazione: approvazione/rifiuto.
    return normalizza(re.sub(r'(?<=[\w/])-\n(?=\w)', '', '\n'.join(result)))


baseline = json.loads((HERE / 'baseline-conservazione.json').read_text())
for name, digest in baseline.items():
    assert sha256((REPO / name).read_bytes()).hexdigest() == digest, name
sources = json.loads((HERE / 'fonti.json').read_text())
for row in sources:
    assert sha256((REPO / row['source']).read_bytes()).hexdigest() == row['sha256'], row

chapter = (DOC / 'capitoli/3_implementazione.tex').read_text()
fragments = json.loads((HERE / 'frammenti.json').read_text())
blocks = re.findall(r'\\begin\{lstlisting\}\[(.*?)\]\n(.*?)\\end\{lstlisting\}', chapter, re.S)
assert len(blocks) == len(fragments) == 13
for (options, code), row in zip(blocks, fragments):
    current = ''.join((REPO / row['source']).read_text().splitlines(keepends=True)[row['firstline']-1:row['lastline']])
    assert code == row['text'] == current, row['label']
    assert sha256(code.encode()).hexdigest() == row['sha256']
    assert 'firstnumber=' + str(row['firstline']) + ',' in options
    assert 'label=' + row['label'] in options
    assert 3 <= row['lastline'] - row['firstline'] + 1 <= 19

pdfpath = DOC / 'relazione-drivehub.pdf'
assert pdfpath.read_bytes() == (DOC / 'build/main.pdf').read_bytes()
pdf = pymupdf.open(pdfpath)
assert not pdf.is_encrypted and pdf.metadata['format'] == 'PDF 1.7'
toc = pdf.get_toc()
index = (DOC / 'indice.md').read_text()
chapters = re.findall(r'^## [1-5]\. (.+)$', index, re.M)
appendices = re.findall(r'^- \*\*[ABC] — (.+?)\.\*\*', index, re.M)
sections = re.findall(r'^- \*\*\d+\.\d+ (.+?)\.\*\*', index, re.M)
assert len(chapters) == 5 and len(sections) == 25 and len(appendices) == 3
assert [normalizza(t) for l,t,_ in toc if l == 1] == list(map(normalizza, chapters + appendices))
assert [normalizza(t) for l,t,_ in toc if l == 2][:25] == list(map(normalizza, sections))
area = intervallo(pdf, 'Implementazione', 'Testing')
assert 'Provvisorio' not in testo(pdf, *area)
for title in chapters[:3] + appendices[:2]:
    page = next(p for _,t,p in toc if t == title)
    assert 'Provvisorio' not in pdf[page-1].get_text(), title
for title in chapters[3:] + appendices[2:]:
    page = next(p for _,t,p in toc if t == title)
    assert 'Provvisorio' in pdf[page-1].get_text(), title
assert 'Capitoli 1–3' in normalizza(pdf[0].get_text())
assert 'per revisione' in normalizza(pdf[0].get_text())

# Il contenuto dei capitoli/appendici già completati resta identico nel PDF.
old = pymupdf.open(HERE / 'prima/relazione-drivehub.pdf')
for title, following in [(chapters[0], chapters[1]), (chapters[1], chapters[2]),
                         (appendices[0], appendices[1]), (appendices[1], appendices[2])]:
    assert testo(pdf, *intervallo(pdf, title, following)) == testo(old, *intervallo(old, title, following)), title

links, fonts = 0, set()
for i, page in enumerate(pdf):
    assert abs(page.rect.width - 595.28) < 1 and abs(page.rect.height - 841.89) < 1
    assert '??' not in page.get_text(), i + 1
    for link in page.get_links():
        if link['kind'] in (pymupdf.LINK_GOTO, pymupdf.LINK_NAMED):
            assert 0 <= link['page'] < len(pdf), link
            if link['kind'] == pymupdf.LINK_NAMED:
                assert pdf.resolve_link('#' + link['nameddest'])[0] == link['page']
            links += 1
    fonts.update(f[0] for f in page.get_fonts())
    for block in page.get_text('dict')['blocks']:
        for line in block.get('lines', []):
            for span in line['spans']:
                assert (page.rect + (-1,-1,1,1)).contains(pymupdf.Rect(span['bbox'])), (i+1, span)
    page.get_pixmap(matrix=pymupdf.Matrix(1.4,1.4)).save(VIS / f'pagina-{i+1:02}.png')
assert all(pdf.extract_font(f)[3] for f in fonts)

# Confronto lessicale dei frammenti effettivamente estratti dal PDF, oltre al LaTeX.
locations = []
for n, row in enumerate(fragments, 1):
    marker = f'Frammento 3.{n}:'
    found = [(i, pdf[i].get_text()) for i in range(*area) if marker in pdf[i].get_text()]
    assert len(found) == 1, marker
    i, text = found[0]
    before = text.split(marker)[0]
    starts = list(re.finditer(r'(?m)^' + str(row['firstline']) + r'\s*$', before))
    assert starts, (marker, 'inizio non nella pagina della didascalia')
    code = re.sub(r'(?m)^\d+\s*$', '', before[starts[-1].start():])
    assert compatto(code) == compatto(row['text']), (marker, code)
    locations.append({'fragment': f'3.{n}', 'page': i+1, 'source': row['source'],
                      'lines': [row['firstline'], row['lastline']]})
(HERE / 'pagine-frammenti.json').write_text(json.dumps(locations, indent=2) + '\n')

log = (DOC / 'build/main.log').read_text()
assert not re.search(r'^!|Warning:|Overfull|Underfull|Missing character', log, re.M)
warnings = re.findall(r'^warning: (.+)$', (HERE / 'compilazione.txt').read_text(), re.M)
expected = 'Trying to include PDF file with version (1.7), which is newer than current output PDF setting (1.5).'
assert all(w == expected for w in warnings), warnings
extract = pymupdf.open()
extract.insert_pdf(pdf, from_page=area[0], to_page=area[1]-1)
extract.set_metadata({'title': 'DriveHub — Capitolo 3 per revisione degli autori'})
extract.save(HERE / 'capitolo-3-per-revisione.pdf')
print(f'PDF: {len(pdf)} pagine A4, {links} link interni risolti, {len(fonts)} font incorporati.')
print(f'Capitolo 3: pagine fisiche {area[0]+1}–{area[1]}; cinque sezioni e indice in ordine.')
print('13 frammenti: identità esatta con sorgenti e snapshot; testo PDF equivalente senza whitespace/legature; ogni frammento e didascalia su una pagina.')
print(f'Conservazione: {len(baseline)} file intatti; {len(sources)} fonti improntate; capitoli 1–2 e A–B equivalenti anche nel testo PDF, esclusi piè di pagina e sillabazione a fine riga.')
print('Capitoli 1–3/A–B proposti per revisione; 4–5/C provvisori; nessun riferimento irrisolto, carattere mancante o box difettoso nel log finale.')
print(f'{len(warnings)} avvisi noti di versione PDF nelle figure; output effettivo PDF 1.7.')
print('Tutte le pagine renderizzate. Il controllo visivo è un verbale distinto, non dedotto dagli assert.')
print('SHA-256 PDF:', sha256(pdfpath.read_bytes()).hexdigest())
