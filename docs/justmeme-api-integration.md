# Integración de la API de justmeme.wtf

Documento de planificación para consumir la API pública de **justmeme.wtf** y ampliar
las plantillas disponibles en **mm-generator** (proyecto JavaFX/Kotlin con Gradle).

> Estado: planificación (sin implementación todavía).

---

## 1. Resumen del proyecto actual

- App de escritorio JavaFX (Kotlin, JDK 21) que permite crear memes sobre plantillas.
- Las plantillas se cargan desde un JSON local (`assets/bundled/+0A_memetastic.conf.json`)
  y de archivos importados por el usuario.
- `TemplateRepository` (object) centraliza carga, categorías, búsqueda por título/tags e
  imágenes (local → classpath → null).
- `MainController` renderiza la galería, categorías y búsqueda.
- `MemesEditorController` edita un meme a partir de un `MemeTemplate` y exporta PNG/JPG.

Limitación actual: el catálogo de plantillas está fijo al bundle local.

---

## 2. API de justmeme.wtf

- **Base URL:** `https://justmeme.wtf/api/v1`
- **Autenticación:** ninguna.
- **Rate limit:** 60 req/min por IP.
- **CORS:** habilitado.
- **Formato:** JSON con envoltorio `{ success, ... }`.
- **Templates:** 2.400+.

### Endpoints relevantes

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/templates?page=&limit=&category=` | Lista paginada (default 20, max 100) |
| GET | `/templates/search?q=` | Búsqueda por nombre (min 2 chars) |
| GET | `/templates/:slug` | Detalle de una plantilla (URL de imagen, categorías, `page_url`) |
| GET | `/trending` | Top 20 trending (se refresca cada 6 h) |
| GET | `/categories` | Lista de categorías con conteos |
| GET | `/random` | Plantilla aleatoria |
| POST | `/ai-generate` | Genera meme con IA (plantilla + textos) |

### Ejemplo de plantilla

```json
{
  "id": "drake-hotline-bling",
  "name": "Drake Hotline Bling",
  "slug": "drake-hotline-bling",
  "url": "i.imgflip.com/30b1gx.jpg",
  "categories": ["reaction", "comparison"]
}
```

### AI generate

Request:
```json
{ "prompt": "something funny about coding" }
```
Response:
```json
{
  "success": true,
  "template": "drake-hotline-bling",
  "top_text": "Writing tests",
  "bottom_text": "console.log debugging",
  "editor_url": "justmeme.wtf/?template=drake-hotline-bling"
}
```

### Notas importantes

- Las URLs de imagen vienen **sin esquema** (`i.imgflip.com/...`); hay que prefijar `https://`.
- El detalle (`/templates/:slug`) incluye posiciones de cajas de texto; el listado básico no.
- Sin OpenAPI/Swagger oficial; el contrato se infiere de la documentación web.

---

## 3. Objetivo de la integración

Permitir que mm-generator obtenga plantillas desde justmeme.wtf para no limitarse al
bundle local, con:

- Listado paginado y categorías online.
- Búsqueda remota.
- Trending.
- Cache de imágenes en disco.
- Fallback offline.
- (Opcional) Generación con IA.

---

## 4. Diseño propuesto

### 4.1 Modelo de datos (`model/MemeTemplate.kt`)

Extender `MemeTemplate` con campos opcionales:

```kotlin
data class MemeTemplate(
    val title: String,
    val filename: String = "",
    val tags: List<String> = emptyList(),
    var isFavorite: Boolean = false,
    var isCustom: Boolean = false,
    var customFile: File? = null,
    // Nuevos campos para plantillas remotas
    val slug: String? = null,
    val remoteUrl: String? = null,
    val isRemote: Boolean = false,
    val remoteCategories: List<String> = emptyList()
)
```

### 4.2 Servicio HTTP (`service/JustMemeApiService.kt`, nuevo)

- Usar `java.net.http.HttpClient` (JDK 11+, disponible en JDK 21).
- Parseo con Gson (ya en dependencias).
- Métodos que devuelven `Result<T>` o lanzan excepción controlada.
- Normalizar URLs de imagen.
- Manejar códigos HTTP (429 rate limit, 404, 5xx).

### 4.3 Cache de imágenes (`service/ImageCache.kt`, nuevo)

- Directorio: `~/.mm-generator/cache/images/<slug>.<ext>`.
- `getImage(template)`:
  - Local → carga como hoy.
  - Remoto con cache → carga desde archivo.
  - Remoto sin cache → descarga en hilo de fondo, guarda y notifica a la UI.
  - Mientras carga → placeholder / `Image.progress`.

### 4.4 Repositorio (`service/TemplateRepository.kt`)

- Mantener `allTemplates` (locales) y `remoteTemplates` (API).
- Métodos async: `fetchRemoteTemplates(page, limit, category)`, `searchRemote(query)`,
  `fetchTrending()`, `fetchCategories()`, `fetchRandom()`.
- Callbacks/observables para notificar a `MainController`.
- Persistir favoritos remotos por `slug` en `~/.mm-generator/favorites.json`.
- Debounce en búsqueda (≈400 ms) y paginación (50 por página).

### 4.5 UI (`MainController.kt`)

- Categorías dinámicas: mantener actuales + "Trending" + categorías online.
- Búsqueda: si hay texto (≥2 chars) y hay conexión → API; si no → filtro local.
- Paginación: botón "Cargar más" o scroll infinito.
- Indicador de carga y toast de error/offline.
- Al seleccionar plantilla remota, `MemesEditorController.setTemplate` descarga/carga
  la imagen (ya soporta `Image.progress`).

### 4.6 Generación con IA (opcional)

- Botón "Generar con IA" → diálogo con prompt → llama `/ai-generate` →
  descarga plantilla y pre-rellena textos top/bottom en el editor.

---

## 5. Fases de implementación

1. **Fase 1 – Modelo y cliente HTTP**
2. **Fase 2 – Cache de imágenes**
3. **Fase 3 – Repositorio y estado**
4. **Fase 4 – UI**
5. **Fase 5 – Generación con IA (opcional)**
6. **Fase 6 – Tests**

---

## 6. Riesgos y mitigaciones

| Riesgo | Mitigación |
|--------|------------|
| Sin red | Fallback a locales + mensaje; no bloquear UI |
| Rate limit (60 req/min) | Debounce + paginación + cache agresivo |
| Imágenes remotas lentas | Carga en background + placeholder |
| Concurrencia JavaFX | Actualizaciones de UI vía `Platform.runLater` |
| Tests sin internet | Mockear HTTP con fixtures JSON |
| Contrato no documentado formalmente | Tests de parseo defensivos (campos opcionales) |

---

## 7. Archivos a tocar (estimado)

- `model/MemeTemplate.kt`
- `service/TemplateRepository.kt`
- `service/JustMemeApiService.kt` (nuevo)
- `service/ImageCache.kt` (nuevo)
- `MainController.kt`
- `MemesEditorController.kt` (mínimo)
- Tests nuevos + ajustes a `TemplateRepositoryTest.kt`

---

## 8. Decisiones abiertas

1. ¿Incluir generación con IA en el primer alcance o dejarla para después?
2. ¿Paginación con botón "Cargar más" o scroll infinito?
3. ¿Modo 100% offline (solo cache) o mostrar error si no hay red?

---

## 9. Referencias

- API docs: https://justmeme.wtf/api-docs
- Repo: https://github.com/JustMeme-wtf/justmeme-api
- DEV article: https://dev.to/codelong888/i-built-a-free-meme-api-with-2300-templates-on-cloudflare-workers-26l