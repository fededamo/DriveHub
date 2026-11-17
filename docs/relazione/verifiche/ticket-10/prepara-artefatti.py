"""Estratto e rendering del PDF reale; nessun avvio applicativo."""
from pathlib import Path
import json
import pymupdf as fitz
from PIL import Image,ImageDraw
HERE=Path(__file__).resolve().parent; DOC=HERE.parents[1]
pdf=fitz.open(DOC/'relazione-drivehub.pdf');toc=pdf.get_toc();chapters=[r for r in toc if r[0]==1]
assert len(chapters)==8
start=chapters[1][2]-1;end=chapters[2][2]-1;a=chapters[5][2]-1;b=chapters[7][2]-1
pages=list(range(start,end))+list(range(a,b));extract=fitz.open()
for i in pages:extract.insert_pdf(pdf,from_page=i,to_page=i)
lookup={old+1:new+1 for new,old in enumerate(pages)}
extract.set_toc([[level,title,lookup[page]] for level,title,page in toc if page in lookup])
extract.save(HERE/'capitolo-2-appendici-A-B.pdf');extract.close()
(HERE/'pagine').mkdir(exist_ok=True);(HERE/'provini').mkdir(exist_ok=True)
# Oltre al perimetro, osserviamo indice e pagine dei messaggi TeX residui.
extra=list(range(1,start-3))+list(range(chapters[7][2]-1,len(pdf)))
for i in sorted(set(pages+extra)):
 pix=pdf[i].get_pixmap(matrix=fitz.Matrix(1.5,1.5));pix.save(HERE/'pagine'/f'pagina-{i+1:03}.png')
for batch in range(0,len(pages),4):
 sheet=Image.new('RGB',(1680,2380),'#dddddd')
 for index,i in enumerate(pages[batch:batch+4]):
  im=Image.open(HERE/'pagine'/f'pagina-{i+1:03}.png').convert('RGB');im.thumbnail((820,1150))
  x=(index%2)*840;y=(index//2)*1190
  sheet.paste(im,(x+(840-im.width)//2,y+30));ImageDraw.Draw(sheet).text((x+12,y+8),f'Pagina fisica {i+1}',fill='black')
 sheet.save(HERE/'provini'/f'provino-{batch//4+1:02}.png')
report={'pdf_pages':len(pdf),'chapter2':[start+1,end],'appendices_AB':[a+1,b],'inspected_pages':[i+1 for i in pages],'extra_pages':[i+1 for i in extra],'extract_pages':len(pages),'toc':toc}
(HERE/'pagine.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
print({k:v for k,v in report.items() if k not in ['toc','inspected_pages','extra_pages']})
