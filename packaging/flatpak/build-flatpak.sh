#!/usr/bin/env bash
# Script para compilar e instalar MM Generator en Flatpak localmente
set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$PROJECT_DIR"

echo "==> Paso 1: Compilando aplicación con Gradle..."
./gradlew installDist

echo "==> Paso 2: Verificando herramientas de Flatpak..."
if ! command -v flatpak-builder >/dev/null 2>&1; then
    echo "Aviso: 'flatpak-builder' no está instalado."
    echo "Para instalarlo en elementary OS / Ubuntu / Debian:"
    echo "  sudo apt install -y flatpak-builder"
    echo "Para instalar los runtimes necesarios de Freedesktop 24.08 y OpenJDK 21:"
    echo "  flatpak install -y flathub org.freedesktop.Platform//24.08 org.freedesktop.Sdk//24.08 org.freedesktop.Sdk.Extension.openjdk21//24.08"
    exit 1
fi

BUILD_DIR="build/flatpak-build"
REPO_DIR="build/flatpak-repo"
mkdir -p "$BUILD_DIR" "$REPO_DIR"

echo "==> Paso 3: Compilando paquete Flatpak..."
cd packaging/flatpak
flatpak-builder --force-clean --user --install-deps-from=flathub --repo="$PROJECT_DIR/$REPO_DIR" "$PROJECT_DIR/$BUILD_DIR" com.buga.mmgenerator.yaml

echo "==> Paso 4: Creando bundle instalable (.flatpak)..."
flatpak build-bundle "$PROJECT_DIR/$REPO_DIR" "$PROJECT_DIR/build/com.buga.mmgenerator.flatpak" com.buga.mmgenerator 24.08

echo ""
echo "=========================================================="
echo "¡Flatpak compilado con éxito!"
echo "Archivo generado: build/com.buga.mmgenerator.flatpak"
echo "Para instalarlo localmente en tu sistema:"
echo "  flatpak install --user build/com.buga.mmgenerator.flatpak"
echo "Para ejecutarlo:"
echo "  flatpak run com.buga.mmgenerator"
echo "=========================================================="
