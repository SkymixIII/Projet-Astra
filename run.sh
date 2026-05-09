#!/usr/bin/env bash
set -e

cd "$(dirname "$0")"

JFX_LIB=$(echo rsrc/javafx-sdk-21.0.*/lib | awk '{print $1}')
MODULES="javafx.controls"
OUT="out"

if ! command -v java >/dev/null 2>&1; then
  echo "Erreur : Java n'est pas installé. Installe un JDK 17+ (ex: brew install openjdk@21)."
  exit 1
fi

mkdir -p "$OUT"

echo "==> Compilation..."
# Pour l'instant on ne compile que la partie visuelle ; le reste du repo
# (jeu, batiments, etc.) référence encore des classes en chantier qui
# bloquent javac. À élargir quand ces classes seront en place.
javac --module-path "$JFX_LIB" --add-modules "$MODULES" -d "$OUT" \
    src/Main.java src/RenduCarte.java src/GestionInputs.java


echo "==> Lancement..."
java --module-path "$JFX_LIB" --add-modules "$MODULES" -cp "$OUT" Main
