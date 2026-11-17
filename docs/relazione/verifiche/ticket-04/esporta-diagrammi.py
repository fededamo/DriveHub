"""Esporta solo le tavole del ticket 04 con draw.io Desktop, senza installazioni."""
from pathlib import Path
import datetime
import json
import os
import shlex
import subprocess
import sys

HERE = Path(__file__).resolve().parent
DIAGRAMS = HERE.parents[1] / "diagrammi"
BINARY = os.environ.get("T04_DRAWIO", "/tmp/drivehub-ticket04-tools/drawio/opt/drawio/drawio")
PROFILE = os.environ.get("T04_DRAWIO_PROFILE", "/tmp/drivehub-ticket04-profile")
repair = r'''
const fs = require("fs"), file = process.argv[1];
let xml = fs.readFileSync(file, "utf8");
const diagram = xml.match(/<mxfile[\s\S]*?<\/mxfile>|<mxGraphModel[\s\S]*?<\/mxGraphModel>/);
if (diagram && xml.trim() !== diagram[0]) xml = diagram[0];
xml = xml.replace(/="([^"]*)"/g, function(m, v) {
  return "=\"" + v.replace(/</g, "&lt;").replace(/&(?!(?:amp|lt|gt|quot|apos|#[0-9]+|#x[0-9a-fA-F]+);)/g, "&amp;") + "\"";
});
xml = xml.replace(/<mxCell id="([^"]+)" parent="\1"\s*\/>/g, "<mxCell id=\"$1\" parent=\"0\"/>");
fs.writeFileSync(file, xml);
'''
names = json.loads((HERE / "tavole.json").read_text())
if sys.argv[1:]:
    assert set(sys.argv[1:]) <= set(names)
    names = sys.argv[1:]
for name in names:
    source = DIAGRAMS / (name + ".drawio")
    subprocess.run(["node", "-e", repair, str(source)], check=True)
    command = [BINARY, "--disable-gpu", "--user-data-dir=" + PROFILE,
               "-x", "-f", "pdf", "-e", "--crop", "-b", "10",
               "-o", str(source) + ".pdf", str(source)]
    result = subprocess.run(command, capture_output=True, text=True, timeout=55)
    (HERE / (name + "-esportazione.txt")).write_text(
        datetime.datetime.now(datetime.timezone.utc).isoformat() + "\n"
        + shlex.join(command) + "\n" + result.stdout + result.stderr
        + f"\nexit: {result.returncode}\n")
    print(name, "exit", result.returncode, flush=True)
    assert result.returncode == 0, result.stderr
