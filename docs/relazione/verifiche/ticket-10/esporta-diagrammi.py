"""Legge undici copie adattate; scrive solo esportazioni PDF e log del ticket 10."""
from pathlib import Path
import subprocess, os, json, hashlib
import xml.etree.ElementTree as E
import pymupdf as fitz
HERE=Path(__file__).resolve().parent
DIAGRAMS=HERE.parents[1]/'diagrammi'
BINARY=os.environ.get('T10_DRAWIO','/tmp/drivehub-ticket10-tools32/opt/drawio/drawio')
names=['casi-uso-autori','navigazione-autori','package-autori','classi-dominio-insieme','classi-business-insieme','classi-dao-insieme','classi-presentazione-insieme','sequenza-vendita-insieme','sequenza-noleggio-insieme','sequenza-cessione-insieme','database-er-autori']
results=[]
for name in names:
 source=DIAGRAMS/(name+'.drawio'); E.parse(source)
 before=hashlib.sha256(source.read_bytes()).hexdigest()
 command=[BINARY,'--disable-gpu','--disable-dev-shm-usage','--user-data-dir=/tmp/drivehub-ticket10-profile32-'+name,'-x','-f','pdf','-e','-a','--crop','-b','20','-o',str(source)+'.pdf',str(source)]
 result=subprocess.run(command,capture_output=True,text=True,timeout=55)
 (HERE/(name+'-esportazione.txt')).write_text('\n'.join(command)+'\n'+result.stdout+result.stderr+'\nexit: '+str(result.returncode)+'\n')
 assert result.returncode==0 and Path(str(source)+'.pdf').is_file(),name
 # Composizione su foglio A3 dopo il rendering draw.io; il contenuto resta vettoriale.
 raw=HERE/(name+'-export-grezzo.pdf')
 raw.write_bytes(Path(str(source)+'.pdf').read_bytes())
 drawing=fitz.open(raw); final=fitz.open()
 size=(841.8898,1190.5512) if name.startswith('sequenza-') else (1190.5512,841.8898)
 for page in drawing:
  target=final.new_page(width=size[0],height=size[1])
  target.show_pdf_page(fitz.Rect(24,24,size[0]-24,size[1]-42),drawing,page.number,keep_proportion=True)
 final.embfile_add(name+'.drawio',source.read_bytes(),filename=name+'.drawio')
 final.save(str(source)+'.pdf'); final.close(); drawing.close()
 assert hashlib.sha256(source.read_bytes()).hexdigest()==before
 results.append({'name':name,'source_sha256':before,'export_sha256':hashlib.sha256(Path(str(source)+'.pdf').read_bytes()).hexdigest()})
 print(name,'esportato',flush=True)
(HERE/'esportazioni.json').write_text(json.dumps(results,indent=2)+'\n')
