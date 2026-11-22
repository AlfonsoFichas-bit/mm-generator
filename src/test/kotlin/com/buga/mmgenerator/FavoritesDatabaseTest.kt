package com.buga.mmgenerator

import com.buga.mmgenerator.service.FavoritesDatabase
import com.buga.mmgenerator.service.TemplateRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class FavoritesDatabaseTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var testDbFile: File

    @BeforeEach
    fun setUp() {
        testDbFile = tempDir.resolve("test_favorites.db").toFile()
        FavoritesDatabase.setCustomUrl("jdbc:sqlite:${testDbFile.absolutePath}")
        TemplateRepository.clearForTesting()
    }

    @AfterEach
    fun tearDown() {
        FavoritesDatabase.clearAllFavorites()
        FavoritesDatabase.setCustomUrl(null)
        TemplateRepository.clearForTesting()
    }

    @Test
    fun `database file and table are initialized correctly`() {
        assertTrue(testDbFile.exists(), "SQLite database file should be created on disk")
        val favorites = FavoritesDatabase.getFavoriteFilenames()
        assertTrue(favorites.isEmpty(), "New database should start with 0 favorites")
    }

    @Test
    fun `adding and removing favorites updates sqlite database correctly`() {
        val filename1 = "rage-troll_face.jpg"
        val filename2 = "potato-01.jpg"

        assertFalse(FavoritesDatabase.isFavorite(filename1))
        assertFalse(FavoritesDatabase.isFavorite(filename2))

        // Mark filename1 as favorite
        FavoritesDatabase.setFavorite(filename1, true, "Troll Face")
        assertTrue(FavoritesDatabase.isFavorite(filename1))
        assertFalse(FavoritesDatabase.isFavorite(filename2))

        var favorites = FavoritesDatabase.getFavoriteFilenames()
        assertEquals(1, favorites.size)
        assertTrue(favorites.contains(filename1))

        // Mark filename2 as favorite
        FavoritesDatabase.setFavorite(filename2, true, "Potato")
        assertTrue(FavoritesDatabase.isFavorite(filename2))

        favorites = FavoritesDatabase.getFavoriteFilenames()
        assertEquals(2, favorites.size)
        assertTrue(favorites.contains(filename1))
        assertTrue(favorites.contains(filename2))

        // Unmark filename1
        FavoritesDatabase.setFavorite(filename1, false)
        assertFalse(FavoritesDatabase.isFavorite(filename1))
        assertTrue(FavoritesDatabase.isFavorite(filename2))

        favorites = FavoritesDatabase.getFavoriteFilenames()
        assertEquals(1, favorites.size)
        assertFalse(favorites.contains(filename1))
        assertTrue(favorites.contains(filename2))
    }

    @Test
    fun `template repository persists favorites across reloads`() {
        // Initial load
        val initialTemplates = TemplateRepository.loadTemplates()
        assertTrue(initialTemplates.isNotEmpty())

        val target = initialTemplates.first()
        assertFalse(target.isFavorite, "Template should not be favorite initially")

        // Toggle favorite via TemplateRepository
        val nowFav = TemplateRepository.toggleFavorite(target)
        assertTrue(nowFav)
        assertTrue(target.isFavorite)
        assertTrue(FavoritesDatabase.isFavorite(target.filename))

        // Simulate application restart by clearing in-memory cache
        // but keeping the SQLite database intact
        TemplateRepository.loadTemplates() // Still cached
        // Clear in-memory templates only (do not clear DB)
        val clearInMemoryMethod = TemplateRepository::class.java.getDeclaredField("allTemplates").apply {
            isAccessible = true
        }
        (clearInMemoryMethod.get(TemplateRepository) as MutableList<*>).clear()

        // Reload templates from JSON and SQLite
        val reloadedTemplates = TemplateRepository.loadTemplates()
        val reloadedTarget = reloadedTemplates.find { it.filename == target.filename }
        assertNotNull(reloadedTarget, "Reloaded template should exist")
        assertTrue(reloadedTarget!!.isFavorite, "Reloaded template should remain marked as favorite from SQLite")

        // Unfavorite and reload again
        TemplateRepository.toggleFavorite(reloadedTarget)
        assertFalse(reloadedTarget.isFavorite)

        // Clear in-memory again
        (clearInMemoryMethod.get(TemplateRepository) as MutableList<*>).clear()

        val secondReload = TemplateRepository.loadTemplates()
        val secondTarget = secondReload.find { it.filename == target.filename }
        assertNotNull(secondTarget)
        assertFalse(secondTarget!!.isFavorite, "Reloaded template should be unfavorited in SQLite")
    }
}
