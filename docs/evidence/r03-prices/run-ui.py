from pathlib import Path
import subprocess, os, json
root=Path('/tmp/drivehub-ticket05-ui')
repo=Path('/home/federico/dev/Drivehub/DriveHub')
env=os.environ.copy();env.update(json.loads(Path('/tmp/drivehub-r00-env.json').read_text()));env['DISPLAY']=':95'
mods=':'.join(str(next((Path.home()/'.m2/repository/org/openjfx'/name/'21.0.2').glob('*-linux.jar'))) for name in ['javafx-base','javafx-graphics','javafx-controls'])
cp=':'.join([str(repo/'target/classes'),str(repo/'target/test-classes'),str(Path.home()/'.m2/repository/org/postgresql/postgresql/42.7.3/postgresql-42.7.3.jar'),str(root)])
java=Path(env['JAVA_HOME'])/'bin'
with (root/'ui.log').open('w') as log:
 compile=subprocess.run([str(java/'javac'),'--module-path',mods,'--add-modules','javafx.controls','-cp',cp,'-d',str(root),str(root/'PriceUiCheck.java')],env=env,stdout=log,stderr=subprocess.STDOUT)
 if compile.returncode: raise SystemExit(compile.returncode)
 result=subprocess.run([str(java/'java'),'--module-path',mods,'--add-modules','javafx.controls','-Dprism.order=sw','-cp',cp,'it.unifi.drivehub.PriceUiCheck'],env=env,stdout=log,stderr=subprocess.STDOUT,timeout=180)
print((root/'ui.log').read_text())
raise SystemExit(result.returncode)
