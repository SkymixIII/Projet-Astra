#!/usr/bin/env bash
set -e

cd "$(dirname "$0")"

JFX_LIB="rsrc/javafx-sdk-21.0.5/lib"
MODULES="javafx.controls"
OUT="out"

if ! command -v java >/dev/null 2>&1; then
  echo "Erreur : Java n'est pas installé. Installe un JDK 17+ (ex: brew install openjdk@21)."
  exit 1
fi

mkdir -p "$OUT"

echo "==> Compilation..."
javac --module-path "$JFX_LIB" --add-modules "$MODULES" -d "$OUT" src/*.java

echo "==> Lancement..."
java --module-path "$JFX_LIB" --add-modules "$MODULES" -cp "$OUT" Main
