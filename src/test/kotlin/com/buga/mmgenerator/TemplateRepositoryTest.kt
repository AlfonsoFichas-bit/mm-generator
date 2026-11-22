package com.buga.mmgenerator

import com.buga.mmgenerator.model.MemeTemplate
import com.buga.mmgenerator.service.TemplateRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files

class TemplateRepositoryTest {

    @BeforeEach
    fun setUp() {
        // Clear repository state before each test to ensure isolation
        TemplateRepository.clearForTesting()
    }

    @Test
    fun testBundledTemplateDeserialization() {
        val templates = TemplateRepository.loadTemplates()

        assertNotNull(templates, "Template list should not be null")
        assertTrue(templates.isNotEmpty(), "Should load at least one bundled template")

        // Verify structure of loaded templates
        val firstTemplate = templates.first()
        assertNotNull(firstTemplate.title, "Template should have a title")
        assertNotNull(firstTemplate.filename, "Template should have a filename")
        assertFalse(firstTemplate.title.isBlank(), "Title should not be blank")
        assertFalse(firstTemplate.filename.isBlank(), "Filename should not be blank")
    }

    @Test
    fun testBundledTemplatesHaveExpectedFields() {
        val templates = TemplateRepository.loadTemplates()

        // Check that templates have the expected fields from JSON
        templates.forEach { template ->
            assertNotNull(template.title)
            assertNotNull(template.filename)
            assertNotNull(template.tags)
            assertFalse(template.isCustom, "Bundled templates should not be marked as custom")
            assertFalse(template.isFavorite, "Bundled templates should not be favorite by default")
            assertNull(template.customFile, "Bundled templates should not have custom file")
        }
    }

    @Test
    fun testCategoryMatchingRageComics() {
        val templates = TemplateRepository.loadTemplates()

        // Find templates that should match Rage Comics category
        val rageTemplates = templates.filter { TemplateRepository.matchesCategory(it, "Rage Comics") }
        assertTrue(rageTemplates.isNotEmpty(), "Should find at least one Rage Comics template")

        // Verify specific known rage comics match
        val knownRageTemplates = templates.filter { it.title.lowercase().contains("rage") }
        knownRageTemplates.forEach { template ->
            assertTrue(
                TemplateRepository.matchesCategory(template, "Rage Comics"),
                "Template '${template.title}' should match Rage Comics category"
            )
        }

        // Templates with "troll" should match
        val trollTemplates = templates.filter { it.title.lowercase().contains("troll") }
        trollTemplates.forEach { template ->
            assertTrue(
                TemplateRepository.matchesCategory(template, "Rage Comics"),
                "Template '${template.title}' should match Rage Comics category"
            )
        }

        // Templates with "me_gusta" should match
        val meGustaTemplates = templates.filter { it.title.lowercase().contains("me_gusta") }
        meGustaTemplates.forEach { template ->
            assertTrue(
                TemplateRepository.matchesCategory(template, "Rage Comics"),
                "Template '${template.title}' should match Rage Comics category"
            )
        }
    }

    @Test
    fun testCategoryMatchingAnimales() {
        val templates = TemplateRepository.loadTemplates()

        val animalTemplates = templates.filter { TemplateRepository.matchesCategory(it, "Animales") }
        assertTrue(animalTemplates.isNotEmpty(), "Should find at least one Animales template")

        // Templates with bear, penguin, doge, potato should match
        val keywords = listOf("bear", "penguin", "doge", "potato")
        keywords.forEach { keyword ->
            val matching = templates.filter { it.title.lowercase().contains(keyword) }
            matching.forEach { template ->
                assertTrue(
                    TemplateRepository.matchesCategory(template, "Animales"),
                    "Template '${template.title}' with keyword '$keyword' should match Animales category"
                )
            }
        }
    }

