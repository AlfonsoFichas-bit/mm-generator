package com.buga.mmgenerator.ui

import com.buga.mmgenerator.HelloApplication
import com.buga.mmgenerator.MemesEditorController
import com.buga.mmgenerator.service.TemplateRepository
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.Slider
import javafx.scene.control.TextArea
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.Stage
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testfx.api.FxToolkit
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils

class MemesEditorUITest : ApplicationTest() {

    companion object {
        @JvmStatic
        @AfterAll
        fun tearDownClass() {
            FxToolkit.cleanupStages()
        }
    }

    private lateinit var stage: Stage
    private lateinit var controller: MemesEditorController
    private lateinit var template: com.buga.mmgenerator.model.MemeTemplate

    override fun start(stage: Stage) {
        this.stage = stage
        val fxmlLoader = FXMLLoader(HelloApplication::class.java.getResource("memes-editor.fxml"))
        val root = fxmlLoader.load<Parent>()
        controller = fxmlLoader.getController()
        controller.setStage(stage)

        val templates = TemplateRepository.loadTemplates()
        template = templates.first()
        controller.setTemplate(template)

        val scene = Scene(root, 1200.0, 800.0)
        val customCss = HelloApplication::class.java.getResource("style.css")
        if (customCss != null) {
            scene.stylesheets.add(customCss.toExternalForm())
        }
        stage.scene = scene
        stage.show()
    }

    @BeforeEach
    fun setUp() {
        interact {
            stage.show()
            stage.toFront()
            stage.requestFocus()
            controller.setTemplate(template)
        }
        WaitForAsyncUtils.waitForFxEvents()
    }

    @Test
    fun `classic preset button populates top and bottom text`() {
        WaitForAsyncUtils.waitForFxEvents()

        clickOn("#btnClassicPreset")
        WaitForAsyncUtils.waitForFxEvents()

        val memeContainer = lookup("#memeContainer").query<Pane>()
        assert(memeContainer != null) { "memeContainer Pane should exist" }

        val textWrappers = memeContainer.children.filterIsInstance<StackPane>()
        assert(textWrappers.size == 2) { "Classic preset should create 2 text wrappers, but found ${textWrappers.size}" }

        val texts = textWrappers.mapNotNull { it.children.filterIsInstance<Text>().firstOrNull()?.text }
        assert(texts.any { it.contains("TEXTO SUPERIOR", ignoreCase = true) }) { "Should contain top text" }
        assert(texts.any { it.contains("TEXTO INFERIOR", ignoreCase = true) }) { "Should contain bottom text" }
    }

    @Test
    fun `quick add text, text property update and uppercase toggle`() {
        WaitForAsyncUtils.waitForFxEvents()

        val memeContainer = lookup("#memeContainer").query<Pane>()
        val initialCount = memeContainer.children.filterIsInstance<StackPane>().size

        // Quick add text
        clickOn("#btnQuickAddText")
        WaitForAsyncUtils.waitForFxEvents()

        val newWrappers = memeContainer.children.filterIsInstance<StackPane>()
        assert(newWrappers.size == initialCount + 1) { "Text count should increase by 1" }

        val textContentArea = lookup("#textContentArea").query<TextArea>()
        assert(textContentArea != null) { "textContentArea TextArea should exist" }

        // Update text content
        interact {
            textContentArea.text = "Hello World"
        }
        WaitForAsyncUtils.waitForFxEvents()

        val activeWrapper = newWrappers.last()
        val textNode = activeWrapper.children.filterIsInstance<Text>().first()
        assert(textNode.text == "HELLO WORLD") { "Default uppercase should render 'HELLO WORLD', found '${textNode.text}'" }

        // Toggle uppercase button to lowercase
        val btnUppercase = lookup("#btnUppercase").query<Button>()
        clickOn("#btnUppercase")
        WaitForAsyncUtils.waitForFxEvents()

        assert(textNode.text == "hello world") { "After toggling to lowercase, text should be 'hello world', found '${textNode.text}'" }
        assert(textContentArea.text == "hello world") { "After toggling to lowercase, textContentArea should be 'hello world', found '${textContentArea.text}'" }

        // Toggle back to uppercase
        clickOn("#btnUppercase")
        WaitForAsyncUtils.waitForFxEvents()

        assert(textNode.text == "HELLO WORLD") { "After toggling back to uppercase, text should be 'HELLO WORLD', found '${textNode.text}'" }
        assert(textContentArea.text == "HELLO WORLD") { "After toggling back to uppercase, textContentArea should be 'HELLO WORLD', found '${textContentArea.text}'" }
    }

