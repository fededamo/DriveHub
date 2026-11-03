from pathlib import Path
import hashlib, json, os, re, signal, subprocess, time

s=Path('/tmp/drivehub-ticket16-session')
e=Path('/home/federico/dev/Drivehub/DriveHub/docs/evidence/r09-instructions')
repo=s/'install/DriveHub'
env=json.loads((s/'env.json').read_text())
out=s/'startup'
out.mkdir(exist_ok=True)
java=Path(env['JAVA_HOME'])/'bin'
m2=Path.home()/'.m2/repository'
modules=':'.join(str(next((m2/'org/openjfx'/n/'21.0.2').glob('*-linux.jar'))) for n in ['javafx-base','javafx-graphics','javafx-controls'])
cp=':'.join([str(repo/'target/classes'),str(repo/'target/test-classes'),str(m2/'org/postgresql/postgresql/42.7.3/postgresql-42.7.3.jar'),str(out)])
sources=[repo/'docs/evidence/r03-prices/PriceUiCheck.java',repo/'docs/evidence/r08-tables/ReactiveTablesUiCheck.java',repo/'docs/evidence/r10-cleanup/AppStartupUiCheck.java',s/'InstallationDbCheck.java']
with (e/'startup-check.log').open('w') as log:
    subprocess.run([str(java/'javac'),'--module-path',modules,'--add-modules','javafx.controls','-cp',cp,'-d',str(out),*map(str,sources)],env=env,stdout=log,stderr=subprocess.STDOUT,check=True)
    subprocess.run([str(java/'java'),'--module-path',modules,'--add-modules','javafx.controls','-cp',cp,'it.unifi.drivehub.InstallationDbCheck'],env=env,stdout=log,stderr=subprocess.STDOUT,check=True,timeout=30)
    subprocess.run([str(java/'java'),'--module-path',modules,'--add-modules','javafx.controls','-Dprism.order=sw','-cp',cp,'it.unifi.drivehub.AppStartupUiCheck',str(out)],env=env,stdout=log,stderr=subprocess.STDOUT,check=True,timeout=45)
for attempt in (1,2):
    with (e/f'javafx-run-{attempt}.log').open('w') as log:
        process=subprocess.Popen(['mvn','-B','javafx:run'],cwd=repo,env=env,stdout=log,stderr=subprocess.STDOUT,start_new_session=True)
        try:
            deadline=time.monotonic()+45
            while True:
                tree=subprocess.check_output(['xwininfo','-root','-tree'],env=env,text=True)
                match=re.search(r'(0x[0-9a-f]+) "DriveHub - Piattaforma Concessionario Auto"',tree)
                if match:
                    assert process.poll() is None
                    properties=subprocess.check_output(['xprop','-id',match[1],'_NET_WM_PID','WM_CLASS'],env=env,text=True)
                    (e/f'javafx-window-{attempt}.txt').write_text(tree+'\n'+properties)
                    break
                if process.poll() is not None: raise AssertionError('Maven/JavaFX terminato prima della finestra')
                if time.monotonic()>deadline: raise TimeoutError('Finestra JavaFX non osservata')
                time.sleep(.2)
        finally:
            os.killpg(process.pid,signal.SIGTERM)
            try: process.wait(timeout=10)
            except subprocess.TimeoutExpired:
                os.killpg(process.pid,signal.SIGKILL)
                process.wait(timeout=5)
    print(f'javafx:run window observed, attempt {attempt}; process group stopped by harness')
for p in out.glob('*.png'):
    __import__('shutil').copy2(p,e/p.name)
assert 'APP STARTUP SUCCESS' in (e/'startup-check.log').read_text()
print('Startup checks passed; ordinary Maven goal launched twice without schema/demo rerun')