    @Test
    fun testCategoryMatchingPersonas() {
        val templates = TemplateRepository.loadTemplates()

        val personaTemplates = templates.filter { TemplateRepository.matchesCategory(it, "Personas") }
        assertTrue(personaTemplates.isNotEmpty(), "Should find at least one Personas template")

        val keywords = listOf("obama", "chang", "bean", "smith", "maroney", "kid")
        keywords.forEach { keyword ->
            val matching = templates.filter { it.title.lowercase().contains(keyword) }
            matching.forEach { template ->
                assertTrue(
                    TemplateRepository.matchesCategory(template, "Personas"),
                    "Template '${template.title}' with keyword '$keyword' should match Personas category"
                )
            }
        }
    }

    @Test
    fun testCategoryMatchingCaricaturas() {
        val templates = TemplateRepository.loadTemplates()

        val caricaturaTemplates = templates.filter { TemplateRepository.matchesCategory(it, "Caricaturas") }
        assertTrue(caricaturaTemplates.isNotEmpty(), "Should find at least one Caricaturas template")

        val keywords = listOf("meme", "sunflower", "artistic")
        keywords.forEach { keyword ->
            val matching = templates.filter { it.title.lowercase().contains(keyword) }
            matching.forEach { template ->
                assertTrue(
                    TemplateRepository.matchesCategory(template, "Caricaturas"),
                    "Template '${template.title}' with keyword '$keyword' should match Caricaturas category"
                )
            }
        }
    }

    @Test
    fun testCategoryMatchingOtras() {
        val templates = TemplateRepository.loadTemplates()

        // Test the "Otras" logic with a template that doesn't match any specific category
        // (since all bundled templates match at least one category, we create a test template)
        val otrasTemplate = MemeTemplate(
            title = "random-unrelated-template",
            filename = "random.jpg",
            tags = emptyList(),
            isFavorite = false,
            isCustom = false
        )

        assertTrue(
            TemplateRepository.matchesCategory(otrasTemplate, "Otras"),
            "Template not matching any specific category should match Otras"
        )

        // Verify it doesn't match the specific categories
        assertFalse(TemplateRepository.matchesCategory(otrasTemplate, "Rage Comics"))
        assertFalse(TemplateRepository.matchesCategory(otrasTemplate, "Animales"))
        assertFalse(TemplateRepository.matchesCategory(otrasTemplate, "Personas"))
        assertFalse(TemplateRepository.matchesCategory(otrasTemplate, "Caricaturas"))
        assertFalse(TemplateRepository.matchesCategory(otrasTemplate, "Personalizadas"))
        assertFalse(TemplateRepository.matchesCategory(otrasTemplate, "Favoritos"))

        // But should match Todas las plantillas
        assertTrue(TemplateRepository.matchesCategory(otrasTemplate, "Todas las plantillas"))

        // Also verify that custom templates don't match Otras (they have their own category)
        val customTemplate = MemeTemplate(
            title = "custom-template",
            filename = "custom.jpg",
            tags = listOf("custom"),
            isCustom = true
        )
        assertFalse(
            TemplateRepository.matchesCategory(customTemplate, "Otras"),
            "Custom templates should not match Otras"
        )
    }

    @Test
    fun testCategoryMatchingFavoritos() {
        val templates = TemplateRepository.loadTemplates()

        // By default, no templates should be favorites
        val favoriteTemplates = templates.filter { TemplateRepository.matchesCategory(it, "Favoritos") }
        assertTrue(favoriteTemplates.isEmpty(), "No templates should be favorites by default")

        // Mark one as favorite and verify
        val template = templates.first()
        template.isFavorite = true
        assertTrue(
            TemplateRepository.matchesCategory(template, "Favoritos"),
            "Template marked as favorite should match Favoritos category"
        )

        // Reset for other tests
        template.isFavorite = false
    }

    @Test
    fun testCategoryMatchingTodasLasPlantillas() {
        val templates = TemplateRepository.loadTemplates()

        val allTemplates = templates.filter { TemplateRepository.matchesCategory(it, "Todas las plantillas") }
        assertEquals(templates.size, allTemplates.size, "All templates should match 'Todas las plantillas'")
    }

