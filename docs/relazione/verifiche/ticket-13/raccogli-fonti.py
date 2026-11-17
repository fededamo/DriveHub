"""Conserva intervalli leggibili e impronte, senza processi applicativi."""
from pathlib import Path
import hashlib
import json
import zipfile
import pymupdf as fitz

OUT = Path(__file__).resolve().parent
ROOT = OUT.parents[3]
DOC = ROOT / 'docs/relazione'
P = 'src/main/java/it/unifi/drivehub/'
T = 'src/test/java/it/unifi/drivehub/'
SPANS = {
 P+'dao/DatabaseManager.java': [(49,106)],
 P+'service/PriceCalculatorService.java': [(26,111)],
 P+'service/RentalService.java': [(59,178)],
 P+'service/PurchaseService.java': [(65,233)],
 P+'service/SaleToDealershipService.java': [(55,182)],
 P+'service/TestDriveService.java': [(40,87)],
 P+'event/VehicleEventPublisher.java': [(28,32)],
 P+'service/TestDriveCancellationObserver.java': [(15,26)],
 P+'presentation/view/AsyncLoad.java': [(23,75)],
 P+'App.java': [(34,122)],
 P+'dao/postgres/PostgresRequestDao.java': [(147,168),(214,227)],
 P+'dao/postgres/PostgresUserDao.java': [(215,291)],
 'src/main/resources/db/schema.sql': [(36,39),(79,96),(147,310)],
 T+'AuthServiceIntegrationTest.java': [(19,104)],
 T+'PurchaseAgreementIntegrationTest.java': [(26,78)],
 T+'TestDriveCancellationIntegrationTest.java': [(25,181)],
 T+'PurchaseConcurrencyIntegrationTest.java': [(35,85),(172,179),(267,355)],
 T+'IdentityDeletionIntegrationTest.java': [(309,323)],
 T+'DemoSqlValidationTest.java': [(16,51)],
 T+'PriceSelectionIntegrationTest.java': [(25,30),(61,69)],
 T+'PriceCalculatorServiceTest.java': [(93,166)],
 T+'SaleTypeTest.java': [(16,71)],
}

def sha(path):
 return hashlib.sha256(path.read_bytes()).hexdigest()

def save(name, value):
 (OUT/name).write_text(json.dumps(value, ensure_ascii=False, indent=2)+'\n')

records=[]
text=['Riscontri S0 del ticket 13: sole letture, nessuna esecuzione applicativa.']
for name, intervals in SPANS.items():
 lines=(ROOT/name).read_text().splitlines()
 for first,last in intervals:
  assert last<=len(lines), (name,last,len(lines))
  text.extend(['',f'{name}:{first}–{last}','\n'.join(f'{i}: {lines[i-1]}' for i in range(first,last+1))])
  records.append(dict(source=name,first=first,last=last,sha256=sha(ROOT/name)))
(OUT/'estratti-fonti.txt').write_text('\n'.join(text)+'\n')
save('intervalli-fonti.json',records)
sources=set(SPANS)
for ticket in [7,9,10,11,12]:
 manifest=json.loads((DOC/f'verifiche/ticket-{ticket:02}/fonti.json').read_text())
 sources.update(manifest if isinstance(manifest,dict) else (row['source'] for row in manifest))
save('fonti.json',[dict(source=name,sha256=sha(ROOT/name)) for name in sorted(sources)])
notes=ROOT.parent/'Slides and notes - academic year 202526-20261020/NotesOnTheProjectWork_Jan2025.pdf'
pdf=fitz.open(notes)
assert len(pdf)==47
(OUT/'note-consegna.txt').write_text('Fonte locale '+str(notes)+'\nSHA256 '+sha(notes)+'\nPagine fisiche.\n'+'\n'.join(f'PAGINA FISICA {n}\n'+pdf[n-1].get_text() for n in [2,4,9,13,14,26,27,28,29,33,34,35,44,45,47]))
archive=ROOT/'docs/evidence/revision-records.zip'
with zipfile.ZipFile(archive) as package:
 manifest=json.loads(package.read('SHA256.json'))
 for name,digest in manifest.items():
  assert hashlib.sha256(package.read(name)).hexdigest()==digest
 save('registri-originali.json',dict(archive_sha256=sha(archive),verified_members=manifest,limit='Registri e dichiarazioni, non trascrizioni o attribuzioni per riga.'))
print('PASS:',len(sources),'fonti;',len(records),'intervalli;',len(manifest),'membri dei registri originali.')
