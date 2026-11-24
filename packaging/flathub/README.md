# Publicación en Flathub (Flatpak)

Este directorio contiene los archivos listos para enviar **MM Generator** (`com.buga.mmgenerator`) a **Flathub**.

---

## Archivos para Flathub

- `com.buga.mmgenerator.yaml`: Manifiesto principal de Flatpak configurado con Runtime Freedesktop 24.08, OpenJDK 21 y sandbox con permisos de escritorio.
- `com.buga.mmgenerator.desktop`: Lanzador XDG para escritorios Linux (GNOME, KDE, Pantheon, etc.).
- `com.buga.mmgenerator.metainfo.xml`: Metadatos AppStream validados para centros de software (GNOME Software, AppCenter, KDE Discover).
- `com.buga.mmgenerator.svg`: Icono vectorial escalable oficial de la Meme Card.
- `mm-generator-wrapper.sh`: Script de inicialización dentro del entorno aislado `/app/`.

---

## Paso 1: Crear el Release v1.0.0 en GitHub

Flathub requiere que el tarball de la aplicación esté publicado en los Releases de GitHub:

1. Ve a tu repositorio en GitHub: [Releases](https://github.com/AlfonsoFichas-bit/mm-generator/releases) > **Draft a new release**.
2. **Tag version:** `v1.0.0` (crear nuevo tag sobre `master`).
3. **Release title:** `MM Generator v1.0.0`
4. **Description:**
   ```markdown
   Primera versión estable de MM Generator.
   - Soporte para memes clásicos (Impact) y modernos (franja superior).
   - Biblioteca de 49 plantillas clásicas y soporte para imágenes personalizadas.
   - Persistencia local de favoritos con SQLite.
   - Exportación en alta calidad en formatos PNG y JPG.
   ```
5. **Attach binaries by dropping them here:**
   Adjunta el archivo generado:
   `build/distributions/mm-generator-1.0.0.tar.gz`
   *(SHA-256: `2968cfdc2d4d664da2d4091ed9c9680ea54cb1691d7e68b6499555f5546ea02a`)*.
6. Haz clic en **Publish release**.

---

## Paso 2: Abrir el Pull Request en Flathub

1. Ve al repositorio oficial de envíos de Flathub: [https://github.com/flathub/flathub](https://github.com/flathub/flathub).
2. Haz clic en el botón **Fork** (arriba a la derecha) para crear tu copia personal.
3. Clona tu fork o usa la interfaz web de GitHub para crear una nueva rama:
   - Nombre de rama: `add-com.buga.mmgenerator`
4. Copia los archivos de este directorio (`packaging/flathub/*`) en la raíz de tu rama en el fork.
5. Haz commit de los archivos:
   ```bash
   git add com.buga.mmgenerator.yaml com.buga.mmgenerator.desktop com.buga.mmgenerator.metainfo.xml com.buga.mmgenerator.svg mm-generator-wrapper.sh
   git commit -m "Add com.buga.mmgenerator"
   git push origin add-com.buga.mmgenerator
   ```
6. Ve a [https://github.com/flathub/flathub/pulls](https://github.com/flathub/flathub/pulls) y haz clic en **New pull request**:
   - **Base repository:** `flathub/flathub` (rama `new-pr`)
   - **Head repository:** `tu-usuario/flathub` (rama `add-com.buga.mmgenerator`)
   - **Título del PR:** `Add com.buga.mmgenerator`
7. El bot de Flathub (`flathubbot`) compilará automáticamente la aplicación para arquitecturas `x86_64` y `aarch64` y publicará un enlace de prueba para verificarla. Una vez revisada por los moderadores, se aprobará y se publicará en [flathub.org](https://flathub.org).