    @Test
    fun testCategoryMatchingPersonalizadas() {
        val templates = TemplateRepository.loadTemplates()

        // By default, no bundled templates should be custom
        val customTemplates = templates.filter { TemplateRepository.matchesCategory(it, "Personalizadas") }
        val bundledCustom = customTemplates.filter { !it.isCustom }
        assertTrue(bundledCustom.isEmpty(), "Bundled templates should not match Personalizadas")
    }

    @Test
    fun testCustomTemplateIngestion() {
        // Create a temporary test image file
        val tempDir = Files.createTempDirectory("mm-generator-test").toFile()
        val testImageFile = File(tempDir, "custom-test-image.jpg")
        testImageFile.createNewFile()

        try {
            val customTemplate = TemplateRepository.addCustomTemplate(testImageFile)

            assertNotNull(customTemplate, "Should return the created custom template")
            assertEquals("custom-test-image", customTemplate.title, "Title should be filename without extension")
            assertEquals("custom-test-image.jpg", customTemplate.filename, "Filename should be preserved")
            assertTrue(customTemplate.isCustom, "Custom template should have isCustom = true")
            assertNotNull(customTemplate.customFile, "Custom template should have customFile set")
            assertEquals(testImageFile, customTemplate.customFile, "customFile should reference the original file")
            assertEquals(listOf("custom"), customTemplate.tags, "Custom template should have 'custom' tag")

            // Verify it's added to the custom templates list (prepended to customTemplates)
            val allTemplates = TemplateRepository.loadTemplates()
            assertTrue(allTemplates.contains(customTemplate), "Custom template should be in the combined list")

            // Verify it's at the front of the customTemplates portion (after all bundled templates)
            val customTemplatesList = allTemplates.filter { it.isCustom }
            assertEquals(
                customTemplate,
                customTemplatesList.first(),
                "Custom template should be first in custom templates list"
            )
        } finally {
            // Cleanup
            testImageFile.delete()
            tempDir.delete()
        }
    }

    @Test
    fun testCustomTemplateCategoryFiltering() {
        val tempDir = Files.createTempDirectory("mm-generator-test").toFile()
        val testImageFile = File(tempDir, "another-custom.jpg")
        testImageFile.createNewFile()

        try {
            val customTemplate = TemplateRepository.addCustomTemplate(testImageFile)

            // Custom template should match Personalizadas
            assertTrue(
                TemplateRepository.matchesCategory(customTemplate, "Personalizadas"),
                "Custom template should match Personalizadas category"
            )

            // Custom template should NOT match other specific categories
            assertFalse(TemplateRepository.matchesCategory(customTemplate, "Rage Comics"))
            assertFalse(TemplateRepository.matchesCategory(customTemplate, "Animales"))
            assertFalse(TemplateRepository.matchesCategory(customTemplate, "Personas"))
            assertFalse(TemplateRepository.matchesCategory(customTemplate, "Caricaturas"))
            assertFalse(TemplateRepository.matchesCategory(customTemplate, "Otras"))

            // But should match Todas las plantillas
            assertTrue(TemplateRepository.matchesCategory(customTemplate, "Todas las plantillas"))
        } finally {
            testImageFile.delete()
            tempDir.delete()
        }
    }

    @Test
    fun testCustomTemplateFileNaming() {
        val tempDir = Files.createTempDirectory("mm-generator-test").toFile()

        // Test with various file extensions
        val testFiles = listOf(
            "meme.png",
            "meme.jpg",
            "meme.jpeg",
            "meme.gif",
            "meme.webp"
        )

        testFiles.forEach { fileName ->
            val testImageFile = File(tempDir, fileName)
            testImageFile.createNewFile()

            try {
                val customTemplate = TemplateRepository.addCustomTemplate(testImageFile)
                val expectedTitle = fileName.substringBeforeLast('.')
                assertEquals(
                    expectedTitle, customTemplate.title,
                    "Title should be filename without extension for $fileName"
                )
                assertEquals(
                    fileName, customTemplate.filename,
                    "Filename should be preserved for $fileName"
                )
            } finally {
                testImageFile.delete()
            }
        }

        tempDir.delete()
    }
}
