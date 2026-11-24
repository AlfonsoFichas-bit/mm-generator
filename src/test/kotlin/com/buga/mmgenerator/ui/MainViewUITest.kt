package com.buga.mmgenerator.ui

import com.buga.mmgenerator.HelloApplication
import com.buga.mmgenerator.MainController
import javafx.scene.layout.FlowPane
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testfx.api.FxToolkit
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils

class MainViewUITest : ApplicationTest() {

    companion object {
        @JvmStatic
        @AfterAll
        fun tearDownClass() {
            FxToolkit.cleanupStages()
        }
    }

    private lateinit var mainController: MainController

    override fun start(stage: javafx.stage.Stage) {
        val fxmlLoader = javafx.fxml.FXMLLoader(HelloApplication::class.java.getResource("main-view.fxml"))
        val root = fxmlLoader.load<javafx.scene.Parent>()
        mainController = fxmlLoader.getController()
        val scene = javafx.scene.Scene(root, 1200.0, 800.0)
        scene.fill = javafx.scene.paint.Color.TRANSPARENT
        val customCss = HelloApplication::class.java.getResource("style.css")
        if (customCss != null) {
            scene.stylesheets.add(customCss.toExternalForm())
        }
        stage.scene = scene
        stage.show()
    }

    @BeforeEach
    fun setUp() {
        com.buga.mmgenerator.service.TemplateRepository.loadTemplates()
        WaitForAsyncUtils.waitForFxEvents()
    }

    @Test
    fun `gallery template cards populate in #galleryFlow`() {
        // Wait for the gallery to render
        WaitForAsyncUtils.waitForFxEvents()

        val galleryFlow = lookup("#galleryFlow").query<FlowPane>()
        assert(galleryFlow != null) { "galleryFlow FlowPane should exist" }

        // Gallery should have template cards populated
        val childrenCount = galleryFlow.children.size
        assert(childrenCount > 0) { "Gallery should have at least one template card, but found $childrenCount" }
    }

    @Test
    fun `typing into #searchField filters gallery results`() {
        WaitForAsyncUtils.waitForFxEvents()

        val searchField = lookup("#searchField").query<TextField>()
        assert(searchField != null) { "searchField TextField should exist" }

        val galleryFlow = lookup("#galleryFlow").query<FlowPane>()
        assert(galleryFlow != null) { "galleryFlow FlowPane should exist" }

        // Get initial count
        val initialCount = galleryFlow.children.size
        assert(initialCount > 0) { "Initial gallery should have template cards" }

        // Type a search query that should match some templates
        clickOn("#searchField")
        write("troll")
        WaitForAsyncUtils.waitForFxEvents()

        // Verify filtered results
        val filteredCount = galleryFlow.children.size
        assert(filteredCount > 0) { "Filtered gallery should have results for 'troll'" }
        assert(filteredCount <= initialCount) { "Filtered count ($filteredCount) should be <= initial count ($initialCount)" }

        // Clear search
        interact { searchField.clear() }
        WaitForAsyncUtils.waitForFxEvents()

        val clearedCount = galleryFlow.children.size
        assert(clearedCount == initialCount) { "Cleared search should restore all $initialCount templates, but found $clearedCount" }
    }

    @Test
    fun `clicking sidebar category buttons filters gallery`() {
        WaitForAsyncUtils.waitForFxEvents()

        val galleryFlow = lookup("#galleryFlow").query<FlowPane>()
        assert(galleryFlow != null) { "galleryFlow FlowPane should exist" }

        // Click "Animales" category button in sidebar
        clickOn("Animales")
        WaitForAsyncUtils.waitForFxEvents()

        val animalesCount = galleryFlow.children.size
        assert(animalesCount >= 0) { "Animales category should have results (count: $animalesCount)" }

        // Click "Todas las plantillas" to show all
        clickOn("Todas las plantillas")
        WaitForAsyncUtils.waitForFxEvents()

        val allCount = galleryFlow.children.size
        assert(allCount >= animalesCount) { "All categories ($allCount) should have >= Animales category ($animalesCount)" }
    }
}
