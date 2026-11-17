"""Controlli documentali del ticket 12. Nessun processo applicativo o connessione DB."""
from pathlib import Path
import difflib
import hashlib
import json
import re
import subprocess
import tarfile
import unicodedata
import xml.etree.ElementTree as ET
import zipfile
from decimal import Decimal, ROUND_HALF_UP
import math
import pymupdf as fitz

OUT = Path(__file__).resolve().parent
ROOT = OUT.parents[3]
DOC = ROOT / 'docs/relazione'
def sha(p):
    return hashlib.sha256(p.read_bytes()).hexdigest()
def save(name, data):
    (OUT / name).write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n')
def norm(s):
    return re.sub(r'\s+', '', unicodedata.normalize('NFKC', s)).replace('\u00ad','').replace('␣','').replace('−','-')
def titles(s):
    return re.findall(r'\\(chapter|section|subsection)\{([^}]+)\}', s)

base = json.loads((OUT/'baseline.json').read_text())
assert subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT,text=True).strip() == base['head']
allowed = {'docs/relazione/'+x for x in ['capitoli/4_testing.tex','appendici/c_verifiche.tex','relazione-drivehub.pdf', 'README.md','piano.md','indice.md','allineamento.md','tracker/issues/12-riallineamento-testing.md']}
changed=[]
for name, old in base['files'].items():
    p=ROOT/name
    assert p.is_file(), 'Rimosso: '+name
    if sha(p)!=old['sha256']:
        changed.append(name)
assert set(changed)<=allowed, 'Modifiche fuori perimetro: '+str(set(changed)-allowed)
current={str(p.relative_to(ROOT)) for root in ['docs','src','Relazione_SWE']
         for p in (ROOT/root).rglob('*') if p.is_file() and 'build' not in p.parts}
added=sorted(current-set(base['files']))
assert all(p.startswith('docs/relazione/verifiche/ticket-12/') for p in added), added
with tarfile.open(OUT/'baseline-editoriale.tar.gz') as tar:
    before=tar.extractfile('docs/relazione/capitoli/4_testing.tex').read().decode()
    oldpdf=fitz.open(stream=tar.extractfile('docs/relazione/relazione-drivehub.pdf').read(),filetype='pdf')
    diff=[]
    for name in changed:
        try: old=tar.extractfile(name).read().decode()
        except (KeyError,UnicodeDecodeError): continue
        diff.extend(difflib.unified_diff(old.splitlines(True),(ROOT/name).read_text().splitlines(True),fromfile='baseline/'+name,tofile=name))
(OUT/'delta-documentale.diff').write_text(''.join(diff))
save('delta-files.json', {'baseline_count':len(base['files']),'changed':changed,'added':added,'unchanged':len(base['files'])-len(changed),'removed':[],'note':'Baseline include non tracciati; build rigenerabili esclusi. Storici ed originali inclusi.'})
chapter=(DOC/'capitoli/4_testing.tex').read_text()
expected=[('chapter','Testing'),('section','Strategia di verifica'),('subsection','Tecnologie e strumenti'),('section','Livello 1: Business Logic'),('subsection','Invarianti del dominio'),('subsection','Autenticazione e identità'),('subsection','Prezzi e sicurezza'),('subsection','Richieste, vendite e accordo economico'),('subsection','Noleggio e conto dimostrativo'),('subsection','Transazioni e Observer'),('section','Livello 2: Persistence Layer (DAO)'),('subsection','DAO e mapping su PostgreSQL'),('subsection','Schema e dati dimostrativi'),('section','Verifiche funzionali e della presentazione'),('section','Esecuzione della suite e portata dei risultati')]
assert titles(chapter)==expected
original=(ROOT/'Relazione_SWE/capitoli/4_testing.tex').read_text()
assert [k for k,t in titles(original)]==[k for k,t in expected]
coverage=json.loads((OUT/'copertura-frammenti.json').read_text())
assert len(coverage)==original.count('\\begin{lstlisting}')==9
save('copertura-strutturale.json', [{'level':l,'title':t} for l,t in expected])
blocks={}
for m in re.finditer(r'\\begin\{lstlisting\}\[(.*?)\]\n(.*?)\\end\{lstlisting\}',chapter,re.S):
    label=re.search(r'label=\{?([\w:.-]+)',m[1]).group(1)
    assert label not in blocks
    blocks[label]=m[2]
