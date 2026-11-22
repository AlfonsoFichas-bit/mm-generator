package com.buga.mmgenerator.service

import com.buga.mmgenerator.model.MemeTemplate
import com.google.gson.Gson
import com.google.gson.JsonObject
import javafx.scene.image.Image
import java.io.File
import java.io.InputStreamReader

object TemplateRepository {
    private val allTemplates = mutableListOf<MemeTemplate>()
    private val customTemplates = mutableListOf<MemeTemplate>()

    fun loadTemplates(): List<MemeTemplate> {
        if (allTemplates.isNotEmpty()) {
            return allTemplates + customTemplates
        }

        try {
            // First attempt to read from local assets/bundled directory
            val localConf = File("assets/bundled/+0A_memetastic.conf.json")
            val jsonString = if (localConf.exists()) {
                localConf.readText(Charsets.UTF_8)
            } else {
                val resourceStream = javaClass.getResourceAsStream("/assets/bundled/+0A_memetastic.conf.json")
                resourceStream?.use { InputStreamReader(it, Charsets.UTF_8).readText() } ?: ""
            }

            if (jsonString.isNotBlank()) {
                val gson = Gson()
                val root = gson.fromJson(jsonString, JsonObject::class.java)
                val imagesArray = root.getAsJsonArray("images")

                imagesArray?.forEach { element ->
                    val obj = element.asJsonObject
                    val title = obj.get("title")?.asString ?: "Meme"
                    val filename = obj.get("filename")?.asString ?: ""
                    val tags = mutableListOf<String>()
                    obj.getAsJsonArray("tags")?.forEach { tagElem ->
                        tags.add(tagElem.asString)
                    }

                    allTemplates.add(
                        MemeTemplate(
                            title = title,
                            filename = filename,
                            tags = tags
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return allTemplates + customTemplates
    }

    fun addCustomTemplate(file: File): MemeTemplate {
        val template = MemeTemplate(
            title = file.nameWithoutExtension,
            filename = file.name,
            tags = listOf("custom"),
            isCustom = true,
            customFile = file
        )
        customTemplates.add(0, template)
        return template
    }

    fun getImage(template: MemeTemplate, backgroundLoading: Boolean = false): Image? {
        if (template.isCustom && template.customFile != null && template.customFile!!.exists()) {
            return try {
                Image(template.customFile!!.toURI().toString(), backgroundLoading)
            } catch (e: Exception) {
                null
            }
        }

        // Check local disk first
        val localFile = File("assets/bundled/${template.filename}")
        if (localFile.exists()) {
            return Image(localFile.toURI().toString(), backgroundLoading)
        }

        // Try classpath resource
        val resource = javaClass.getResource("/assets/bundled/${template.filename}")
        if (resource != null) {
            return Image(resource.toExternalForm(), backgroundLoading)
        }

        return null
    }

    fun matchesCategory(template: MemeTemplate, category: String): Boolean {
        return when (category) {
            "Todas las plantillas" -> true
            "Favoritos" -> template.isFavorite
            "Personalizadas" -> template.isCustom
            "Rage Comics" -> {
                val lower = template.title.lowercase()
                lower.startsWith("rage") || lower.contains("troll") || lower.contains("me_gusta")
            }
            "Animales" -> {
                val lower = template.title.lowercase()
                lower.contains("bear") || lower.contains("penguin") || lower.contains("doge") || lower.contains("potato")
            }
            "Personas" -> {
                val lower = template.title.lowercase()
                lower.contains("obama") || lower.contains("chang") || lower.contains("bean") ||
                        lower.contains("smith") || lower.contains("maroney") || lower.contains("kid")
            }
            "Caricaturas" -> {
                val lower = template.title.lowercase()
                lower.contains("meme") || lower.contains("sunflower") || lower.contains("artistic")
            }
            "Otras" -> {
                !matchesCategory(template, "Rage Comics") &&
                        !matchesCategory(template, "Animales") &&
                        !matchesCategory(template, "Personas") &&
                        !matchesCategory(template, "Caricaturas")
            }
            else -> true
        }
    }
}
