"""Verifiche documentali 06: nessun avvio Java, test applicativo, harness o DB."""
from hashlib import sha256
from pathlib import Path
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
VIS = DOC / 'build/visual-ticket06'
VIS.mkdir(parents=True, exist_ok=True)


def normalizza(text):
    return ' '.join(unicodedata.normalize('NFKC', text).split()).replace('’', "'")


def compatto(text):
    return re.sub(r'\s+', '', normalizza(text)).replace('\u00ad', '')


def intervallo(pdf, title, following=None):
    toc = pdf.get_toc()
    return (next(p-1 for _, t, p in toc if t == title),
            next(p-1 for _, t, p in toc if t == following) if following else len(pdf))


def testo(pdf, start, stop):
    lines = []
    for page in list(pdf)[start:stop]:
        page_lines = page.get_text().splitlines()
        if page_lines and page_lines[-1].strip().isdigit():
            page_lines.pop()
        lines.extend(page_lines)
    return normalizza(re.sub(r'(?<=[\w/])-\n(?=\w)', '', '\n'.join(lines)))


# Una sola eccezione amministrativa nella baseline, con confronto del testo intero.
status_name = 'docs/relazione/capitoli/3_implementazione.tex'
status_old = (HERE/'prima/capitoli/3_implementazione.tex').read_text()
status_new = status_old.replace('Capitoli~4--5 e appendice~C restano provvisori.',
                                'Il capitolo~5 resta provvisorio.')
assert status_new != status_old
baseline = json.loads((HERE/'baseline-conservazione.json').read_text())
for name, digest in baseline.items():
    if name == status_name:
        assert sha256((HERE/'prima/capitoli/3_implementazione.tex').read_bytes()).hexdigest() == digest
        assert (REPO/name).read_text() == status_new
    else:
        assert sha256((REPO/name).read_bytes()).hexdigest() == digest, name
sources = json.loads((HERE/'fonti.json').read_text())
for row in sources:
    assert sha256((REPO/row['source']).read_bytes()).hexdigest() == row['sha256'], row

# Metodo citato esistente: controllo di riferimento, non esecuzione o prova semantica.
for name in ['capitoli/4_testing.tex', 'appendici/c_verifiche.tex']:
    for cls, method in re.findall(r'\\code\{([A-Z]\w+)\.([a-z]\w+)\}', (DOC/name).read_text()):
        if method in ('java', 'sql', 'log') or (cls, method) == ('Future', 'get'):
            continue
        paths = list((REPO/'src').rglob(cls+'.java')) + list((REPO/'docs/evidence').rglob(cls+'.java'))
        assert paths and any(re.search(r'\b'+method+r'\s*\(', p.read_text()) for p in paths), (cls, method)

fragments = json.loads((HERE/'frammenti.json').read_text())
chapter = (DOC/'capitoli/4_testing.tex').read_text()
blocks = re.findall(r'\\begin\{lstlisting\}\[(.*?)\]\n(.*?)\\end\{lstlisting\}', chapter, re.S)
java_blocks = [(options, code) for options, code in blocks if 'firstnumber=' in options]
assert len(java_blocks) == len(fragments) == 3
for (options, code), row in zip(java_blocks, fragments):
    current = ''.join((REPO/row['source']).read_text().splitlines(keepends=True)[row['firstline']-1:row['lastline']])
    assert code == row['text'] == current
    assert sha256(code.encode()).hexdigest() == row['sha256']
    assert 'firstnumber='+str(row['firstline']) in options and 'label='+row['label'] in options

# E1: conteggi storici, senza invocare Maven. E2: XML originali, non target locale.
history = json.loads((HERE/'riscontro-storico.json').read_text())
rows = re.findall(r'Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)[^\n]*-- in it\.unifi\.drivehub\.(\w+)',
                  (REPO/history['enum_full_log']).read_text())