fragments=json.loads((OUT/'frammenti.json').read_text())
assert set(blocks)=={f['label'] for f in fragments}
assert all(set(c['estratti'])<=set(blocks) and c['adattamento'] for c in coverage)
original_blocks=list(re.finditer(r'\\begin\{lstlisting\}.*?\\end\{lstlisting\}',original,re.S))
for c,m in zip(coverage,original_blocks):
    assert c['original_sha256']==hashlib.sha256(m[0].encode()).hexdigest()
    assert c['firstline']==original[:m.start()].count('\n')+1
for f in fragments:
    if f.get('kind')=='commands':
        lines=(ROOT/f['source']).read_text().splitlines()
        actual=blocks[f['label']].replace('\\\n',' ').splitlines()
        actual=[re.sub(r'\s+',' ',x).strip() for x in actual if x and not x.startswith('#')]
        assert actual==[x['text'] for x in f['commands']]
        for c in f['commands']: assert lines[c['line']-1]==c['text']
    else:
        source=''.join((ROOT/f['source']).read_text().splitlines(True)[f['firstline']-1:f['lastline']])
        assert source==blocks[f['label']]==f['text'], f['label']
        assert hashlib.sha256(source.encode()).hexdigest()==f['sha256']
# Initial three fragments preserved literally, regardless of section relocation.
for f in json.loads((DOC/'verifiche/ticket-06/frammenti.json').read_text()):
    assert blocks[f['label']]==f['text']

# Historical results: parse raw logs/XML, never run their scripts.
keys=['tests','failures','errors','skipped']
pattern=r'Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)'
historical={}
e1=ROOT/'docs/evidence/postgres-enums-2026-11-09'
for name, expected_total in [('full-suite.txt',[423,0,0,0]),('targeted-green.txt',[82,0,0,0]),('contract-red.txt',[4,2,0,0])]:
    text=(e1/name).read_text()
    total=list(map(int,re.findall(pattern,text)[-1])); assert total==expected_total
    classes=re.findall(pattern+r'.*?-- in it\.unifi\.drivehub\.(\w+)',text)
    if classes: assert [sum(int(row[i]) for row in classes) for i in range(4)]==total
    historical[name]={'totals':dict(zip(keys,total)),'classes':classes,'finished':re.findall(r'Finished at: (.*)',text)}
assert len(historical['full-suite.txt']['classes'])==23
without={'DomainInvariantsTest','BookingRulesTest','PriceCalculatorServiceTest','RequestAgreementTest','SaleTypeTest','TestDatabaseConfigurationTest'}
assert sum(int(x[0]) for x in historical['full-suite.txt']['classes'] if x[4] in without)==55
historical['E1_groups']={'without_db':55,'postgres':368,'note':'Derivati dalle classi del log; invocazioni, non percentuali.'}
with zipfile.ZipFile(e1/'tdd-failures.zip') as z:
    names=z.namelist(); historical['E1_red_archive_members']=names
    dao=[n for n in names if n.endswith('dao-red.txt')][0]
    values=list(map(int,re.findall(pattern,z.read(dao).decode())[-1]))
    assert values==[81,0,77,0]; historical['E1_dao_red']=dict(zip(keys,values))
e2=ROOT/'docs/evidence/r11-integrated'
for label,total in [('full',[394,0,0,0]),('targeted',[363,0,0,0]),('targeted-attempt1',[363,0,4,0])]:
    cases=[]; counts=[0]*4; files=sorted((e2/(label+'-surefire')).glob('TEST-*.xml'))
    for p in files:
        xml=ET.parse(p).getroot()
        for i,k in enumerate(keys): counts[i]+=int(xml.attrib[k])
        methods=xml.findall('testcase'); assert len(methods)==int(xml.attrib['tests'])
        cases.extend({'class':c.get('classname'),'name':c.get('name'),'error':c.find('error') is not None,'failure':c.find('failure') is not None} for c in methods)
    assert counts==total
    assert list(map(int,re.findall(pattern,(e2/(label+'.log')).read_text())[-1]))==counts
    historical['E2_'+label]={'classes':len(files),'invocations':len(cases),'totals':dict(zip(keys,counts)),'command':(e2/(label+'.log')).read_text().splitlines()[0]}
    save('metodi-storici-'+label+'.json',cases)
