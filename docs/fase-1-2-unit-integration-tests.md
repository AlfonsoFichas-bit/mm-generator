# Plan de Pruebas: Fases 1 y 2 - Pruebas Unitarias y de Integración Gráfica

Este documento contiene el plan técnico y de ejecución detallado para las fases iniciales del testing de **`mm-generator`**.

---

## 📌 Fase 1: Pruebas Unitarias de Dominio y Lógica de Negocio

El objetivo principal es asegurar la integridad del modelo de datos y la correcta manipulación de plantillas en [`TemplateRepository`](file:///C:/Users/alfon/IdeaProjects/mm-generator/src/main/kotlin/com/buga/mmgenerator/service/TemplateRepository.kt) y [`MemeTemplate`](file:///C:/Users/alfon/IdeaProjects/mm-generator/src/main/kotlin/com/buga/mmgenerator/model/MemeTemplate.kt), sin depender de la inicialización de la interfaz gráfica de JavaFX.

### 1. Componentes a Evaluar
* **Carga de configuración (`loadTemplates`)**:
  * Lectura correcta del archivo `+0A_memetastic.conf.json` tanto desde disco como desde classpath.
  * Deserialización de campos obligatorios: `title`, `filename`, `tags`.
  * Verificación de que no retorne colecciones vacías o nulas.
* **Mecanismo de Categorización (`matchesCategory`)**:
  * `"Todas las plantillas"`: Retorna `true` para cualquier objeto.
  * `"Favoritos"`: Valida el flag booleano `isFavorite`.
  * `"Personalizadas"`: Valida el flag booleano `isCustom`.
  * Reglas semánticas por categoría:
    * `"Rage Comics"`: Palabras clave `rage`, `troll`, `me_gusta`.
    * `"Animales"`: Palabras clave `bear`, `penguin`, `doge`, `potato`.
    * `"Personas"`: Palabras clave `obama`, `chang`, `bean`, `smith`, `maroney`, `kid`.
    * `"Caricaturas"`: Palabras clave `meme`, `sunflower`, `artistic`.
    * `"Otras"`: Exclusión estricta de las categorías previas.
* **Plantillas Personalizadas (`addCustomTemplate`)**:
  * Creación y asignación del archivo en `customFile`.
  * Título derivado de `file.nameWithoutExtension`.
  * Inserción al inicio de la lista de plantillas custom.
* **Cálculos Matemáticos de Geometría y Escala**:
  * Fórmulas de aspect ratio y bounding box (`scale = minOf(targetW / origW, targetH / origH, 1.0)`).
  * Prevención de dimensiones negativas o cero (`coerceAtLeast`).

### 2. Casos de Prueba Propuestos (Unit Tests)

| ID | Caso de Prueba | Entrada | Salida Esperada |
| :--- | :--- | :--- | :--- |
| **UT-01** | Carga de plantillas bundled | Archivo de configuración por defecto | Lista con más de 0 plantillas, títulos no vacíos |
| **UT-02** | Filtro Rage Comics | Plantilla con título "Troll Face" | `matchesCategory(t, "Rage Comics") == true` |
| **UT-03** | Filtro Animales | Plantilla con título "Socially Awkward Penguin" | `matchesCategory(t, "Animales") == true` |
| **UT-04** | Filtro Exclusivo Otras | Plantilla que no encaja en ninguna categoría | `matchesCategory(t, "Otras") == true` |
| **UT-05** | Agregar plantilla custom | Archivo `meme_custom.jpg` | Plantilla con flag `isCustom = true`, título `"meme_custom"` |
| **UT-06** | Cálculo de escala proporcional | Imagen 1920x1080 sobre viewport 640x480 | Escala proporcional exacta sin distorsionar ratio |

---

## 🎨 Fase 2: Pruebas de Integración y Renderizado Gráfico (Exportación)

El objetivo es garantizar la fidelidad visual, la ausencia de deformaciones y la correcta codificación de archivos finales en disco, expandiendo la base de [`ExportTest`](file:///C:/Users/alfon/IdeaProjects/mm-generator/src/test/kotlin/com/buga/mmgenerator/ExportTest.kt).

### 1. Componentes a Evaluar
* **Motor de Renderizado Headless (`Platform.startup`, `SnapshotParameters`)**:
  * Renderizado del contenedor `Pane` con `SwingFXUtils.fromFXImage`.
  * Escalado bidireccional (`exportScale = maxOf(origW / dispW, 1.0)`) para generar resolución completa independientemente del tamaño en pantalla.
* **Formato de Exportación PNG vs JPG**:
  * **PNG**: Comprobar transparencia de fondo cuando corresponda.
  * **JPG**: Conversión a `BufferedImage.TYPE_INT_RGB` con canvas blanco para prevenir artefactos negros producidos por canales alfa ausentes.
* **Diversidad de Ratios de Aspecto**:
  * Formato cuadrado (1:1).
  * Formato apaisado / horizontal (16:9, 4:3).
  * Formato vertical (9:16, 3:4).
  * Asegurar que no existan barras negras accidentales por cálculos erróneos de `prefWidth` / `fitWidth`.
* **Capas de Texto y Tipografías**:
  * Renderizado de texto multicapa (Top, Bottom o flotante).
  * Trazo exterior de fuentes (`StrokeType.OUTSIDE`, `strokeWidth = 2.5`).
  * Comportamiento con textos largos y saltos de línea automáticos (`wrappingWidth`).

### 2. Casos de Prueba Propuestos (Integration Tests)

| ID | Caso de Prueba | Condiciones | Verificación |
| :--- | :--- | :--- | :--- |
| **IT-01** | Exportación PNG nativa | Plantilla de alta resolución con texto | Archivo PNG válido, dimensiones coincidentes con resolución original |
| **IT-02** | Exportación JPG fondo limpio | Imagen con canal alfa o bordes transparentes | Archivo JPG sin artefactos negros de compresión |
| **IT-03** | Preservación de Aspect Ratio | Ratios 1:1, 16:9, 9:16 | Proporción ancho/alto de imagen resultante igual a la original |
| **IT-04** | Wrap de texto multilínea | Texto con 3 o más líneas | El texto no desborda los límites horizontales del contenedor |
| **IT-05** | Carga de fuentes bundled | Tipografía `Impact` y fuentes locales | Renderizado sin fallback silencioso a fuente errónea |

---

## 🛠️ Entorno de Ejecución

* **Framework de pruebas**: JUnit 5 (`org.junit.jupiter:junit-jupiter-api:5.12.1`).
* **Comando para ejecución**:
  ```powershell
  ./gradlew test --tests "com.buga.mmgenerator.TemplateRepositoryTest"
  ./gradlew test --tests "com.buga.mmgenerator.ExportTest"
  ```
