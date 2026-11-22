#!/usr/bin/env bash
# Script maestro para preparar distribuciones de Linux (Flatpak y Snap)
set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_DIR"

echo "=========================================================="
echo "          MM Generator - Sistema de Empaquetado           "
echo "=========================================================="

echo "==> 1. Compilando y empaquetando binarios con Gradle..."
./gradlew installDist

echo "==> 2. Verificando activos comunes de escritorio..."
test -f packaging/common/com.buga.mmgenerator.desktop || { echo "Error: falta .desktop"; exit 1; }
test -f packaging/common/com.buga.mmgenerator.metainfo.xml || { echo "Error: falta metainfo.xml"; exit 1; }
test -f packaging/common/com.buga.mmgenerator.svg || { echo "Error: falta icono SVG"; exit 1; }
test -f packaging/common/com.buga.mmgenerator.png || { echo "Error: falta icono PNG 512px"; exit 1; }

echo "==> Activos validados correctamente."
echo ""
echo "Instrucciones de compilación:"
echo " - Para compilar Flatpak: ./packaging/flatpak/build-flatpak.sh"
echo " - Para compilar Snap:    ./packaging/snap/build-snap.sh"
echo ""
echo "Consulta docs/packaging.md para la guía completa de publicación en Flathub y Snap Store."
