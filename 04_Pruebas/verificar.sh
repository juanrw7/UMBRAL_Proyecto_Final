#!/usr/bin/env bash
set -euo pipefail
: "${N2T_TOOLS:?Define N2T_TOOLS con la ruta al directorio tools de Nand2Tetris}"
tools=$(cd "$N2T_TOOLS" && pwd)
project=$(cd "$(dirname "$0")/.." && pwd)
tmp=$(mktemp -d)
trap 'rm -rf -- "$tmp"' EXIT
mkdir -p "$tmp"/{programa,classes}
cp "$project"/02_Ejecutable_VM/*.vm "$tmp/programa/"
if [[ $# == 0 ]]; then set -- logic; fi
mode=${1:-logic}
classpath="$tools/bin/classes:$tools/bin/lib/*:$tmp/classes"
javaflags=()
if [[ $mode == builtin || $mode == *Swing ]]; then
  classpath="$classpath:$tools"
else
  cp "$tools"/OS/*.vm "$tmp/programa/"
fi
if [[ $mode != *Swing && $mode != builtin ]]; then javaflags+=(-Djava.awt.headless=true); fi
javac -cp "$classpath" -d "$tmp/classes" "$project/04_Pruebas/VerificarUmbral.java"
output=${UMBRAL_EVIDENCIAS:-"$project/04_Pruebas/salida_local"}
mkdir -p "$output"
output=$(cd "$output" && pwd)
(
  cd "$tools"
  java -Xmx192m "${javaflags[@]}" -cp "$classpath" VerificarUmbral "$tmp/programa" "$output" "$@"
)
