"""Riscontra la compilazione separata dello ZIP, senza avviarla di nuovo."""
from pathlib import Path
import hashlib
import json
import re
import shutil
import sys
import zipfile

import pymupdf

ROOT = Path(__file__).resolve().parents[4]
OUT = Path(__file__).resolve().parent
extracted = Path(sys.argv[1]).resolve()
toolchain = Path(sys.argv[2]).resolve()
package = OUT / "sorgenti-relazione.zip"
with zipfile.ZipFile(package) as archive:
    for name in archive.namelist():
        assert archive.read(name) == (extracted / name).read_bytes(), name
original = pymupdf.open(ROOT / "docs/relazione/relazione-drivehub.pdf")
rebuilt = pymupdf.open(extracted / "build/main.pdf")
assert len(original) == len(rebuilt)
for first, second in zip(original, rebuilt):
    assert first.get_text() == second.get_text()
    assert first.get_pixmap(dpi=96).samples == second.get_pixmap(dpi=96).samples

inputs = sorted(set(line[6:] for line in (extracted / "build/main.fls").read_text().splitlines() if line.startswith("INPUT ")))
local, external = [], []
for name in inputs:
    path = Path(name)
    path = (extracted / path).resolve() if not path.is_absolute() else path.resolve()
    if path.is_relative_to(extracted):
        local.append(str(path.relative_to(extracted)))
    else:
        assert path.is_relative_to(toolchain) or path.is_relative_to("/usr/share/fonts") or path.is_relative_to("/var/cache/fontconfig"), path
        external.append(str(path))

log = (extracted / "build/main.log").read_text()
warnings = [line for line in log.splitlines() if re.search(r"Warning|Overfull|Underfull|Missing character|undefined|ignored error", line)]
# I messaggi richiedono valutazione manuale, registrata in valutazione-messaggi-tex.md.
shutil.copy2(extracted / "build/main.fls", OUT / "pacchetto-input.fls")
shutil.copy2(extracted / "build/main.log", OUT / "pacchetto-latex-log.txt")
result = {
    "zip_sha256": hashlib.sha256(package.read_bytes()).hexdigest(),
    "extraction": str(extracted), "pages": len(original),
    "all_pages_text_equal": True, "all_pages_pixels_equal_dpi": 96,
    "local_inputs": local, "toolchain_inputs": external,
    "warnings_last_pass": warnings,
    "pdf_extracted_sha256": hashlib.sha256((extracted / "build/main.pdf").read_bytes()).hexdigest(),
}
(OUT / "autonomia-pacchetto.json").write_text(json.dumps(result, indent=2) + "\n")
print(f"{len(original)} pagine identiche nel testo e nei pixel a 96 dpi; {len(local)} input locali; {len(external)} risorse della toolchain; {len(warnings)} messaggi finali da valutare: {warnings}")
