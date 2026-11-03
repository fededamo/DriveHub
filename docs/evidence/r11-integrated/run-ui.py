"""Riesegue gli harness esistenti sulla copia privata della baseline18."""
from pathlib import Path
import difflib
import hashlib
import json
import shutil
import subprocess
import sys

session = Path('/tmp/drivehub-ticket18-session')
repo = session / 'install/DriveHub'
evidence = Path(__file__).resolve().parent
env = json.loads((session / 'env.json').read_text())
name = sys.argv[1]
phases = {
    'AgreementUiCheck': ['seed', 'check'],
    'SaleOutcomeUiCheck': ['seed', 'check'],
    'DestinationUiCheck': ['seed', 'check'],
    'IdentityUiCheck': ['seed', 'check'],
    'TableLifecycleUiCheck': ['check'],
    'SlowModelsUiCheck': ['check'],
    'AppStartupUiCheck': ['startup'],
}
assert name in phases
out = session / 'ui' / name
out.mkdir(parents=True, exist_ok=False)
sources = [repo / 'docs/evidence' / p for p in [
    'r03-prices/PriceUiCheck.java', 'r04-agreement/AgreementUiCheck.java',
    'r05-sale-outcome/SaleOutcomeUiCheck.java', 'r06-destination/DestinationUiCheck.java',
    'r07-identity/IdentityUiCheck.java', 'r08-tables/ReactiveTablesUiCheck.java',
    'r08-tables/SlowModelsUiCheck.java', 'r08-tables/TableLifecycleUiCheck.java',
    'r10-cleanup/AppStartupUiCheck.java',
]]
inputs = {}
patch = []
derived = out / 'sources'
derived.mkdir()
for source in sources:
    content = source.read_text()
    # I primi harness hanno percorsi output fissi; nessun assert viene cambiato.
    content = content.replace('/tmp/drivehub-ticket05-ui/', str(out) + '/')
    content = content.replace('/tmp/drivehub-ticket06-ui/', str(out) + '/')
    if source.name == 'SaleOutcomeUiCheck.java':
        # Il finally storico può mascherare l'errore originale chiudendo un Alert modale.
        content = content.replace('} finally {',
                                  '} catch (Throwable error) { error.printStackTrace(); throw error; } finally {')
    if source.name == 'DestinationUiCheck.java':
        # Il catalogo14 espone righe preparate; il vecchio harness12 castava Vehicle.
        content = content.replace('TableView<Vehicle>',
                                  'TableView<it.unifi.drivehub.service.CatalogService.VehicleRow>')
        content = content.replace('table.getItems().get(i).getId()',
                                  'table.getItems().get(i).vehicle().getId()')
        content = content.replace('table.getSelectionModel().getSelectedItem().getId()',
                                  'table.getSelectionModel().getSelectedItem().vehicle().getId()')
    target = derived / source.name
    target.write_text(content)
    inputs[str(source.relative_to(repo))] = {
        'original_sha256': hashlib.sha256(source.read_bytes()).hexdigest(),
        'executed_sha256': hashlib.sha256(target.read_bytes()).hexdigest(),
    }
    patch.extend(difflib.unified_diff(source.read_text().splitlines(keepends=True),
                                     content.splitlines(keepends=True),
                                     fromfile=str(source.relative_to(repo)), tofile=source.name))
(evidence / (name + '-inputs.json')).write_text(json.dumps(inputs, indent=2) + '\n')
(evidence / (name + '-harness.patch')).write_text(''.join(patch))
modules = ':'.join(str(next((Path.home() / '.m2/repository/org/openjfx' / module / '21.0.2').glob('*-linux.jar')))
                   for module in ['javafx-base', 'javafx-graphics', 'javafx-controls'])
classpath = ':'.join([str(repo / 'target/classes'), str(repo / 'target/test-classes'),
                      str(Path.home() / '.m2/repository/org/postgresql/postgresql/42.7.3/postgresql-42.7.3.jar'), str(out)])
java = Path(env['JAVA_HOME']) / 'bin'
with (evidence / (name + '.log')).open('w') as log:
    def run(command):
        log.write('COMMAND: ' + ' '.join(map(str, command)) + '\n')
        log.flush()
        subprocess.run(command, env=env, cwd=repo, stdout=log, stderr=subprocess.STDOUT,
                       check=True, timeout=240)
    run([str(java / 'javac'), '--module-path', modules, '--add-modules', 'javafx.controls',
         '-cp', classpath, '-d', str(out), *map(str, sorted(derived.glob('*.java')))])
    for phase in phases[name]:
        args = [str(out)] if phase == 'startup' else [phase, str(out)]
        run([str(java / 'java'), '--module-path', modules, '--add-modules', 'javafx.controls',
             '-Dprism.order=sw', '-cp', classpath, 'it.unifi.drivehub.' + name, *args])
marker = 'APP STARTUP SUCCESS' if name == 'AppStartupUiCheck' else 'UI CHECK SUCCESS'
assert marker in (evidence / (name + '.log')).read_text()
for screenshot in out.glob('*.png'):
    shutil.copy2(screenshot, evidence / (name + '-' + screenshot.name))
print(name, marker)
