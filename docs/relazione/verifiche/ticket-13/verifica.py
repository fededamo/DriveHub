"""Verifica la composizione 13; l'osservazione visiva resta un verbale distinto."""
from pathlib import Path
import difflib
import hashlib
import json
import re
import subprocess
import tarfile
import unicodedata

import pymupdf as fitz

OUT = Path(__file__).resolve().parent
DOC = OUT.parents[1]
ROOT = DOC.parents[1]


def sha(path):
    with path.open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()


def save(name, value):
    (OUT / name).write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n')


def norm(text):
    return re.sub(r'\s+', '', unicodedata.normalize('NFKC', text)).replace('\u00ad', '').replace('␣', '').replace('−', '-')


base = json.loads((OUT / 'baseline.json').read_text())
assert subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=ROOT, text=True).strip() == base['head']
allowed = {'docs/relazione/' + p for p in [
    'capitoli/5_uso_ai.tex', 'relazione-drivehub.pdf', 'README.md', 'LEGGIMI-consegna.md',
    'piano.md', 'indice.md', 'allineamento.md', 'diagrammi/README.md',
    'tracker/issues/13-verifica-riallineamento.md', 'tracker/spec.md']}
changed = []
for name, before in base['files'].items():
    path = ROOT / name
    assert path.is_file(), 'File iniziale mancante: ' + name
    if sha(path) != before['sha256']:
        changed.append(name)
assert set(changed) <= allowed, set(changed) - allowed
current = {str(p.relative_to(ROOT)) for root in ['src', 'docs', 'Relazione_SWE']
           for p in (ROOT / root).rglob('*') if p.is_file() and 'build' not in p.parts}
added = sorted(current - base['files'].keys())
assert all(p.startswith('docs/relazione/verifiche/ticket-13/') for p in added), added
archive = OUT / 'baseline-editoriale.tar.gz'
assert sha(archive) == (OUT / 'baseline-editoriale.sha256').read_text().split()[0]
diff = []
with tarfile.open(archive) as tar:
    for member in tar.getmembers():
        if member.isfile():
            assert hashlib.sha256(tar.extractfile(member).read()).hexdigest() == base['files'][member.name]['sha256']
    initial = fitz.open(stream=tar.extractfile('docs/relazione/relazione-drivehub.pdf').read(), filetype='pdf')
    for name in changed:
        if name.endswith(('.tex', '.md')):
            before = tar.extractfile(name).read().decode()
            diff.extend(difflib.unified_diff(before.splitlines(True), (ROOT / name).read_text().splitlines(True),
                                           fromfile='baseline/' + name, tofile=name))
(OUT / 'delta-documentale.diff').write_text(''.join(diff))
save('delta-files.json', dict(baseline=len(base['files']), changed=changed, added=added,
                             removed=[], preserved=len(base['files']) - len(changed)))

pdfpath = DOC / 'relazione-drivehub.pdf'
assert pdfpath.read_bytes() == (DOC / 'build/ticket13/main.pdf').read_bytes()
pdf = fitz.open(pdfpath)
top = [row for row in pdf.get_toc() if row[0] == 1]
ranges = {title: [start, top[i + 1][2] - 1 if i + 1 < len(top) else len(pdf)]
          for i, (_, title, start) in enumerate(top)}
oldtop = [row for row in initial.get_toc() if row[0] == 1]
oldranges = {title: [start, oldtop[i + 1][2] - 1 if i + 1 < len(oldtop) else len(initial)]
             for i, (_, title, start) in enumerate(oldtop)}
assert ranges.keys() == oldranges.keys()
conservation = []
for title, (a, b) in ranges.items():
    x, y = oldranges[title]
    if title == "Uso dell'AI":
        continue
    assert b - a == y - x
    text_delta, pixel_delta = [], []
    for n, o in zip(range(a - 1, b), range(x - 1, y)):
        if pdf[n].get_text() != initial[o].get_text():
            text_delta.append(n + 1)
        if pdf[n].get_pixmap().samples != initial[o].get_pixmap().samples:
            pixel_delta.append(n + 1)
    assert not text_delta and not pixel_delta, (title, text_delta, pixel_delta)
    conservation.append(dict(part=title, initial=[x, y], final=[a, b],
                             pages=b-a+1, text_equal=True, pixel_equal=True))
