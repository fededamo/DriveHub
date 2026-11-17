"""Controlli documentali del ticket 10, inclusi materiali non tracciati."""
from pathlib import Path
import hashlib,json,re,tarfile,difflib
import xml.etree.ElementTree as E
import pymupdf as fitz
HERE=Path(__file__).resolve().parent;DOC=HERE.parents[1];ROOT=DOC.parents[1]
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def write(name,data): (HERE/name).write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
def expand(base,relative):
 s=(base/relative).read_text()
 return re.sub(r'\\input\{([^}]+)\}',lambda m:expand(base,m[1]+'.tex'),s)
base=json.loads((HERE/'baseline.json').read_text());changed=[];deleted=[]
for name,row in base['files'].items():
 p=ROOT/name
 if not p.exists():deleted.append(name)
 elif sha(p)!=row['sha256']:changed.append(name)
allowed={
 'docs/relazione/'+n for n in ['README.md','piano.md','indice.md','allineamento.md','diagrammi/README.md',
 'capitoli/2_progettazione.tex','capitoli/2_funzioni.tex','capitoli/2_architettura.tex','capitoli/2_sequenze.tex','capitoli/2_database.tex','capitoli/4_testing.tex',
 'appendici/appendici.tex','appendici/a_casi_uso.tex','appendici/b_classi.tex','appendici/c_verifiche.tex','relazione-drivehub.pdf','tracker/issues/10-riallineamento-progettazione.md']}
for n in ['casi-uso-autori','navigazione-autori','database-er-autori']:
 allowed|={'docs/relazione/diagrammi/'+n+suffix for suffix in ['.drawio','.drawio.pdf']}
assert not deleted,deleted
assert set(changed)<=allowed,set(changed)-allowed
current={str(p.relative_to(ROOT)) for directory in ['src','docs','Relazione_SWE'] for p in (ROOT/directory).rglob('*') if p.is_file()}
added=sorted(current-set(base['files']))
assert all(n.startswith(('docs/relazione/verifiche/ticket-10/','docs/relazione/build/ticket10/')) or n=='docs/relazione/capitoli/2_template.tex' or re.fullmatch(r'docs/relazione/diagrammi/(?:package-autori|(?:classi-(?:dominio|business|dao|presentazione)|sequenza-(?:vendita|noleggio|cessione))-insieme)\.drawio(?:\.pdf)?',n) for n in added),[n for n in added if not n.startswith('docs/relazione/')]
# Il confronto editoriale usa le copie iniziali, non HEAD (che ometterebbe i non tracciati).
delta=[]
with tarfile.open(HERE/'baseline-editoriale.tar.gz') as tar:
 for name in changed:
  if name.endswith(('.tex','.md','.drawio')):
   old=tar.extractfile(name)
   if old is not None:delta+=difflib.unified_diff(old.read().decode().splitlines(True),(ROOT/name).read_text().splitlines(True),fromfile='baseline/'+name,tofile='attuale/'+name)
for name in added:
 if not name.startswith('docs/relazione/verifiche/') and name.endswith(('.tex','.drawio')):
  delta+=difflib.unified_diff([],(ROOT/name).read_text().splitlines(True),fromfile='/dev/null',tofile=name)
(HERE/'delta-documentale.diff').write_text(''.join(delta))
write('delta-files.json',{'baseline_time':base['time'],'head':base['head'],'changed':changed,'added':added,'deleted':deleted})
old=expand(ROOT/'Relazione_SWE','capitoli/2_progettazione.tex');new=expand(DOC,'capitoli/2_progettazione.tex')
def headings(s,principal=False):
 if principal:
  s=re.sub(r'\\scheda\{(UC-\d+)\}\{([^}]+)\}',lambda m:'\\subsection{'+m[1]+' --- '+m[2]+'}',s)
 return [(kind,star,title) for kind,star,title in re.findall(r'\\(section|subsection|subsubsection)(\*?)\{([^}\n]+)\}',s) if '#' not in title and not title.startswith('\\texorpdfstring')]