assert len(rows) == 23 and [sum(int(row[i]) for row in rows) for i in range(4)] == [423, 0, 0, 0]
xmls = list((REPO/'docs/evidence/r11-integrated/full-surefire').glob('TEST-*.xml'))
assert [sum(int(ET.parse(p).getroot().get(k)) for p in xmls) for k in ['tests','failures','errors','skipped']] == [394,0,0,0]
old_inputs = json.loads((REPO/history['r11_manifest']).read_text())
changed = [name for name, h in old_inputs.items() if not (REPO/name).exists() or sha256((REPO/name).read_bytes()).hexdigest() != h]
assert changed == history['r11_different_inputs'] and len(changed) == 28
assert not subprocess.check_output(['git','diff','--name-only','d4d82c1815e1fe2d107331fb4f2211b2bc3ca850','HEAD','--','src','pom.xml'], cwd=REPO)
for name in ['Agreement','SaleOutcome','Destination','Identity','TableLifecycle','SlowModels','AppStartup']:
    inputs = json.loads((REPO/f'docs/evidence/r11-integrated/{name}UiCheck-inputs.json').read_text())
    for source, hashes in inputs.items():
        assert sha256((REPO/source).read_bytes()).hexdigest() == hashes['original_sha256'], source
    log = (REPO/f'docs/evidence/r11-integrated/{name}UiCheck.log').read_text()
    assert ('APP STARTUP SUCCESS' if name == 'AppStartup' else 'UI CHECK SUCCESS') in log

pdfpath = DOC/'relazione-drivehub.pdf'
assert pdfpath.read_bytes() == (DOC/'build/main.pdf').read_bytes()
pdf = pymupdf.open(pdfpath)
old = pymupdf.open(HERE/'prima/relazione-drivehub.pdf')
chapters = re.findall(r'^## [1-5]\. (.+)$', (DOC/'indice.md').read_text(), re.M)
appendices = re.findall(r'^- \*\*[ABC] — (.+?)\.\*\*', (DOC/'indice.md').read_text(), re.M)
sections = re.findall(r'^- \*\*\d+\.\d+ (.+?)\.\*\*', (DOC/'indice.md').read_text(), re.M)
toc = pdf.get_toc()
assert [normalizza(t) for l,t,_ in toc if l==1] == list(map(normalizza,chapters+appendices))
assert [normalizza(t) for l,t,_ in toc if l==2][:25] == list(map(normalizza,sections))
area = intervallo(pdf, 'Testing', "Uso dell'AI")
appendix = intervallo(pdf, 'Matrice delle verifiche')
assert 'Provvisorio' not in testo(pdf,*area)+testo(pdf,*appendix)
assert 'Provvisorio' in testo(pdf,*intervallo(pdf,"Uso dell'AI",appendices[0]))
assert 'Capitoli 1–4' in normalizza(pdf[0].get_text()) and 'appendici A–C' in normalizza(pdf[0].get_text())
for title, following in [(chapters[0],chapters[1]),(chapters[1],chapters[2]),(chapters[2],chapters[3]),(appendices[0],appendices[1]),(appendices[1],appendices[2])]:
    before = testo(old,*intervallo(old,title,following))
    if title == chapters[2]:
        before = before.replace('Capitoli 4–5 e appendice C restano provvisori.', 'Il capitolo 5 resta provvisorio.')
    assert testo(pdf,*intervallo(pdf,title,following)) == before, title

links, fonts = 0, set()
for i,page in enumerate(pdf):
    assert abs(page.rect.width-595.28)<1 and abs(page.rect.height-841.89)<1
    assert '??' not in page.get_text()
    for link in page.get_links():
        if link['kind'] in (pymupdf.LINK_GOTO,pymupdf.LINK_NAMED):
            assert 0 <= link['page'] < len(pdf)
            if link['kind'] == pymupdf.LINK_NAMED:
                assert pdf.resolve_link('#'+link['nameddest'])[0] == link['page']
            links += 1
    fonts.update(f[0] for f in page.get_fonts())
    for block in page.get_text('dict')['blocks']:
        for line in block.get('lines',[]):
            for span in line['spans']:
                assert (page.rect+(-1,-1,1,1)).contains(pymupdf.Rect(span['bbox'])), (i+1,span)