assert historical['E2_full']['classes']==22
e2_groups=json.loads((e2/'full-groups.json').read_text())
assert e2_groups=={'without_database':47,'postgresql':347}
historical['E2_groups']=e2_groups
before_sentinel=json.loads((e2/'before-suite-application.sha256.json').read_text())
after_sentinel=json.loads((e2/'after-suite-application.sha256.json').read_text())
assert before_sentinel==after_sentinel
assert sum(k.startswith('table/') for k in before_sentinel)==13
assert sum(k.startswith('sequence/') for k in before_sentinel)==7
historical['E2_external_sentinel']={'tables':13,'sequences':7,'equal_before_after':True}
manifest=json.loads((e2/'full-inputs.sha256.json').read_text())
different=[p for p,h in manifest.items() if not (ROOT/p).exists() or sha(ROOT/p)!=h]
assert len(different)==28
historical['E2_different_inputs']=different
historical['E1_tree_diff']=subprocess.check_output(['git','diff','d4d82c1815e1fe2d107331fb4f2211b2bc3ca850','HEAD','--','src','pom.xml'],cwd=ROOT,text=True)
assert not historical['E1_tree_diff']
ui=[]
for name in ['AgreementUiCheck','SaleOutcomeUiCheck','DestinationUiCheck','IdentityUiCheck','TableLifecycleUiCheck','SlowModelsUiCheck','AppStartupUiCheck']:
    log=(e2/(name+'.log')).read_text(); marker='APP STARTUP SUCCESS' if name=='AppStartupUiCheck' else 'UI CHECK SUCCESS'
    assert marker in log
    inputs=json.loads((e2/(name+'-inputs.json')).read_text())
    for p,h in inputs.items(): assert sha(ROOT/p)==h['original_sha256'],p
    ui.append({'harness':name,'marker':marker,'input_manifest':str((e2/(name+'-inputs.json')).relative_to(ROOT)),'adapted':[p for p,h in inputs.items() if h['original_sha256']!=h['executed_sha256']]})
historical['E3']=ui
historical['scope']='Solo lettura delle fonti storiche, nessuna nuova esecuzione applicativa.'
save('riscontro-storico.json',historical)

# PDF checks, extraction, conservation and render. This script does not claim visual inspection.
money=lambda x: str(Decimal(str(x)).quantize(Decimal('.01'),rounding=ROUND_HALF_UP))
examples={'noleggio_3giorni':money(20000*.001*(1-math.pow(.95,3))/.05),
          'deprezzamenti_combinati':money(10000*.8*.8*.95**2*.95**2),
          'centesimi_prima_formula':money(Decimal('10000.01')*Decimal('.9')),
          'rimborso_acconto_4000':money(4000*.8),'penale_acconto_4000':money(4000*.2)}
assert list(examples.values())==['57.05','5212.84','9000.01','3200.00','800.00']
save('esempi-importi.json',{'values':examples,'scope':'Ricalcolo documentale Python delle formule, non esecuzione Java.'})
pdf=fitz.open(DOC/'build/ticket12/main.pdf')
assert (DOC/'relazione-drivehub.pdf').read_bytes()==(DOC/'build/ticket12/main.pdf').read_bytes()
toc=pdf.get_toc(); top=[x for x in toc if x[0]==1]
assert [x[1] for x in top]==[x[1] for x in oldpdf.get_toc() if x[0]==1]
ranges={}
for i,(_,title,page) in enumerate(top): ranges[title]=[page,(top[i+1][2]-1 if i+1<len(top) else len(pdf))]
oldtop=[x for x in oldpdf.get_toc() if x[0]==1]
oldranges={title:[page,(oldtop[i+1][2]-1 if i+1<len(oldtop) else len(oldpdf))] for i,(_,title,page) in enumerate(oldtop)}
conservation=[]
def body(page):
    lines=page.get_text().splitlines()
    if lines and re.fullmatch(r'\d+|[ivx]+',lines[-1].strip()): lines.pop()
    return norm('\n'.join(lines))
for title in ranges:
    if title in ['Testing','Matrice delle verifiche']: continue
    a,b=ranges[title]; x,y=oldranges[title]; assert b-a==y-x
    pixel_same=[]
    for n,o in zip(range(a-1,b),range(x-1,y)):
        assert body(pdf[n])==body(oldpdf[o]), f'Testo variato {title} p{n+1}'
        pixel_same.append(pdf[n].get_pixmap().samples==oldpdf[o].get_pixmap().samples)
    if title in ['Introduzione','Analisi dei requisiti e progettazione','Implementazione']: assert all(pixel_same)
    conservation.append({'part':title,'before':[x,y],'after':[a,b],'text_equal_without_footer':True,'pixel_equal':sum(pixel_same),'pages':len(pixel_same)})