o=headings(old);n=headings(new,True)
assert len(o)==len(n)==47,(len(o),len(n))
assert [(a,b) for a,b,_ in o]==[(a,b) for a,b,_ in n]
assert sum(k=='subsubsection' for k,_,_ in n)==18
assert sum(k=='section' for k,_,_ in n)==8
write('copertura-strutturale.json',[{'originale':a,'attuale':b} for a,b in zip(o,n)])
templates=(DOC/'capitoli/2_template.tex').read_text();appendix=(DOC/'appendici/a_casi_uso.tex').read_text()
assert re.findall(r'\\scheda\{(UC-[^}]+)\}',templates)==['UC-01','UC-02','UC-03','UC-05','UC-04','UC-06']
assert re.findall(r'\\scheda\{(UC-[^}]+)\}',appendix)==['UC-07','UC-08','UC-09','UC-10','UC-11','UC-S01','UC-S02','UC-S03','UC-S04']
assert templates.count('\\principale')==7 and templates.count('\\alternative')==7
assert templates.count('\\textbf{Prove e portata:}')==6
# Tutti gli attributi delle tredici relazioni compaiono nella spiegazione testuale.
schema=(ROOT/'src/main/resources/db/schema.sql').read_text();dbtext=(DOC/'capitoli/2_database.tex').read_text()
for table,body in re.findall(r'CREATE TABLE "(\w+)" \((.*?)\n\);',schema,re.S):
 fields=re.findall(r'^    "(\w+)" ',body,re.M)
 item=re.search(r'\\item\[\\code\{'+table+r'\}\](.*?)(?=\\item|\\end\{description\})',dbtext,re.S)
 assert item,table
 for field in fields:assert re.search(r'\\code\{(?:[A-Z/]+ )?'+field+r'\??\}',item[1]),(table,field)
# Ogni tavola ha XML nativo valido, PDF esportato e allegato nativo identico.
exports=json.loads((HERE/'esportazioni.json').read_text());assert len(exports)==11
for row in exports:
 p=DOC/'diagrammi'/(row['name']+'.drawio');t=E.parse(p)
 assert sha(p)==row['source_sha256']
 for root in t.iter('root'):
  ids=[c.get('id') for c in root];assert len(set(ids))==len(ids)
  for c in root:
   for k in ['source','target','parent']:assert c.get(k) is None or c.get(k) in ids
   if c.get('edge')=='1':assert c.find('mxGeometry') is not None
 export=Path(str(p)+'.pdf');assert sha(export)==row['export_sha256']
 pdf=fitz.open(export);assert pdf.embfile_get(row['name']+'.drawio')==p.read_bytes()
 for page in pdf:assert sorted(round(v) for v in [page.rect.width,page.rect.height])==[842,1191]
# Tutti gli input effettivi e i riferimenti sono risolti nella compilazione corrente.
log=(DOC/'build/ticket10/main.log').read_text(errors='replace')
for marker in ['undefined','Rerun to get','Overfull','Missing character','Negative labelwidth']:
 assert marker not in log,marker
aux=(DOC/'build/ticket10/main.aux').read_text();alltex='\n'.join(p.read_text() for p in (DOC/'capitoli').glob('*.tex'))+'\n'+'\n'.join(p.read_text() for p in (DOC/'appendici').glob('*.tex'))
labels=set(re.findall(r'\\newlabel\{([^}]+)\}',aux));refs=set(re.findall(r'\\(?:ref|pageref)\{([^}]+)\}',alltex));assert refs<=labels,refs-labels
pdf=fitz.open(DOC/'relazione-drivehub.pdf');meta=json.loads((HERE/'pagine.json').read_text());assert len(pdf)==meta['pdf_pages']
extract=fitz.open(HERE/'capitolo-2-appendici-A-B.pdf');assert len(extract)==len(meta['inspected_pages'])
for i,page in enumerate(meta['inspected_pages']):
 assert extract[i].get_text()==pdf[page-1].get_text()
 assert (HERE/'pagine'/f'pagina-{page:03}.png').is_file()
# Il capitolo 1 resta identico anche nel PDF, al netto dei metadati del file.
with tarfile.open(HERE/'baseline-editoriale.tar.gz') as tar:
 initial=fitz.open(stream=tar.extractfile('docs/relazione/relazione-drivehub.pdf').read(),filetype='pdf')
 for i in range(3,6):
  assert initial[i].get_text()==pdf[i].get_text()
  assert initial[i].get_pixmap().samples==pdf[i].get_pixmap().samples
write('esito-documentale.json',{'baseline_files':len(base['files']),'preserved':len(base['files'])-len(changed),'changed':len(changed),'added':len(added),'structural_entries':47,'ui_functions':18,'principal_cases':6,'additional_cases':9,'relations':13,'original_diagrams':11,'drawio_pairs':len(list((DOC/'diagrammi').glob('*.drawio'))),'resolved_refs':len(refs),'pdf_pages':len(pdf),'extract_pages':len(extract),'chapter1_identical':True,'tex_underfull':log.count('Underfull'),'tex_infinite_glue':log.count('Infinite glue shrinkage')})
print('Verifiche documentali superate: 47 voci, 18 funzioni UI, 6+9 schede, 13 relazioni, 11 tavole; capitolo 1 identico.')
