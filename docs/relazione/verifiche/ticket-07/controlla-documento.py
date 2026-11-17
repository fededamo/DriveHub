"""Controlli documentali 07; non avvia software, test applicativi o database."""
from hashlib import sha256
from pathlib import Path
from zipfile import ZipFile, ZipInfo, ZIP_DEFLATED
import difflib
import json
import re
import subprocess
import unicodedata
import xml.etree.ElementTree as ET

import pymupdf

HERE = Path(__file__).resolve().parent
DOC = HERE.parents[1]
REPO = DOC.parents[1]
VIS = DOC / 'build/visual-ticket07'
VIS.mkdir(parents=True, exist_ok=True)


def digest(path):
    return sha256(path.read_bytes()).hexdigest()


def normalizza(text):
    text = unicodedata.normalize('NFKC', text).replace('’', "'")
    text = re.sub(r'(?<=[\w/])-\n(?=\w)', '', text)
    return re.sub(r'\s+', '', text).replace('\u00ad', '')


def area(pdf, title):
    chapters = [(t, p - 1) for level, t, p in pdf.get_toc() if level == 1]
    i = next(i for i, (t, _) in enumerate(chapters) if t == title)
    return chapters[i][1], chapters[i + 1][1] if i + 1 < len(chapters) else len(pdf)


def testo(pdf, title):
    start, stop = area(pdf, title)
    lines = []
    for page in list(pdf)[start:stop]:
        content = page.get_text().splitlines()
        if content and content[-1].strip().isdigit():
            content.pop()
        lines.extend(content)
    return normalizza('\n'.join(lines))


baseline = json.loads((HERE / 'baseline-conservazione.json').read_text())
assert subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=REPO).decode().strip() == baseline['head']
allowed = {'docs/relazione/' + name for name in [
    'main.tex', 'README.md', 'piano.md', 'capitoli/3_implementazione.tex',
    'capitoli/4_testing.tex', 'capitoli/5_uso_ai.tex', 'relazione-drivehub.pdf']}
changed = []
for name, old_hash in baseline['files'].items():
    path = REPO / name
    assert path.is_file(), ('eliminato', name)
    if digest(path) != old_hash:
        assert name in allowed, ('modifica fuori perimetro', name)
        changed.append(name)
assert set(changed) == allowed
for name in ['capitoli/3_implementazione.tex', 'capitoli/4_testing.tex']:
    old = (HERE / 'prima' / name).read_text()
    assert (DOC / name).read_text() == old.replace(' Il capitolo~5 resta provvisorio.', '')

for row in json.loads((HERE / 'fonti.json').read_text()):
    assert digest(REPO / row['source']) == row['sha256'], row['source']
with ZipFile(REPO / 'docs/evidence/revision-records.zip') as z:
    manifest = json.loads(z.read('SHA256.json'))
    assert len(manifest) == 24
    assert all(sha256(z.read(name)).hexdigest() == h for name, h in manifest.items())
    assert len([n for n in z.namelist() if '/issues/' in n]) == 18
    for row in json.loads((HERE / 'registri-zip.json').read_text())['selected']:
        assert sha256(z.read(row['member'])).hexdigest() == row['sha256']
        lines = z.read(row['member']).decode().splitlines()
        for excerpt in row['excerpts']:
            assert excerpt['text'] == '\n'.join(lines[excerpt['firstline'] - 1:excerpt['lastline']])

# Lettura delle registrazioni storiche, senza eseguire le prove descritte.
log = (REPO / 'docs/evidence/postgres-enums-2026-11-09/full-suite.txt').read_text()
counts = re.findall(r'Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)[^\n]*-- in it\.unifi\.drivehub\.', log)
assert len(counts) == 23 and [sum(int(row[i]) for row in counts) for i in range(4)] == [423, 0, 0, 0]
xmls = list((REPO / 'docs/evidence/r11-integrated/full-surefire').glob('TEST-*.xml'))
assert [sum(int(ET.parse(p).getroot().get(k)) for p in xmls) for k in ['tests', 'failures', 'errors', 'skipped']] == [394, 0, 0, 0]
assert not subprocess.check_output(['git', 'diff', 'd4d82c1815e1fe2d107331fb4f2211b2bc3ca850', 'HEAD', '--', 'src', 'pom.xml'], cwd=REPO)

pdfpath = DOC / 'relazione-drivehub.pdf'
assert pdfpath.read_bytes() == (DOC / 'build/main.pdf').read_bytes()
pdf = pymupdf.open(pdfpath)
oldpdf = pymupdf.open(HERE / 'prima/relazione-drivehub.pdf')
chapters = ['Introduzione', 'Analisi dei requisiti e progettazione', 'Implementazione',
            'Testing', "Uso dell'AI", "Schede complete dei casi d'uso",
            'Tavole delle classi', 'Matrice delle verifiche']
assert [t for level, t, p in pdf.get_toc() if level == 1] == chapters
sections = ['Progettazione e sviluppo iniziale manuali', 'Revisione assistita da Codex',
            'Verifica e controllo umano', 'Documentazione, diagrammi e limiti']
start, stop = area(pdf, "Uso dell'AI")
assert [t for level, t, p in pdf.get_toc() if level == 2 and start < p <= stop] == sections
chapter = (DOC / 'capitoli/5_uso_ai.tex').read_text()
assert re.findall(r'\\section\{([^}]+)\}', chapter) == sections
assert '\\provvisorio' not in chapter
for path in re.findall(r'\\code\{(docs/[^}]+)\}', chapter):
    assert (REPO / path).exists(), path