save('conservazione-pdf.json',conservation)
checks={}
alltext='\n'.join(p.get_text() for p in pdf)
assert '??' not in alltext
links=0; fonts=set()
for page in pdf:
    for link in page.get_links():
        if link.get('kind') in (fitz.LINK_GOTO, fitz.LINK_NAMED):
            assert isinstance(link.get('page'), int), link
            assert 0<=link['page']<len(pdf)
            links+=1
    for font in page.get_fonts(): fonts.add(font[0])
    for word in page.get_text('words'):
        assert word[0]>=-1 and word[1]>=-1 and word[2]<=page.rect.width+1 and word[3]<=page.rect.height+1
assert all(pdf.extract_font(x)[3] for x in fonts)
assert links>0
save('integrita-pdf.json',{'resolved_internal_links':links,'embedded_fonts':len(fonts),'text_within_pages':True})
# Listing presence in PDF, normalizing whitespace/ligatures only. Line numbers are left intact.
fragment_pages={}
for f in fragments:
    if f.get('kind')=='commands': needles=[c['text'] for c in f['commands']]
    else: needles=[line.strip() for line in f['text'].splitlines() if line.strip()]
    hits=[]
    for n in range(ranges['Testing'][0]-1,ranges['Testing'][1]):
        text=norm(pdf[n].get_text())
        if f.get('kind')=='commands': text=text.replace('\\','')
        if all(norm(line) in text for line in needles): hits.append(n+1)
    assert hits, 'Estratto non completo nel PDF: '+f['label']
    fragment_pages[f['label']]=hits
save('pagine-frammenti.json',fragment_pages)
log=(DOC/'build/ticket12/main.log').read_text()
for marker in ['Overfull','Underfull','Missing character','undefined references','Rerun to get']: assert marker not in log,marker
messages=re.findall(r'ignored error: Infinite glue shrinkage found in box being split \[(\d+)\]',log)
save('messaggi-tex.json',{'baseline':re.findall(r'ignored error: Infinite glue shrinkage found in box being split \[(\d+)\]',(OUT/'latex-baseline.log').read_text()),'current_printed_pages':messages,'current_physical_pages':[int(x)+4 for x in messages],'count':len(messages)})
extract=fitz.open()
for title in ['Testing','Matrice delle verifiche']:
    a,b=ranges[title]; extract.insert_pdf(pdf,from_page=a-1,to_page=b-1)
extract_path=OUT/'capitolo-4-appendice-C.pdf'
if extract_path.exists():
    previous=fitz.open(extract_path)
    identical=len(previous)==len(extract) and all(previous[i].get_pixmap().samples==extract[i].get_pixmap().samples for i in range(len(extract)))
else: identical=False
if not identical: extract.save(extract_path)
render=OUT/'pagine'; render.mkdir(exist_ok=True)
selected=list(range(2,5))
for title in ['Testing','Matrice delle verifiche']:
    a,b=ranges[title]; selected.extend(range(a,b+1))
for n in selected: pdf[n-1].get_pixmap(matrix=fitz.Matrix(1.5,1.5)).save(render/f'pagina-{n:03}.png')
save('pagine.json',{'pages':len(pdf),'ranges':ranges,'visual_review_pages':selected,'extract_pages':len(extract)})
# New source manifest; old manifests remain read-only.
sources={x['source'] for x in json.loads((DOC/'verifiche/ticket-06/fonti.json').read_text())}
sources.update(f['source'] for f in fragments)
sources.update(str(p.relative_to(ROOT)) for p in (ROOT/'src/test/java/it/unifi/drivehub').glob('*.java'))
sources.update(str(p.relative_to(ROOT)) for p in e2.glob('*.json'))
sources.update(str(p.relative_to(ROOT)) for p in e2.glob('*.log'))
sources.update(str(p.relative_to(ROOT)) for p in e2.glob('*.patch'))
sources.update(str(p.relative_to(ROOT)) for p in e2.glob('*surefire/TEST-*.xml'))
save('fonti.json',[{'source':s,'sha256':sha(ROOT/s)} for s in sorted(sources)])
save('esito-documentale.json',{'status':'PASS','pages':len(pdf),'structural_entries':len(expected),'listings':len(fragments),'retained_exact_listings':3,'historical_classes_E1':23,'historical_classes_E2':22,'historical_counts':'Separate, nessuna esecuzione o coverage derivata','changed':changed,'pdf_sha256':sha(DOC/'build/ticket12/main.pdf'),'extract_sha256':sha(extract_path),'visual_inspection':'Verbale manuale separato; non dimostrata da questo script.'})
print('PASS:',len(expected),'voci;',len(fragments),'estratti/comandi; E1 423 e82; E2 394 e363/363 con tentativo4errori separato;',len(pdf),'pagine; conservazione verificata.')
