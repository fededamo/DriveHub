#!/usr/bin/env bash
# Renders reviewed design sources only. Never extracts UML from Java.
set -euo pipefail
cd "$(dirname "$0")/.."
: "${PLANTUML_JAR:?Set PLANTUML_JAR to the reviewed PlantUML 1.2026.4 jar}"
mkdir -p docs/diagrams/rendered
java -Djava.awt.headless=true -jar "$PLANTUML_JAR" -checkonly -failfast2 docs/diagrams/*.puml
java -Djava.awt.headless=true -jar "$PLANTUML_JAR" -tsvg -failfast2 -o rendered docs/diagrams/*.puml
java -Djava.awt.headless=true -jar "$PLANTUML_JAR" -tpng -failfast2 -o rendered docs/diagrams/*.puml