    @Test
    fun `drag and drop shifts text coordinates within memeContainer`() {
        WaitForAsyncUtils.waitForFxEvents()

        val memeContainer = lookup("#memeContainer").query<Pane>()
        val textWrappers = memeContainer.children.filterIsInstance<StackPane>()
        assert(textWrappers.isNotEmpty()) { "There should be at least one text node to drag" }

        val targetWrapper = textWrappers.first()
        val initialX = targetWrapper.layoutX
        val initialY = targetWrapper.layoutY

        // Perform drag
        moveTo(targetWrapper)
            .press(MouseButton.PRIMARY)
            .moveBy(40.0, 40.0)
            .release(MouseButton.PRIMARY)
        WaitForAsyncUtils.waitForFxEvents()

        val finalX = targetWrapper.layoutX
        val finalY = targetWrapper.layoutY

        assert(finalX != initialX || finalY != initialY) {
            "Text node position should change after drag: initial=($initialX, $initialY), final=($finalX, $finalY)"
        }
    }

    @Test
    fun `zoom in, zoom out, and reset button update zoom and canvas state`() {
        WaitForAsyncUtils.waitForFxEvents()

        val lblZoomPercent = lookup("#lblZoomPercent").query<Label>()
        assert(lblZoomPercent != null) { "lblZoomPercent should exist" }
        assert(lblZoomPercent.text == "100%") { "Initial zoom should be 100%, found ${lblZoomPercent.text}" }

        // Click zoom in
        clickOn("#btnZoomIn")
        WaitForAsyncUtils.waitForFxEvents()
        val zoomedInVal = lblZoomPercent.text.replace("%", "").trim().toInt()
        assert(zoomedInVal > 100) { "Zoom in should be > 100%, found ${lblZoomPercent.text}" }

        // Click zoom out
        clickOn("#btnZoomOut")
        WaitForAsyncUtils.waitForFxEvents()
        val zoomedOutVal = lblZoomPercent.text.replace("%", "").trim().toInt()
        assert(zoomedOutVal <= 100) { "Zoom out should be <= 100%, found ${lblZoomPercent.text}" }

        // Make modifications, then click reset
        clickOn("#btnQuickAddText")
        WaitForAsyncUtils.waitForFxEvents()

        clickOn("#btnReset")
        WaitForAsyncUtils.waitForFxEvents()

        val memeContainer = lookup("#memeContainer").query<Pane>()
        val wrappers = memeContainer.children.filterIsInstance<StackPane>()
        assert(wrappers.size == 2) { "Reset should restore classic preset with 2 text nodes, found ${wrappers.size}" }
    }

    @Test
    fun `top banner preset button switches to top banner style and configures layout`() {
        WaitForAsyncUtils.waitForFxEvents()

        clickOn("#btnTopBannerPreset")
        WaitForAsyncUtils.waitForFxEvents()

        val memeContainer = lookup("#memeContainer").query<Pane>()
        assert(memeContainer != null) { "memeContainer Pane should exist" }

        val imageView = lookup("#imageView").query<ImageView>()
        assert(imageView.layoutY > 0.0) { "ImageView layoutY should be pushed down by banner, but was ${imageView.layoutY}" }

        val bannerOptionsContainer = lookup("#bannerOptionsContainer").query<VBox>()
        assert(bannerOptionsContainer.isVisible) { "bannerOptionsContainer should be visible when top banner is active" }

        val textWrappers = memeContainer.children.filterIsInstance<StackPane>()
        assert(textWrappers.size == 1) { "Top banner preset should create 1 text wrapper, but found ${textWrappers.size}" }

        val textNode = textWrappers.first().children.filterIsInstance<Text>().firstOrNull()
        assert(textNode != null && textNode.text.contains("FRANJA SUPERIOR", ignoreCase = true)) {
            "Banner text should contain 'FRANJA SUPERIOR', found '${textNode?.text}'"
        }

        // Adjust banner height slider
        val sliderBannerHeight = lookup("#sliderBannerHeight").query<Slider>()
        val initialBannerY = imageView.layoutY
        interact {
            sliderBannerHeight.value = 160.0
        }
        WaitForAsyncUtils.waitForFxEvents()
        assert(imageView.layoutY == 160.0) { "ImageView layoutY should update to 160.0, but was ${imageView.layoutY}" }

        // Switch back to Classic preset
        clickOn("#btnClassicPreset")
        WaitForAsyncUtils.waitForFxEvents()

        assert(imageView.layoutY == 0.0) { "Classic preset should reset imageView layoutY to 0.0, but was ${imageView.layoutY}" }
        assert(!bannerOptionsContainer.isVisible) { "bannerOptionsContainer should be hidden for classic preset" }

        val classicWrappers = memeContainer.children.filterIsInstance<StackPane>()
        assert(classicWrappers.size == 2) { "Classic preset should restore 2 text wrappers, found ${classicWrappers.size}" }
    }
}
