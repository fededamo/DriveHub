"""Ricalcola separatamente gli esiti storici leggendo log, XML e manifest."""
from pathlib import Path
import hashlib
import json
import re
import subprocess
import xml.etree.ElementTree as ET
import zipfile

OUT = Path(__file__).resolve().parent
ROOT = OUT.parents[3]
EVIDENCE = ROOT / 'docs/evidence'
keys = ['tests', 'failures', 'errors', 'skipped']
pattern = r'Tests run: (\d+), Failures: (\d+), Errors: (\d+), Skipped: (\d+)'
result = {'scope': 'Lettura documentale; nessuna esecuzione applicativa, nessun conteggio sommato o trasformato in coverage.'}
sources = set()


def read(path):
    sources.add(path)
    return path.read_text()


def total(text):
    return list(map(int, re.findall(pattern, text)[-1]))


e1 = EVIDENCE / 'postgres-enums-2026-11-09'
for name, expected in [('full-suite.txt', [423, 0, 0, 0]), ('targeted-green.txt', [82, 0, 0, 0]), ('contract-red.txt', [4, 2, 0, 0])]:
    text = read(e1 / name)
    assert total(text) == expected
    classes = re.findall(pattern + r'.*?-- in it\.unifi\.drivehub\.(\w+)', text)
    assert [sum(int(row[i]) for row in classes) for i in range(4)] == expected
    result['E1_' + name] = dict(totals=dict(zip(keys, expected)), classes=classes,
        finished=re.findall(r'Finished at: (.*)', text))
sources.add(e1 / 'tdd-failures.zip')
with zipfile.ZipFile(e1 / 'tdd-failures.zip') as archive:
    assert total(archive.read('dao-red.txt').decode()) == [81, 0, 77, 0]
    result['E1_failed_dao'] = dict(tests=81, failures=0, errors=77, skipped=0)
result['E1_src_pom_diff'] = subprocess.check_output(['git', 'diff',
    'd4d82c1815e1fe2d107331fb4f2211b2bc3ca850', 'HEAD', '--', 'src', 'pom.xml'], cwd=ROOT, text=True)
assert not result['E1_src_pom_diff']
result['E1_limit'] = 'Nessun XML per metodo o manifest runtime; confronto storico src/POM non equivale a impronte dei byte eseguiti.'
e2 = EVIDENCE / 'r11-integrated'
for session, expected in [('full', [394, 0, 0, 0]), ('targeted', [363, 0, 0, 0]), ('targeted-attempt1', [363, 0, 4, 0])]:
    files = sorted((e2 / (session + '-surefire')).glob('TEST-*.xml'))
    counts = [0] * 4
    for path in files:
        sources.add(path)
        root = ET.parse(path).getroot()
        assert len(root.findall('testcase')) == int(root.get('tests'))
        for i, key in enumerate(keys):
            counts[i] += int(root.get(key))
    log = read(e2 / (session + '.log'))
    assert counts == total(log) == expected
    result['E2_' + session] = dict(totals=dict(zip(keys, counts)), classes=len(files), command=log.splitlines()[0])
manifest = json.loads(read(e2 / 'full-inputs.sha256.json'))
different = [name for name, digest in manifest.items()
             if not (ROOT / name).is_file() or hashlib.sha256((ROOT / name).read_bytes()).hexdigest() != digest]
assert len(different) == 28
result['E2_different_inputs'] = different
before = json.loads(read(e2 / 'before-suite-application.sha256.json'))
after = json.loads(read(e2 / 'after-suite-application.sha256.json'))
assert before == after
result['E2_sentinel'] = dict(tables=sum(x.startswith('table/') for x in before),
    sequences=sum(x.startswith('sequence/') for x in before), unchanged=True)
assert result['E2_sentinel'] == dict(tables=13, sequences=7, unchanged=True)
result['E3'] = []
for name in ['AgreementUiCheck', 'SaleOutcomeUiCheck', 'DestinationUiCheck', 'IdentityUiCheck',
             'TableLifecycleUiCheck', 'SlowModelsUiCheck', 'AppStartupUiCheck']:
    marker = 'APP STARTUP SUCCESS' if name == 'AppStartupUiCheck' else 'UI CHECK SUCCESS'
    assert marker in read(e2 / (name + '.log'))
    manifest = json.loads(read(e2 / (name + '-inputs.json')))
    for filename, row in manifest.items():
        assert hashlib.sha256((ROOT / filename).read_bytes()).hexdigest() == row['original_sha256']
    result['E3'].append(dict(harness=name, marker=marker,
        adapted=[filename for filename, row in manifest.items() if row['original_sha256'] != row['executed_sha256']]))
result['E3_limit'] = 'Sessione fase 1, non ripetuta in fase 2/E1; sentinella finale UI mancante. SaleOutcome usa ACQUISITION e importi negativi incompatibili con il corrente. Tentativi falliti e patch restano conservati.'
for name in ['verification.md', 'commands.md', 'SaleOutcomeUiCheck-adaptation.patch', 'DestinationUiCheck-adaptation.patch']:
    if (e2 / name).is_file():
        sources.add(e2 / name)
(OUT / 'riscontro-storico.json').write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n')
(OUT / 'fonti-storiche.json').write_text(json.dumps([
    dict(source=str(path.relative_to(ROOT)), sha256=hashlib.sha256(path.read_bytes()).hexdigest())
    for path in sorted(sources)], indent=2) + '\n')
print('PASS: E1 423/82 e fallimenti separati; E2 394/363/363 con 4 errori nel primo mirato; 28 input diversi; sette marcatori E3.')
