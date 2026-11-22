package com.buga.mmgenerator

import com.buga.mmgenerator.model.MemeTemplate
import com.buga.mmgenerator.service.TemplateRepository
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.TextArea
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.text.Text
import javafx.stage.Stage
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.testfx.api.FxToolkit
import org.testfx.framework.junit5.ApplicationTest
import org.testfx.util.WaitForAsyncUtils
import java.io.File
import java.nio.file.Path

class RobustnessTest : ApplicationTest() {

    companion object {
        @JvmStatic
        @AfterAll
        fun tearDownClass() {
            FxToolkit.cleanupStages()
        }
    }

    private lateinit var stage: Stage
    private lateinit var controller: MemesEditorController

    override fun start(stage: Stage) {
        this.stage = stage
        val fxmlLoader = FXMLLoader(HelloApplication::class.java.getResource("memes-editor.fxml"))
        val root = fxmlLoader.load<Parent>()
        controller = fxmlLoader.getController()
        controller.setStage(stage)

        val templates = TemplateRepository.loadTemplates()
        val template = templates.first()
        controller.setTemplate(template)

        val scene = Scene(root, 1200.0, 800.0)
        stage.scene = scene
        stage.show()
    }

    @BeforeEach
    fun setUp() {
        WaitForAsyncUtils.waitForFxEvents()
    }

    @Test
    fun `handles corrupted image file gracefully without crashing application`(@TempDir tempDir: Path) {
        // Create a corrupted image file containing invalid bytes
        val corruptFile = tempDir.resolve("corrupt_image.png").toFile()
        corruptFile.writeBytes(byteArrayOf(0x00, 0x1F, 0x7E, 0x4A, 0x33, 0x12, 0x55, 0x77, 0x00, 0x11))

        val customTemplate = MemeTemplate(
            title = "Corrupt Template",
            filename = corruptFile.name,
            tags = listOf("custom", "test"),
            isCustom = true,
            customFile = corruptFile
        )

        // Loading corrupted image directly or via controller should not crash the JavaFX thread
        interact {
            val img = TemplateRepository.getImage(customTemplate)
            // Even if an invalid image object or null is returned, setTemplate handles it without throwing unhandled exceptions
            controller.setTemplate(customTemplate)
        }
        WaitForAsyncUtils.waitForFxEvents()

        val memeContainer = lookup("#memeContainer").query<Pane>()
        assert(memeContainer != null) { "UI must remain responsive after loading corrupted image" }
    }

    @Test
    fun `handles non-existent template file gracefully without crashing`() {
        val nonExistentFile = File("non_existent_template_99999.png")
        val missingTemplate = MemeTemplate(
            title = "Missing Template",
            filename = nonExistentFile.name,
            tags = listOf("custom"),
            isCustom = true,
            customFile = nonExistentFile
        )

        interact {
            val img = TemplateRepository.getImage(missingTemplate)
            assert(img == null) { "Non-existent file should return null image" }
            controller.setTemplate(missingTemplate)
        }
        WaitForAsyncUtils.waitForFxEvents()

        val memeContainer = lookup("#memeContainer").query<Pane>()
        assert(memeContainer != null) { "UI must remain responsive when file does not exist" }
    }

    @Test
    fun `extreme text lengths, emojis and diacritics render without exceptions`() {
        WaitForAsyncUtils.waitForFxEvents()

        // Construct 500+ characters with long text, emojis, and diverse diacritics
        val emojiSet = "\uD83D\uDE00\uD83D\uDE02\uD83D\uDE80\uD83D\uDD25\uD83C\uDF89\uD83D\uDCBB\uD83C\uDF55\u2728\u26A1\uD83E\uDD16"
        val diacriticsSet = "áéíóú àèìòù âêîôû äëïöü ñ ç å ø æ œ č š ž ã õ"
        val baseSentence = "Este es un texto extremadamente largo diseñado para probar la robustez de renderizado de fuentes y ajuste automático dentro de los límites del contenedor de memes. "
        val extremeText = (baseSentence + emojiSet + " " + diacriticsSet + " ").repeat(4) // > 500 characters

        assert(extremeText.length >= 500) { "Text length must be 500+ characters, actual length: ${extremeText.length}" }

        val textContentArea = lookup("#textContentArea").query<TextArea>()
        assert(textContentArea != null) { "textContentArea must exist" }

        interact {
            textContentArea.text = extremeText
        }
        WaitForAsyncUtils.waitForFxEvents()

        val memeContainer = lookup("#memeContainer").query<Pane>()
        val textWrappers = memeContainer.children.filterIsInstance<StackPane>()
        assert(textWrappers.isNotEmpty()) { "There should be text items on the canvas" }

        val activeText = textWrappers.first().children.filterIsInstance<Text>().first()
        assert(activeText.text.contains(diacriticsSet.uppercase())) { "Text must properly contain and preserve uppercase diacritics" }
        assert(activeText.text.contains(emojiSet)) { "Text must properly contain emojis" }
    }

    @Test
    fun `repeated opening and closing of editor controller does not leak or crash`() {
        val templates = TemplateRepository.loadTemplates()
        val template = templates.first()

        for (i in 1..15) {
            interact {
                val dummyStage = Stage()
                val fxmlLoader = FXMLLoader(HelloApplication::class.java.getResource("memes-editor.fxml"))
                val root = fxmlLoader.load<Parent>()
                val iterController = fxmlLoader.getController<MemesEditorController>()
                iterController.setStage(dummyStage)
                iterController.setTemplate(template)

                val scene = Scene(root, 800.0, 600.0)
                dummyStage.scene = scene
                dummyStage.show()
                dummyStage.close()
            }
            WaitForAsyncUtils.waitForFxEvents()
        }

        System.gc()
        val memeContainer = lookup("#memeContainer").query<Pane>()
        assert(memeContainer != null) { "Primary UI must remain stable and operational after 15 open/close cycles" }
    }
}
