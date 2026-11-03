"""Confronto con la fotografia locale18, senza scritture Git."""
from pathlib import Path
import difflib
import hashlib
import json
import os
import subprocess

root = Path('/home/federico/dev/Drivehub')
evidence = Path(__file__).resolve().parent
before = Path('/tmp/drivehub-ticket18-session/before')
baseline = json.loads((evidence / 'baseline-sha256.json').read_text())
excluded = {'session.patch', 'session-files.sha256.json', 'preservation.json',
            'review.patch', 'review-files.sha256.json', 'delivery.json'}

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def files():
    for base in [root / 'DriveHub', root / 'TRACKER_DRIVEHUB']:
        for directory, dirs, names in os.walk(base):
            dirs[:] = [name for name in dirs if name not in ('.git', 'Relazione_SWE')]
            for name in names:
                path = Path(directory) / name
                if path.is_file() and not (path.parent == evidence and name in excluded):
                    yield path
    yield root / 'PIANO_REVISIONI_DRIVEHUB.md'

current = {str(path.relative_to(root)): digest(path) for path in files()}
missing = sorted(set(baseline) - set(current))
changed = sorted(rel for rel in baseline if rel in current and baseline[rel] != current[rel])
assert not missing, missing
allowed = {'PIANO_REVISIONI_DRIVEHUB.md',
           'TRACKER_DRIVEHUB/revisioni/issues/18-verifica-integrata-e-chiusura.md',
           'DriveHub/docs/testing.md', 'DriveHub/docs/evidence/README.md'}
assert set(changed) <= allowed, changed
assert all(digest(before / rel) == expected for rel, expected in baseline.items())
repo = root / 'DriveHub'
assert subprocess.check_output(['git', '-C', str(repo), 'rev-parse', 'HEAD'], text=True) == (evidence / 'initial-head.txt').read_text()
assert digest(repo / '.git/index') == (evidence / 'initial-index.sha256').read_text().strip()
added = sorted(set(current) - set(baseline))
assert all(rel.startswith('DriveHub/docs/evidence/r11-integrated/') for rel in added)
patch = []
for rel in sorted(changed + added):
    old = (before / rel).read_bytes() if rel in baseline else b''
    new = (root / rel).read_bytes()
    try:
        oldtext, newtext = old.decode('utf-8'), new.decode('utf-8')
    except UnicodeDecodeError:
        patch.append(f'Binary {rel}: SHA256 {hashlib.sha256(new).hexdigest()}\n')
        continue
    patch.extend(difflib.unified_diff(oldtext.splitlines(keepends=True), newtext.splitlines(keepends=True),
                                     fromfile='before/' + rel, tofile='after/' + rel))
(evidence / 'session.patch').write_text(''.join(patch))
(evidence / 'session-files.sha256.json').write_text(json.dumps({rel: current[rel] for rel in sorted(changed + added)}, indent=2) + '\n')
result = {'initial_files': len(baseline), 'identical': len(baseline) - len(changed),
          'intentional_changes': changed, 'missing': missing, 'new_files': len(added),
          'head_unchanged': True, 'index_unchanged': True,
          'ignored_and_target_included': True, 'excluded': sorted(excluded)}
(evidence / 'preservation.json').write_text(json.dumps(result, indent=2) + '\n')
print(json.dumps(result, ensure_ascii=False))
