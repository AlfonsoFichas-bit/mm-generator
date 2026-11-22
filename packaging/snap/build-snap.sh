#!/usr/bin/env bash
# Script para compilar el paquete Snap de MM Generator
set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$PROJECT_DIR"

echo "==> Paso 1: Compilando distribución con Gradle..."
./gradlew installDist

echo "==> Paso 2: Verificando Snapcraft..."
if ! command -v snapcraft >/dev/null 2>&1; then
    echo "Aviso: 'snapcraft' no está instalado en este equipo."
    echo "Para instalarlo en elementary OS / Ubuntu:"
    echo "  sudo snap install snapcraft --classic"
    echo ""
    echo "También puedes subir los cambios a un repositorio de GitHub y habilitar"
    echo "la compilación remota oficial de Canonical mediante:"
    echo "  snapcraft remote-build"
    echo "o usando GitHub Actions con el archivo de snapcraft.yaml."
    exit 1
fi

echo "==> Paso 3: Compilando paquete Snap..."
cd packaging/snap
snapcraft --destructive-mode || snapcraft

echo ""
echo "=========================================================="
echo "¡Snap compilado con éxito!"
echo "Archivo generado: mm-generator_1.0.0_amd64.snap"
echo "Para instalarlo localmente en tu sistema:"
echo "  sudo snap install --dangerous mm-generator_1.0.0_amd64.snap"
echo "Para ejecutarlo:"
echo "  mm-generator"
echo "=========================================================="