assert all(pdf.extract_font(f)[3] for f in fonts)
locations=[]
for n,row in enumerate(fragments,1):
    marker=f'Frammento 4.{n}:'
    found=[(i,pdf[i].get_text()) for i in range(*area) if marker in pdf[i].get_text()]
    assert len(found)==1, marker
    i,content=found[0]; before=content.split(marker)[0]
    starts=list(re.finditer(r'(?m)^'+str(row['firstline'])+r'\s*$',before)); assert starts
    code=re.sub(r'(?m)^\d+\s*$','',before[starts[-1].start():])
    assert compatto(code)==compatto(row['text']), (marker,code)
    locations.append({'label':row['label'],'page':i+1})
(HERE/'pagine-frammenti.json').write_text(json.dumps(locations,indent=2)+'\n')
log=(DOC/'build/main.log').read_text()
assert not re.search(r'^!|Warning:|Overfull|Underfull|Missing character',log,re.M)
warnings=re.findall(r'^warning: (.+)$',(HERE/'compilazione.txt').read_text(),re.M)
expected='Trying to include PDF file with version (1.7), which is newer than current output PDF setting (1.5).'
assert all(w==expected for w in warnings),warnings
extract=pymupdf.open()
for start,stop in [area,appendix]:
    extract.insert_pdf(pdf,from_page=start,to_page=stop-1)
extract.set_metadata({'title':'DriveHub — Capitolo 4 e appendice C per revisione degli autori'})
extract.save(HERE/'ticket-06-per-revisione.pdf')
visual_pages=sorted(set([0,1,2]+list(range(*area))+list(range(*appendix))+[area[0]-1,area[1],appendix[0]-1]))
for i in visual_pages:
    pdf[i].get_pixmap(matrix=pymupdf.Matrix(1.5,1.5)).save(VIS/f'pagina-{i+1:03}.png')
result={'pdf_sha256':sha256(pdfpath.read_bytes()).hexdigest(),'pages':len(pdf),'chapter_4_physical':[area[0]+1,area[1]],'appendix_C_physical':[appendix[0]+1,appendix[1]],'rendered_pages':[i+1 for i in visual_pages],'internal_links':links,'embedded_fonts':len(fonts),'known_pdf_version_warnings':len(warnings),'baseline_unchanged':len(baseline)-1,'baseline_status_only':1}
(HERE/'esito-documentale.json').write_text(json.dumps(result,indent=2)+'\n')
# Il delta reale include anche sorgenti gia' non tracciati.
changes=[]
for p in sorted((HERE/'prima').rglob('*.tex'))+[(HERE/'prima/README.md')]:
    rel=p.relative_to(HERE/'prima'); changes.extend(difflib.unified_diff(p.read_text().splitlines(keepends=True),(DOC/rel).read_text().splitlines(keepends=True),fromfile='prima/'+str(rel),tofile=str(rel)))
for rel in ['appendici/c_verifiche.tex']:
    changes.extend(difflib.unified_diff([], (DOC/rel).read_text().splitlines(keepends=True),fromfile='/dev/null',tofile=rel))
(HERE/'delta-documentale.diff').write_text(''.join(changes))
print(json.dumps(result,indent=2))
print(f'{len(sources)} fonti e tre frammenti esatti; conservazione dei contenuti 1–3/A–B nel PDF salvo sola frase di stato.')
print('E1: 423/0/0/0 nel log; E2: 394/0/0/0 negli XML; E3: sette marker storici e impronte degli originali verificati. Nessuna nuova esecuzione applicativa.')
print('Indice e riferimenti risolti, font incorporati, nessun box difettoso/carattere mancante nel log; estratto 4/C creato.')
print('Il rendering non sostituisce il verbale di osservazione visiva. Nessuna approvazione degli autori attribuita.')
