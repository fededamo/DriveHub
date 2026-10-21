#!/usr/bin/env python3
"""Archive actual Surefire/JaCoCo evidence, omitting JVM properties and local paths."""
import argparse, datetime, hashlib, json, os, pathlib, subprocess, xml.etree.ElementTree as ET
ROOT = pathlib.Path(__file__).resolve().parents[1]
def baseline():
    paths = [ROOT / 'pom.xml', ROOT / 'compose.yaml', ROOT / 'compose.test.yaml'] + sorted((ROOT / 'src').rglob('*'))
    files = {str(p.relative_to(ROOT)): hashlib.sha256(p.read_bytes()).hexdigest() for p in paths if p.is_file()}
    return files, hashlib.sha256(json.dumps(files, sort_keys=True).encode()).hexdigest()
def main():
    parser = argparse.ArgumentParser(); parser.add_argument('run'); parser.add_argument('--command', required=True); parser.add_argument('--log', required=True)
    parser.add_argument('--java-description', help='Exact java -version output from an isolated runner')
    args = parser.parse_args(); out = ROOT / 'docs/evidence' / args.run; out.mkdir(parents=True, exist_ok=True)
    files, fingerprint = baseline(); suites=[]
    for path in sorted((ROOT / 'target/surefire-reports').glob('TEST-*.xml')):
        tree=ET.parse(path); suite=tree.getroot()
        for prop in suite.findall('properties'): suite.remove(prop)
        for elem in list(suite):
            if elem.tag in ('system-out', 'system-err'): suite.remove(elem)
        suites.append({**{k:suite.get(k) for k in ('name','tests','failures','errors','skipped')}, 'cases':[t.get('name') for t in suite.findall('testcase')]})
        tree.write(out/path.name, encoding='utf-8', xml_declaration=True)
    if not suites: raise SystemExit('No Surefire reports; refusing empty evidence')
    jacoco=ROOT/'target/site/jacoco/jacoco.xml'; tree=ET.parse(jacoco); tree.write(out/'jacoco.xml',encoding='utf-8',xml_declaration=True)
    def counters(node):
        result={}
        for c in node.findall('counter'):
            missed,covered=int(c.get('missed')),int(c.get('covered')); total=missed+covered
            result[c.get('type')]={'covered':covered,'missed':missed,'total':total,'percent':round(100*covered/total,2) if total else None}
        return result
    log=pathlib.Path(args.log).read_text()
    log=log.replace(str(ROOT),'<DriveHub>').replace(str(pathlib.Path.home()),'<home>')
    (out/'maven.log').write_text(log)
    commit_base = os.environ.get('DRIVEHUB_COMMIT_BASE')
    if not commit_base:
        commit_base = subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT,text=True).strip()
    java_description = args.java_description or subprocess.run(
        ['java','-version'],capture_output=True,text=True,check=True).stderr.strip()
    data={'run':args.run,'recorded_at':datetime.datetime.now(datetime.timezone.utc).isoformat(),
          'command':args.command,'java':java_description,
          'commit_base':commit_base,
          'baseline_sha256':fingerprint,'baseline_kind':'commit base + working tree; manifest of pom, compose and src',
          'build_success':'BUILD SUCCESS' in log and 'BUILD FAILURE' not in log,
          'totals':{k:sum(int(s[k]) for s in suites) for k in ('tests','failures','errors','skipped')},
          'coverage':counters(tree.getroot()),'package_coverage':{p.get('name'):counters(p) for p in tree.getroot().findall('package')}, 'suites':suites}
    (out/'summary.json').write_text(json.dumps(data,indent=2)+'\n'); (out/'source-manifest.json').write_text(json.dumps(files,indent=2)+'\n')
    print(json.dumps({k:data[k] for k in ('run','build_success','totals','coverage','baseline_sha256')},indent=2))
    if not data['build_success'] or any(data['totals'][k] for k in ('failures','errors')): raise SystemExit(1)
if __name__=='__main__': main()
