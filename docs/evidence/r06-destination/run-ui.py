from pathlib import Path
import os
import subprocess

repo = Path(__file__).resolve().parents[3]
out = Path(os.environ.get("DRIVEHUB_UI_DIR", "/tmp/drivehub-ticket12-session/ui"))
out.mkdir(parents=True, exist_ok=True)
env = os.environ.copy()
assert env.get("DISPLAY"), "Impostare DISPLAY su un display di test isolato"
java = Path(env["JAVA_HOME"]) / "bin"
modules = ":".join(str(next((Path.home() / ".m2/repository/org/openjfx" / name / "21.0.2").glob("*-linux.jar")))
                   for name in ["javafx-base", "javafx-graphics", "javafx-controls"])
classpath = ":".join([str(repo / "target/classes"), str(repo / "target/test-classes"),
                      str(Path.home() / ".m2/repository/org/postgresql/postgresql/42.7.3/postgresql-42.7.3.jar"), str(out)])
sources = [repo / "docs/evidence" / folder / name for folder, name in [
    ("r03-prices", "PriceUiCheck.java"), ("r04-agreement", "AgreementUiCheck.java"),
    ("r05-sale-outcome", "SaleOutcomeUiCheck.java"), ("r06-destination", "DestinationUiCheck.java")]]
with (out / "ui.log").open("w") as log:
    subprocess.run([str(java / "javac"), "--module-path", modules, "--add-modules", "javafx.controls",
                    "-cp", classpath, "-d", str(out), *map(str, sources)], env=env, stdout=log, stderr=subprocess.STDOUT, check=True)
    for phase in ["seed", "check"]:
        subprocess.run([str(java / "java"), "--module-path", modules, "--add-modules", "javafx.controls",
                        "-Dprism.order=sw", "-cp", classpath, "it.unifi.drivehub.DestinationUiCheck", phase, str(out)],
                       env=env, stdout=log, stderr=subprocess.STDOUT, timeout=180, check=True)
assert "UI CHECK SUCCESS" in (out / "ui.log").read_text()
print("UI CHECK SUCCESS; log e schermate:", out)
