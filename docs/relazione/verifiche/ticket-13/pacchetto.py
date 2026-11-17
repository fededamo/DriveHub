"""Crea lo ZIP corrente, lo estrae da zero, compila e confronta i PDF."""
from pathlib import Path
import hashlib
import json
import shutil
import subprocess
import tempfile
import zipfile

import pymupdf as fitz

OUT = Path(__file__).resolve().parent
DOC = OUT.parents[1]


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


members = [DOC / 'main.tex', DOC / 'LEGGIMI-consegna.md']
for folder, pattern in [('capitoli', '*.tex'), ('appendici', '*.tex'),
                        ('diagrammi', '*.drawio'), ('diagrammi', '*.drawio.pdf'), ('screenshot', '*.png')]:
    members.extend(sorted((DOC / folder).glob(pattern)))
assert len(members) == 118, len(members)
archive = OUT / 'sorgenti-relazione.zip'
with zipfile.ZipFile(archive, 'w', compression=zipfile.ZIP_DEFLATED) as package:
    for path in sorted(members):
        package.write(path, path.relative_to(DOC))
digest = sha(archive)
work = Path(tempfile.mkdtemp(prefix='drivehub-ticket13-consegna-'))
attempt = len(list(OUT.glob('pacchetto-*-input.fls'))) + 1
with zipfile.ZipFile(archive) as package:
    assert package.testzip() is None
    for item in package.infolist():
        assert not Path(item.filename).is_absolute() and '..' not in Path(item.filename).parts
        assert package.read(item.filename) == (DOC / item.filename).read_bytes()
    package.extractall(work)
(work / 'build').mkdir()
command = ['xelatex', '-no-shell-escape', '-halt-on-error', '-interaction=nonstopmode',
           '-recorder', '-output-directory=build', 'main.tex']
stable = []
for number in range(1, 4):
    log = OUT / f'pacchetto-{attempt}-compilazione-{number}.txt'
    with log.open('w') as stream:
        stream.write('CWD: ' + str(work) + '\nCOMMAND: ' + ' '.join(command) + '\n')
        stream.flush()
        run = subprocess.run(command, cwd=work, stdout=stream, stderr=subprocess.STDOUT, timeout=120)
        stream.write('\nEXIT: ' + str(run.returncode) + '\n')
    assert run.returncode == 0, str(log)
    stable.append({name: sha(work / 'build' / name) for name in ['main.toc', 'main.aux', 'main.out']})
assert stable[1] == stable[2]
shutil.copy2(work / 'build/main.fls', OUT / f'pacchetto-{attempt}-input.fls')
shutil.copy2(work / 'build/main.log', OUT / f'pacchetto-{attempt}-latex-log.txt')
shutil.copy2(work / 'build/main.pdf', OUT / 'ricompilato-da-zip.pdf')
editorial, generated, resources = set(), set(), set()
for line in (work / 'build/main.fls').read_text().splitlines():
    if not line.startswith('INPUT '):
        continue
    path = Path(line[6:].strip('"'))
    path = (work / path).resolve() if not path.is_absolute() else path.resolve()
    if path.is_relative_to(work / 'build'):
        generated.add(str(path.relative_to(work)))
    elif path.is_relative_to(work):
        relative = str(path.relative_to(work))
        assert relative in {str(p.relative_to(DOC)) for p in members}, relative
        assert path.read_bytes() == (DOC / relative).read_bytes()
        editorial.add(relative)
    else:
        assert path.is_relative_to('/home/federico/dev/Drivehub/.ticket11-tools') or path.is_relative_to('/tmp/drivehub-ticket13-tex'), 'Input esterno non autorizzato: ' + str(path)
        resources.add(str(path))
delivered = fitz.open(DOC / 'relazione-drivehub.pdf')
rebuilt = fitz.open(work / 'build/main.pdf')
assert len(delivered) == len(rebuilt)
for n in range(len(delivered)):
    assert delivered[n].rect == rebuilt[n].rect, n + 1
    assert delivered[n].get_text() == rebuilt[n].get_text(), n + 1
    assert delivered[n].get_pixmap(matrix=fitz.Matrix(4/3, 4/3)).samples == rebuilt[n].get_pixmap(matrix=fitz.Matrix(4/3, 4/3)).samples, n + 1
log = (work / 'build/main.log').read_text()
assert log.count('ignored error: Infinite glue shrinkage found in box being split') == 5
for marker in ['Overfull', 'Underfull', 'Missing character', 'undefined', 'Rerun to', 'Warning:', '! ']:
    assert marker not in log, marker
assert sha(archive) == digest
result = dict(status='PASS', zip_sha256=digest, archive=str(archive), extraction=str(work), attempt=attempt,
    members=len(members), editorial_inputs=sorted(editorial), generated_inputs=sorted(generated),
    external_toolchain_resources=[dict(path=p, sha256=sha(Path(p))) for p in sorted(resources)],
    pages=len(rebuilt), same_pagination=True, same_text_all_pages=True, same_pixels_all_pages_dpi=96,
    delivered_pdf_sha256=sha(DOC / 'relazione-drivehub.pdf'), rebuilt_pdf_sha256=sha(work / 'build/main.pdf'),
    delivered_metadata=delivered.metadata, rebuilt_metadata=rebuilt.metadata,
    delivered_ID=delivered.xref_get_key(-1, 'ID'), rebuilt_ID=rebuilt.xref_get_key(-1, 'ID'),
    tex_messages=5, stabilization=stable,
    binary_difference='Metadati e ID registrati; identità binaria distinta dalla verifica completa di contenuto e resa.')
(OUT / f'autonomia-pacchetto-{attempt}.json').write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n')
(OUT / 'autonomia-pacchetto.json').write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n')
(OUT / 'manifest-pacchetto.json').write_text(json.dumps([
    dict(member=str(p.relative_to(DOC)), bytes=p.stat().st_size, sha256=sha(p)) for p in sorted(members)], ensure_ascii=False, indent=2) + '\n')
print(f'PASS: ZIP {digest}; {len(members)} membri; {len(editorial)} input editoriali interni; {len(rebuilt)} pagine identiche a testo e pixel a 96 dpi.')
print('Estrazione pulita:', work)
