from pathlib import Path
import hashlib, json, shutil, subprocess

ROOT = Path('/home/federico/dev/Drivehub')
SESSION = Path('/tmp/drivehub-ticket16-session')
def files():
    for base in (ROOT / 'DriveHub', ROOT / 'TRACKER_DRIVEHUB'):
        for directory, dirs, names in __import__('os').walk(base):
            dirs[:] = [d for d in dirs if d not in ('.git', 'target', 'Relazione_SWE')]
            for name in names:
                p = Path(directory) / name
                if p.is_file():
                    yield p
    yield ROOT / 'PIANO_REVISIONI_DRIVEHUB.md'
def digest(p):
    return hashlib.sha256(p.read_bytes()).hexdigest()
if __name__ == '__main__':
    before = SESSION / 'before'
    assert not before.exists()
    manifest = {}
    for p in files():
        rel = p.relative_to(ROOT)
        dest = before / rel
        dest.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(p, dest)
        manifest[str(rel)] = digest(p)
    (SESSION / 'baseline-sha256.json').write_text(json.dumps(manifest, indent=2) + '\n')
    head = subprocess.check_output(['git', '-C', str(ROOT/'DriveHub'), 'rev-parse', 'HEAD'], text=True)
    (SESSION/'initial-head.txt').write_text(head)
    index = ROOT/'DriveHub/.git/index'
    (SESSION/'initial-index.sha256').write_text(digest(index)+'\n')
    statuses = {}
    for p in sorted((ROOT/'TRACKER_DRIVEHUB/revisioni/issues').glob('*.md')):
        if int(p.name[:2]) <= 15:
            content = p.read_text()
            assert 'State: closed' in content and '- [ ]' not in content, p
            statuses[str(p.relative_to(ROOT))] = {'state': 'closed', 'sha256':digest(p)}
    assert len(statuses) == 15
    (SESSION/'prerequisites.json').write_text(json.dumps(statuses, indent=2)+'\n')
    print(f'Snapshot: {len(manifest)} files; prerequisites: {len(statuses)} closed')
