from pathlib import Path
import hashlib, json, shutil, subprocess, sys, xml.etree.ElementTree as ET

s=Path('/tmp/drivehub-ticket17-session')
e=Path('/home/federico/dev/Drivehub/DriveHub/docs/evidence/r09-diagrams')
repo=s/'install/DriveHub'
env=json.loads((s/'env.json').read_text())
label, *args=sys.argv[1:]
assert label in ('build','without-db','targeted','full')
inputs={str(p.relative_to(repo)):hashlib.sha256(p.read_bytes()).hexdigest() for p in [repo/'pom.xml',*sorted((repo/'src').rglob('*'))] if p.is_file()}
(e/(label+'-inputs.sha256.json')).write_text(json.dumps(inputs,indent=2)+'\n')
command=['mvn','-B',*args]
with (e/(label+'.log')).open('w') as log:
    log.write('COMMAND: '+' '.join(command)+'\n'); log.flush()
    result=subprocess.run(command,cwd=repo,env=env,stdout=log,stderr=subprocess.STDOUT)
if label != 'build':
    dest=e/(label+'-surefire')
    dest.mkdir(exist_ok=True)
    totals=dict.fromkeys(['tests','failures','errors','skipped'],0)
    for p in sorted((repo/'target/surefire-reports').glob('TEST-*.xml')):
        tree=ET.parse(p)
        for key in totals: totals[key]+=int(tree.getroot().attrib[key])
        # Surefire properties contain the private installation configuration in classpath only;
        # omit environment-specific properties and retain testcases/failures intact.
        props=tree.getroot().find('properties')
        if props is not None: tree.getroot().remove(props)
        tree.write(dest/p.name,encoding='utf-8',xml_declaration=True)
    (e/(label+'-summary.json')).write_text(json.dumps(totals,indent=2)+'\n')
    print(label,totals)
print(label,'exit',result.returncode)
sys.exit(result.returncode)
