# MM Generator — Guía de Identidad y Uso del Logo

Guía de estilo y especificación técnica para la marca y los activos visuales de **MM Generator (Creador de Memes)**.

---

## 1. El Concepto y el Símbolo
* **Concepto:** *Meme Card (Edición MemeLines)*. Representa la tarjeta de un meme con su franja superior de texto (*top banner*) y un lienzo inferior con el icónico fondo radial de rayos (*sunburst*) de la era dorada de los memes (*Advice Animals / memelines-yellow-orange.png*), enmarcando una mirada con guiño y sonrisa cómplice.
* **Formatos maestros:**
  * Símbolo principal a todo color: `branding/dist/mm-generator-symbol.svg`
  * Versión monocolor negro: `branding/dist/mm-generator-symbol-mono.svg`
  * Versión blanca invertida: `branding/dist/mm-generator-symbol-white.svg`
  * Versión reducida (16/32 px): `branding/dist/mm-generator-symbol-small.svg`
  * Lockup horizontal: `branding/dist/mm-generator-horizontal.svg`

---

## 2. Área de Seguridad y Margen
Mantener siempre un área de respeto mínima equivalente a la altura de la **franja superior del meme** ($1\times h_{\text{franja}}$) alrededor del isotipo para garantizar que no compita visualmente con otros textos o elementos de la interfaz.

---

## 3. Paleta de Color Oficial

| Elemento | Nombre | HEX | RGB | Uso |
| :--- | :--- | :--- | :--- | :--- |
| **Primario Cálido** | *Meme Orange* | `#E86C2D` | `rgb(232, 108, 45)` | Fondo base de rayos y acento en títulos |
| **Rayo Solar** | *Meme Yellow* | `#FCD34D` | `rgb(252, 211, 77)` | Rayos radiales de energía pop |
| **Franja Superior** | *Header Slate* | `#27272A` | `rgb(39, 39, 42)` | Bloque superior del meme |
| **Rostro / Tinta** | *Impact Charcoal* | `#18181B` | `rgb(24, 24, 27)` | Expresión (guiño/sonrisa) y tipografía |
| **Luz / Reversa** | *Pure White* | `#FFFFFF` | `rgb(255, 255, 255)` | Modo oscuro y contraste en fondos oscuros |

---

## 4. Tamaños Mínimos Recomendados

| Aplicación | Tamaño Mínimo | Archivo Recomendado |
| :--- | :--- | :--- |
| **Favicon / Barra de título** | 16 × 16 px | `mm-generator-symbol-small.svg` / `com.buga.mmgenerator.ico` |
| **Icono de sistema (Linux)** | 48 × 48 px a 512 × 512 px | `packaging/common/icons/hicolor/.../com.buga.mmgenerator.png` |
| **Lockup horizontal (Header)** | 180 × 52 px | `branding/dist/mm-generator-horizontal.svg` |

---

## 5. Tipografía Oficial
* **Tipografía de Marca (Wordmark):** Sans-serif geométrica pesada (*Inter Display / Roboto / Segoe UI Black*).
* **Tipografía de Memes (dentro de la app):** *Impact* (clásico meme) e *Inter/Adwaita* (franja superior).

---

## 6. Reglas de Uso (Qué NO hacer)
* **No deformar:** Mantener siempre la relación de aspecto 1:1 en el isotipo.
* **No alterar los rayos:** Los 16 rayos radiales están calibrados con precisión angular.
* **No usar colores no autorizados:** Evitar gradientes artificiales no aprobados o efectos de bisel 3D.
* **No saturar:** En fondos complejos o fotografías, utilizar la versión monocolor dentro del contenedor de tarjeta o sobre una base sólida.