for title in chapters:
    if title == "Uso dell'AI":
        continue
    before = testo(oldpdf, title)
    if title in ['Implementazione', 'Testing']:
        before = before.replace(normalizza('Il capitolo 5 resta provvisorio.'), '')
    assert before == testo(pdf, title), ('testo pregresso PDF diverso', title)
assert normalizza('Proposta agli autori per revisione, non ancora approvata') in normalizza(pdf[0].get_text())
assert all(normalizza(t) in normalizza(''.join(pdf[i].get_text() for i in [1, 2])) for t in sections)

links, fonts = 0, set()
for page in pdf:
    assert abs(page.rect.width - 595.28) < 1 and abs(page.rect.height - 841.89) < 1
    assert '??' not in page.get_text()
    for link in page.get_links():
        if link['kind'] in (pymupdf.LINK_GOTO, pymupdf.LINK_NAMED):
            assert 0 <= link['page'] < len(pdf)
            if link['kind'] == pymupdf.LINK_NAMED:
                assert pdf.resolve_link('#' + link['nameddest'])[0] == link['page']
            links += 1
    fonts.update(f[0] for f in page.get_fonts())
    for block in page.get_text('dict')['blocks']:
        for line in block.get('lines', []):
            for span in line['spans']:
                x0, y0, x1, y1 = span['bbox']
                assert x0 >= 0 and y0 >= 0 and x1 <= page.rect.width + 1 and y1 <= page.rect.height + 1
assert all(pdf.extract_font(f)[3] for f in fonts)
assert links >= 144
texlog = (HERE / 'latex-log.txt').read_text()
assert not re.search(r'Overfull|Underfull|Missing character|undefined|^!', texlog, re.M)

extract = pymupdf.open()
extract.insert_pdf(pdf, from_page=start, to_page=stop - 1)
extract.save(HERE / 'capitolo-5-per-revisione.pdf', garbage=4, deflate=True)
visual = sorted({0, 1, 2, 53, 63, start - 1, stop, len(pdf) - 1, *range(start, stop)})
for i in visual:
    pdf[i].get_pixmap(dpi=110).save(VIS / f'pagina-{i + 1:03}.png')

# Archivio autonomo dei sorgenti e delle figure, senza dipendenze editoriali da /tmp.
sources = [DOC / 'main.tex', *sorted((DOC / 'capitoli').glob('*.tex')),
           *sorted((DOC / 'appendici').glob('*.tex')),
           *sorted((DOC / 'diagrammi').glob('*.drawio')),
           *sorted((DOC / 'diagrammi').glob('*.pdf')),
           *sorted((DOC / 'screenshot').glob('*.png'))]
with ZipFile(HERE / 'sorgenti-relazione.zip', 'w', compression=ZIP_DEFLATED) as z:
    for p in sources:
        info = ZipInfo(str(p.relative_to(DOC)), date_time=(2026, 11, 13, 0, 0, 0))
        info.compress_type = ZIP_DEFLATED
        z.writestr(info, p.read_bytes())
    z.writestr('COMPILAZIONE.txt', 'Estrarre in una directory. Con Tectonic 0.17.0:\ntectonic --untrusted --keep-intermediates --keep-logs main.tex\nLa prima compilazione richiede rete per i pacchetti LaTeX. Nessun input editoriale da /tmp.\nCapitoli 1-5 e appendici A-C proposti agli autori, non approvati.\nLe verifiche e le fonti storiche sono nel repository docs/relazione/verifiche/.\n')
with ZipFile(HERE / 'sorgenti-relazione.zip') as z:
    assert all(z.read(str(p.relative_to(DOC))) == p.read_bytes() for p in sources)

result = {'pdf_sha256': digest(pdfpath), 'pages': len(pdf),
          'chapter_5_physical': [start + 1, stop], 'internal_links': links,
          'embedded_fonts': len(fonts), 'rendered_pages': [i + 1 for i in visual],
          'known_pdf_version_warnings': len(re.findall(r'^warning:.*version \(1\.7\).*\(1\.5\)', (HERE / 'compilazione.txt').read_text(), re.M)),
          'baseline_files': len(baseline['files']), 'baseline_unchanged': len(baseline['files']) - len(changed),
          'modified_baseline_files': sorted(changed), 'previous_pdf_sections_equivalent': 7,
          'historical_counts_only': {'R11': [394, 0, 0, 0], 'enum': [423, 0, 0, 0]},
          'source_archive_files': len(sources) + 1}
(HERE / 'esito-documentale.json').write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n')
(HERE / 'conservazione.txt').write_text(f"Baseline {len(baseline['files'])}: {result['baseline_unchanged']} intatti, 7 modifiche documentali autorizzate, zero eliminazioni.\nSoftware/test/SQL/dati, vecchia relazione, evidenze e manifest 01–06 identici.\nCapitoli 3–4: solo rimozione delle due frasi di stato inesatte. Sette parti PDF pregresse equivalenti.\n")
diff = ''
for name in sorted(changed):
    short = Path(name).relative_to('docs/relazione')
    before = HERE / 'prima' / short
    if before.suffix == '.pdf':
        diff += f'Binary files prima/{short} and {name} differ\n'
    else:
        assert digest(before) == baseline['files'][name]
        diff += ''.join(difflib.unified_diff(before.read_text().splitlines(keepends=True), (REPO / name).read_text().splitlines(keepends=True), fromfile='prima/' + str(short), tofile=name))
(HERE / 'delta-documentale.diff').write_text(diff)
print(json.dumps(result, ensure_ascii=False, indent=2))
print('Exit 0: controlli documentali; i rendering richiedono osservazione separata. Nessuna approvazione umana attribuita.')
