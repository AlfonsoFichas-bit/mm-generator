# MM Generator

Creador y editor de memes clásico y moderno. Permite crear memes clásicos con texto superior e inferior en fuente Impact y memes modernos con franja superior configurable, con persistencia local de favoritos mediante SQLite y soporte para plantillas personalizadas.

## Características

- Estilo clásico (Impact) y estilo moderno con franja superior configurable.
- Plantillas clásicas integradas y soporte para importar imágenes personalizadas.
- Persistencia local de favoritos con SQLite (sin telemetría ni anuncios).
- Exportación en alta calidad en PNG y JPG.

## Instalación

### Snap (Linux)

[![Get it from the Snap Store](https://snapcraft.io/en/light/install.svg)](https://snapcraft.io/mm-generator)

```bash
sudo snap install mm-generator
```

### Scoop (Windows)

Si usas Scoop, puedes añadir este repositorio como bucket e instalar la aplicación fácilmente:

```powershell
# 1. Añadir el bucket
scoop bucket add mm-generator https://github.com/AlfonsoFichas-bit/mm-generator.git

# 2. Instalar
scoop install mm-generator
```

O si prefieres instalarla directamente sin agregar el bucket:

```powershell
scoop install https://raw.githubusercontent.com/AlfonsoFichas-bit/mm-generator/master/bucket/mm-generator.json
```

Para actualizar la aplicación a futuras versiones:

```powershell
scoop update mm-generator
```

## Capturas

![Editor principal](https://dashboard.snapcraft.io/site_media/appmedia/2026/11/Captura_de_pantalla_de_2026-10-27_16.23.30-1795528289.png)

![Editor de memes](https://dashboard.snapcraft.io/site_media/appmedia/2026/11/Captura_de_pantalla_de_2026-11-24_09.44.16-1795528289.png)

## Licencia

GPL-3.0-or-later
