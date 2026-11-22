# Plan de Pruebas: Fases 3 y 4 - Pruebas de UI (TestFX), Robustez y CI/CD

Este documento contiene el plan técnico y de configuración para las pruebas de interfaz gráfica (E2E), límites del sistema y automatización en integración continua para **`mm-generator`**.

---

## 🖥️ Fase 3: Pruebas de Interfaz de Usuario (UI) con TestFX

El objetivo es automatizar las interacciones directas del usuario en la interfaz gráfica (JavaFX) tanto en la galería principal ([`MainController`](file:///C:/Users/alfon/IdeaProjects/mm-generator/src/main/kotlin/com/buga/mmgenerator/MainController.kt)) como en el editor interactivo ([`MemesEditorController`](file:///C:/Users/alfon/IdeaProjects/mm-generator/src/main/kotlin/com/buga/mmgenerator/MemesEditorController.kt)).

### 1. Requisitos de Dependencias en `build.gradle.kts`
Para poder ejecutar TestFX con JUnit 5:
```kotlin
val testfxVersion = "4.0.18"

dependencies {
    testImplementation("org.testfx:testfx-core:$testfxVersion")
    testImplementation("org.testfx:testfx-junit5:$testfxVersion")
    testImplementation("org.assertj:assertj-core:3.26.3")
}
```

### 2. Flujos a Probar en la Galería (`MainViewUITest`)
* **Poblado de Galería**:
  * Comprobar que el contenedor `FlowPane` (`#galleryFlow`) contiene nodos de tipo tarjeta para cada plantilla.
* **Búsqueda Reactiva**:
  * Interactuar con `#searchField`, escribir cadenas de búsqueda (ej. `"troll"`) y validar que la cantidad de tarjetas visibles se reduce únicamente a las coincidencias.
* **Navegación por Categorías**:
  * Simular clic en botones de la barra lateral (ej. categoría "Animales").
  * Validar que se actualiza el estilo activo del botón y que la galería refleja el filtro correspondiente.
* **Transición al Editor**:
  * Seleccionar una plantilla y hacer clic para abrir la ventana modal del editor (`#editorRoot`).
  * Verificar que el título de la plantilla en el editor corresponde al elemento seleccionado.

### 3. Flujos a Probar en el Editor (`MemesEditorUITest`)
* **Preset Clásico**:
  * Presionar el botón `#btnClassicPreset`.
  * Validar que se generan automáticamente 2 capas de texto (Top y Bottom) con fuente estilo "Impact".
* **Manipulación de Capas de Texto**:
  * Presionar `#btnQuickAddText` y comprobar que un nuevo nodo `Text` se añade al canvas (`#memeContainer`).
  * Modificar el texto en el campo de edición y verificar la reactividad visual inmediata.
  * Presionar el toggle `#btnUppercase` y comprobar la transformación del texto a mayúsculas.
  * Drag & Drop: Simular el arrastre de una capa de texto y verificar que sus coordenadas (`layoutX`, `layoutY`) se desplazan en la dirección esperada.
* **Controles de Zoom y Viewport**:
  * Probar botones `#btnZoomIn`, `#btnZoomOut` y `#btnZoomReset`.
  * Comprobar que la etiqueta `#lblZoomPercent` actualiza su valor porcentual (ej. `120%`, `100%`).
* **Acción Reset**:
  * Realizar transformaciones en el meme, presionar `#btnReset` y verificar que el estado vuelve a sus valores predeterminados.

---

## 🛡️ Fase 4: Pruebas de Robustez, Rendimiento y Pipeline de CI/CD

El objetivo es garantizar la estabilidad de la aplicación frente a escenarios inesperados, prevenir fugas de memoria y asegurar que todo el conjunto de pruebas pueda ejecutarse de forma desatendida en un pipeline de integración continua (CI).

### 1. Casos Límite y Robustez (Edge Cases)
* **Gestión de Archivos Dañados o Ausentes**:
  * Intentar cargar plantillas personalizadas con rutas inexistentes o archivos corruptos.
  * Confirmar que la aplicación no sufre `Crash` o excepciones no controladas en el hilo de JavaFX (`Platform.runLater`).
* **Textos Extremos e Inyección de Caracteres**:
  * Textos con longitud excesiva (más de 500 caracteres sin espacios).
  * Emojis (`🔥`, `😂`), caracteres con diacríticos (`ñ`, `ü`, acentos) y caracteres Unicode bidireccionales.
* **Nombres de Archivo Especiales en Windows**:
  * Exportar memes con nombres que contengan caracteres restringidos o rutas muy largas.

### 2. Rendimiento y Gestión de Memoria
* **Ciclo de Vida de Imágenes y Garbage Collection**:
  * Abrir y cerrar la vista del editor 20 veces consecutivas.
  * Monitorear el consumo de memoria Heap para confirmar que las instancias de `Image`, `ImageView` y listeners se liberan correctamente sin fugas.

### 3. Automatización en CI/CD (GitHub Actions / Modo Headless)
Para que las pruebas de JavaFX y TestFX corran en máquinas virtuales sin entorno gráfico (servidores Linux / Windows Runner en la nube):

* **Configuración en `build.gradle.kts`**:
  ```kotlin
  tasks.withType<Test> {
      useJUnitPlatform()
      if (project.hasProperty("headless")) {
          systemProperty("testfx.robot", "glass")
          systemProperty("testfx.headless", "true")
          systemProperty("prism.order", "sw")
          systemProperty("prism.text", "t2k")
          systemProperty("java.awt.headless", "true")
      }
  }
  ```

* **Workflow de GitHub Actions (`.github/workflows/test.yml`)**:
  ```yaml
  name: JavaFX & Unit Tests

  on:
    push:
      branches: [ main, master ]
    pull_request:
      branches: [ main, master ]

  jobs:
    test:
      runs-on: ubuntu-latest
      steps:
        - uses: actions/checkout@v4
        - name: Set up JDK 21
          uses: actions/setup-java@v4
          with:
            java-version: '21'
            distribution: 'temurin'
        - name: Run Tests (Headless)
          run: ./gradlew test -Pheadless
  ```

---

## 📊 Matriz de Resumen de Fases

| Fase | Enfoque Principal | Herramientas Clave | Entorno Requerido |
| :--- | :--- | :--- | :--- |
| **Fase 3** | Pruebas de Interfaz y Usabilidad (E2E) | TestFX, JUnit 5, AssertJ | JavaFX Toolkit (Headless o Display) |
| **Fase 4** | Robustez, Límites y CI/CD Automático | JUnit 5, GitHub Actions, JVM Profiler | Headless (Software Rendering / Glass) |
