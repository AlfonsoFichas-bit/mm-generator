# Guía de Empaquetado y Distribución: Flatpak y Snap

Esta guía explica cómo compilar, probar localmente y publicar **MM Generator** (`com.buga.mmgenerator`) en **Flathub** y en la **Snap Store**.

---

## 1. Estructura de Empaquetado

```
packaging/
├── build-all.sh                     # Script maestro de validación y preparación
├── common/                          # Metadatos e iconos compartidos
│   ├── com.buga.mmgenerator.desktop # Archivo de lanzador XDG
│   ├── com.buga.mmgenerator.metainfo.xml # Metadatos AppStream
│   ├── com.buga.mmgenerator.svg     # Icono vectorial escalable
│   ├── com.buga.mmgenerator.png     # Icono 512×512
│   ├── com.buga.mmgenerator.ico     # Icono multi-resolución
│   └── icons/hicolor/...            # Árbol estándar de resoluciones (16px a 512px)
├── flatpak/
│   ├── com.buga.mmgenerator.yaml    # Manifiesto de Flatpak
│   ├── mm-generator-wrapper.sh      # Script de inicio en el sandbox
│   └── build-flatpak.sh             # Script de compilación local
└── snap/
    ├── snapcraft.yaml               # Manifiesto de Snapcraft (core24 + GNOME)
    └── build-snap.sh                # Script de compilación local
```

---

## 2. Flatpak (Flathub / Distribución Universal)

### Requisitos previos en el sistema
Para compilar localmente con `flatpak-builder`:
```bash
sudo apt install -y flatpak-builder
flatpak install -y flathub org.freedesktop.Platform//24.08 org.freedesktop.Sdk//24.08 org.freedesktop.Sdk.Extension.openjdk21//24.08
```

### Compilación local
Ejecuta el script automatizado:
```bash
./packaging/flatpak/build-flatpak.sh
```

El script genera el paquete `build/com.buga.mmgenerator.flatpak`.

### Instalación y ejecución local
```bash
flatpak install --user build/com.buga.mmgenerator.flatpak
flatpak run com.buga.mmgenerator
```

### Publicación en Flathub
1. Haz un fork del repositorio oficial de Flathub: `https://github.com/flathub/flathub`.
2. Crea una rama `new-pr/com.buga.mmgenerator`.
3. Añade el manifiesto [packaging/flatpak/com.buga.mmgenerator.yaml](file:///home/rook/IdeaProjects/mm-generator/packaging/flatpak/com.buga.mmgenerator.yaml) adaptando las fuentes a los releases de GitHub / tarball publicado.
4. Abre un Pull Request en Flathub. El bot de Flathub compilará y probará automáticamente la aplicación para arquitecturas `x86_64` y `aarch64`.

---

## 3. Snap (Ubuntu / Snap Store)

### Requisitos previos en el sistema
```bash
sudo snap install snapcraft --classic
```

### Compilación local
```bash
./packaging/snap/build-snap.sh
```
O ejecutando directamente Snapcraft:
```bash
cd packaging/snap
snapcraft
```

### Instalación y prueba local
```bash
sudo snap install --dangerous mm-generator_1.0.0_amd64.snap
mm-generator
```

### Publicación en la Snap Store
1. Registra el nombre de la aplicación:
   ```bash
   snapcraft login
   snapcraft register mm-generator
   ```
2. Sube y publica el archivo compilado:
   ```bash
   snapcraft upload --release=stable mm-generator_1.0.0_amd64.snap
   ```
3. O conecta tu repositorio de GitHub directamente en [snapcraft.io](https://snapcraft.io) para compilaciones automáticas en cada push o tag de release.

---

## 4. Pruebas de Calidad en el Sandbox

Al probar tanto en Flatpak como en Snap, verifica los siguientes puntos clave:
* **Persistencia de Favoritos:** Al marcar plantillas como favoritas y reiniciar la app, los datos deben persistir (en Flatpak dentro de `~/.var/app/com.buga.mmgenerator/` y en Snap dentro de `~/snap/mm-generator/current/`).
* **Acceso al Sistema de Archivos:** Abre una imagen personalizada desde cualquier carpeta de tu usuario (`~/Imágenes`, `~/Descargas`, etc.) y exporta un meme creado.
* **Integración Visual:** La barra de título y el lanzador deben mostrar el icono oficial de la Meme Card con el fondo MemeLines.
