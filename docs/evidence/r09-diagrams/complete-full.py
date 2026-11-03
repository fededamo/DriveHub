from pathlib import Path
import subprocess
s=Path("/tmp/drivehub-ticket17-session")
r=subprocess.run(["python3",str(s/"run-maven.py"),"full","-Ppostgres-tests","clean","test"])
(s/"full-exit.txt").write_text(str(r.returncode)+"\n")