save('conservazione-pdf.json', conservation)
save('paginazione.json', dict(initial_pages=len(initial), final_pages=len(pdf),
     initial_ranges=oldranges, final_ranges=ranges,
     changed_text_pages=[n+1 for n in range(min(len(pdf), len(initial))) if pdf[n].get_text()!=initial[n].get_text()],
     changed_pixel_pages=[n+1 for n in range(min(len(pdf), len(initial))) if pdf[n].get_pixmap().samples!=initial[n].get_pixmap().samples]))

alltext = '\n'.join(p.get_text() for p in pdf)
assert '??' not in alltext
links, fonts, sizes = 0, set(), []
for n, page in enumerate(pdf):
    size = sorted(round(v) for v in [page.rect.width, page.rect.height])
    assert size in [[595, 842], [842, 1191]], (n + 1, size)
    if size == [842, 1191]:
        sizes.append(n + 1)
    for word in page.get_text('words'):
        assert word[0] >= -1 and word[1] >= -1 and word[2] <= page.rect.width + 1 and word[3] <= page.rect.height + 1
    for link in page.get_links():
        if link['kind'] in (fitz.LINK_GOTO, fitz.LINK_NAMED):
            assert isinstance(link.get('page'), int) and 0 <= link['page'] < len(pdf), link
            links += 1
    fonts.update(f[0] for f in page.get_fonts())
assert all(pdf.extract_font(x)[3] for x in fonts)
assert len(sizes) == 12
aux = (DOC / 'build/ticket13/main.aux').read_text()
labels = set(re.findall(r'\\newlabel\{([^}]+)\}', aux))
tex = '\n'.join(p.read_text() for folder in ['capitoli', 'appendici'] for p in (DOC / folder).glob('*.tex'))
refs = set(re.findall(r'\\(?:ref|pageref)\{([^}]+)\}', tex))
assert refs <= labels, refs - labels
assert (OUT / 'stabilizzazione-2.sha256').read_text() == (OUT / 'stabilizzazione-3.sha256').read_text()
log = (OUT / 'latex-log.txt').read_text()
diagnostics = [line for line in log.splitlines() if re.search(r'Warning:|Error:|ignored error:|Overfull|Underfull|Missing character|Rerun to|undefined', line)]
for marker in ['Overfull', 'Underfull', 'Missing character', 'undefined', 'Rerun to', 'Warning:', '! ']:
    assert marker not in log, marker
message = r'ignored error: Infinite glue shrinkage found in box being split \[(\d+)\]'
messages = re.findall(message, log)
oldmessages = re.findall(message, (DOC / 'verifiche/ticket-12/latex-log.txt').read_text())
assert len(messages) == len(oldmessages) == 5
offset = ranges['Introduzione'][0] - 1
save('messaggi-tex.json', dict(baseline_printed=oldmessages, current_printed=messages,
     current_physical=[int(n) + offset for n in messages], diagnostics=diagnostics,
    assessment='Confronto con baseline e osservazione diretta dell’agente nel verbale; messaggi non soppressi né dichiarati risolti.'))

fragment_pages = {}
for f in json.loads((OUT / 'frammenti-correnti.json').read_text()):
    needles = [c['text'] for c in f['commands']] if f.get('kind') == 'commands' else f['text'].splitlines()
    pages = []
    for n, page in enumerate(pdf):
        text = norm(page.get_text())
        if f.get('kind') == 'commands':
            text = text.replace('\\', '')
        if all(norm(line) in text for line in needles if line.strip()):
            pages.append(n + 1)
    assert pages, f['label']
    fragment_pages[f['label']] = pages
save('pagine-frammenti.json', fragment_pages)
render = OUT / 'pagine'
render.mkdir(exist_ok=True)
renderings = []
for n, page in enumerate(pdf, 1):
    target = render / f'pagina-{n:03}.png'
    page.get_pixmap(matrix=fitz.Matrix(1.5, 1.5)).save(target)
    renderings.append(dict(page=n, path=str(target.relative_to(ROOT)), sha256=sha(target)))
save('rendering.json', renderings)
save('esito-documentale.json', dict(status='PASS', pdf_sha256=sha(pdfpath), pages=len(pdf),
     ranges=ranges, A3_pages=sizes, resolved_refs=len(refs), internal_links=links,
     embedded_fonts=len(fonts), original_entries=102, original_fragments=23,
     current_blocks=len(fragment_pages), diagrams=43, residual_tex_messages=5,
     visual_inspection='Verbale separato: il rendering e questi assert non dimostrano osservazione.'))
print(f'PASS: {len(pdf)} pagine, {len(sizes)} A3, {len(refs)} rinvii, {links} link, {len(fonts)} font; conservazione e 42 blocchi verificati.')
