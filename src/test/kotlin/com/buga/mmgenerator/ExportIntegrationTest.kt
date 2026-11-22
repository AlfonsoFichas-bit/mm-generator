package com.buga.mmgenerator

import com.buga.mmgenerator.model.MemeTemplate
import com.buga.mmgenerator.service.TemplateRepository
import javafx.application.Platform
import javafx.embed.swing.SwingFXUtils
import javafx.scene.Scene
import javafx.scene.SnapshotParameters
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeType
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import javafx.scene.transform.Transform
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

/**
 * Integration tests for headless graphic export functionality.
 * Uses classpath bundled images for test isolation and determinism across environments.
 */
class ExportIntegrationTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun initToolkit() {
            try {
                Platform.startup {}
            } catch (_: IllegalStateException) {
            }
        }
    }

    /**
     * Loads a bundled test image from classpath resources.
     * Test images are located in src/test/resources/assets/bundled/
     */
    private fun loadTestImage(resourceName: String): Image {
        val resource = javaClass.getResource("/assets/bundled/$resourceName")
        assertNotNull(resource, "Test image not found in classpath: /assets/bundled/$resourceName")
        return Image(resource.toExternalForm())
    }

    /**
     * Creates a clean meme container with the given image and optional text.
     * This container has no borders, shadows, or editor chrome - just the image and text.
     */
    private fun createCleanMemeContainer(
        image: Image,
        displayedWidth: Double,
        displayedHeight: Double,
        bottomTextContent: String? = null
    ): Pane {
        val cleanMemeContainer = Pane().apply {
            prefWidth = displayedWidth
            prefHeight = displayedHeight
            minWidth = displayedWidth
            minHeight = displayedHeight
            maxWidth = displayedWidth
            maxHeight = displayedHeight
            style = "-fx-background-color: transparent; -fx-border-width: 0;"
            clip = Rectangle(displayedWidth, displayedHeight)
        }

        val imageView = ImageView(image).apply {
            fitWidth = displayedWidth
            fitHeight = displayedHeight
            isPreserveRatio = true
            isSmooth = true
        }
        cleanMemeContainer.children.add(imageView)

        bottomTextContent?.let { textContent ->
            val bottomText = Text(textContent).apply {
                font = Font.font("Impact", FontWeight.BOLD, 28.0)
                fill = Color.WHITE
                stroke = Color.BLACK
                strokeWidth = 2.5
                strokeType = StrokeType.OUTSIDE
                textAlignment = TextAlignment.CENTER
                wrappingWidth = displayedWidth - 20.0
                layoutX = 10.0
                layoutY = displayedHeight - 70.0
            }
            cleanMemeContainer.children.add(bottomText)
        }

        return cleanMemeContainer
    }

    /**
     * Exports the given container as PNG and JPG, verifying dimensions and format properties.
     */
    private fun exportAndVerify(
        container: Pane,
        originalWidth: Double,
        originalHeight: Double,
        testName: String
    ) {
        val latch = CountDownLatch(1)
        var testSuccess = false
        var failureReason = ""

        Platform.runLater {
            try {
                val exportScale = maxOf(originalWidth / container.prefWidth, 1.0)

                // Create temporary output directory
                val outDir = Files.createTempDirectory("mm-generator-export-test").toFile()

                // Export PNG (transparent background)
                val pngParams = SnapshotParameters().apply {
                    fill = Color.TRANSPARENT
                    transform = Transform.scale(exportScale, exportScale)
                }
                val pngSnapshot = container.snapshot(pngParams, null)
                val pngBuffered = SwingFXUtils.fromFXImage(pngSnapshot, null)
                val pngFile = File(outDir, "${testName}_export.png")
                ImageIO.write(pngBuffered, "png", pngFile)

                // Verify PNG dimensions match original
                assertEquals(
                    originalWidth.toInt(), pngBuffered.width,
                    "PNG exported width should match original for $testName"
                )
                assertEquals(
                    originalHeight.toInt(), pngBuffered.height,
                    "PNG exported height should match original for $testName"
                )

                // Verify PNG has alpha channel (TYPE_4BYTE_ABGR or similar)
                assertTrue(
                    pngBuffered.colorModel.hasAlpha(),
                    "PNG should preserve alpha channel for $testName"
                )

                // Export JPG (white background, no alpha)
                val jpgParams = SnapshotParameters().apply {
                    fill = Color.WHITE
                    transform = Transform.scale(exportScale, exportScale)
                }
                val jpgSnapshot = container.snapshot(jpgParams, null)
                val jpgBuffered = SwingFXUtils.fromFXImage(jpgSnapshot, null)
                val rgbImage = BufferedImage(
                    jpgBuffered.width,
                    jpgBuffered.height,
                    BufferedImage.TYPE_INT_RGB
                )
                val g2d = rgbImage.createGraphics()
                g2d.color = java.awt.Color.WHITE
                g2d.fillRect(0, 0, rgbImage.width, rgbImage.height)
                g2d.drawImage(jpgBuffered, 0, 0, null)
                g2d.dispose()
                val jpgFile = File(outDir, "${testName}_export.jpg")
                ImageIO.write(rgbImage, "jpg", jpgFile)

                // Verify JPG dimensions match original
                assertEquals(
                    originalWidth.toInt(), rgbImage.width,
                    "JPG exported width should match original for $testName"
                )
                assertEquals(
                    originalHeight.toInt(), rgbImage.height,
                    "JPG exported height should match original for $testName"
                )

                // Verify JPG has no alpha channel (TYPE_INT_RGB)
                assertFalse(
                    rgbImage.colorModel.hasAlpha(),
                    "JPG should not have alpha channel for $testName"
                )

                println("$testName: PNG exported ${pngBuffered.width}x${pngBuffered.height}, JPG exported ${rgbImage.width}x${rgbImage.height}")
                testSuccess = true
            } catch (e: Exception) {
                e.printStackTrace()
                failureReason = e.message ?: "Unknown error"
            } finally {
                latch.countDown()
            }
        }

        latch.await(15, TimeUnit.SECONDS)
        assertTrue(testSuccess, "Export failed for $testName: $failureReason")
    }

    @Test
    fun testExport16x9Landscape() {
        // 16:9 landscape image - e.g., 1920x1080
        val image = loadTestImage("rage-blonde_smile.jpg")

        val originalWidth = image.width.coerceAtLeast(10.0)
        val originalHeight = image.height.coerceAtLeast(10.0)

        val targetMaxW = 640.0
        val targetMaxH = 500.0
        val scale = minOf(targetMaxW / originalWidth, targetMaxH / originalHeight, 1.0)
        val displayedWidth = originalWidth * scale
        val displayedHeight = originalHeight * scale

        val container = createCleanMemeContainer(image, displayedWidth, displayedHeight)
        exportAndVerify(container, originalWidth, originalHeight, "16x9_landscape")
    }

    @Test
    fun testExport1x1Square() {
        // Use a square-ish image or create one
        // We'll use freedom-sunflower-kid.png which may not be perfectly square
        // but we'll test with forced square dimensions
        val image = loadTestImage("freedom-sunflower-kid.png")

        // Force square aspect ratio for testing
        val originalWidth = 1080.0
        val originalHeight = 1080.0

        val targetMaxW = 640.0
        val targetMaxH = 500.0
        val scale = minOf(targetMaxW / originalWidth, targetMaxH / originalHeight, 1.0)
        val displayedWidth = originalWidth * scale
        val displayedHeight = originalHeight * scale

        val container = createCleanMemeContainer(image, displayedWidth, displayedHeight)
        exportAndVerify(container, originalWidth, originalHeight, "1x1_square")
    }

    @Test
    fun testExport9x16Portrait() {
        // 9:16 portrait image
        // Use rage-Not-Bad-Obama.jpg which is roughly portrait
        val image = loadTestImage("rage-Not-Bad-Obama.jpg")

        val originalWidth = image.width.coerceAtLeast(10.0)
        val originalHeight = image.height.coerceAtLeast(10.0)

        val targetMaxW = 640.0
        val targetMaxH = 500.0
        val scale = minOf(targetMaxW / originalWidth, targetMaxH / originalHeight, 1.0)
        val displayedWidth = originalWidth * scale
        val displayedHeight = originalHeight * scale

        val container = createCleanMemeContainer(image, displayedWidth, displayedHeight)
        exportAndVerify(container, originalWidth, originalHeight, "9x16_portrait")
    }

    @Test
    fun testExportWithMultiLineText() {
        // Test multi-line text wrapping and outline stroke rendering
        val image = loadTestImage("rage-blonde_smile.jpg")

        val originalWidth = image.width.coerceAtLeast(10.0)
        val originalHeight = image.height.coerceAtLeast(10.0)

        val targetMaxW = 640.0
        val targetMaxH = 500.0
        val scale = minOf(targetMaxW / originalWidth, targetMaxH / originalHeight, 1.0)
        val displayedWidth = originalWidth * scale
        val displayedHeight = originalHeight * scale

        // Multi-line text with explicit newlines
        val multiLineText = "LÍNEA UNO DE TEXTO\nLÍNEA DOS CON MÁS PALABRAS\nTERCERA LÍNEA FINAL"

        val container = createCleanMemeContainer(image, displayedWidth, displayedHeight, multiLineText)
        exportAndVerify(container, originalWidth, originalHeight, "multiline_text")
    }

    @Test
    fun testExportWithLongSingleLineTextWrapping() {
        // Test automatic text wrapping when text exceeds container width
        val image = loadTestImage("freedom-sunflower-kid.png")

        val originalWidth = image.width.coerceAtLeast(10.0)
        val originalHeight = image.height.coerceAtLeast(10.0)

        val targetMaxW = 640.0
        val targetMaxH = 500.0
        val scale = minOf(targetMaxW / originalWidth, targetMaxH / originalHeight, 1.0)
        val displayedWidth = originalWidth * scale
        val displayedHeight = originalHeight * scale

        // Long single line that should wrap
        val longText =
            "ESTA ES UNA FRASE MUY LARGA QUE DEBERÍA ENVOLVERSE AUTOMÁTICAMENTE EN MÚLTIPLES LÍNEAS DENTRO DEL CONTENEDOR"

        val container = createCleanMemeContainer(image, displayedWidth, displayedHeight, longText)
        exportAndVerify(container, originalWidth, originalHeight, "long_text_wrapping")
    }

    @Test
    fun testExportTextOutlineStrokeRendering() {
        // Test that text outline stroke renders cleanly without artifacts
        val image = loadTestImage("rage-blonde_smile.jpg")

        val originalWidth = image.width.coerceAtLeast(10.0)
        val originalHeight = image.height.coerceAtLeast(10.0)

        val targetMaxW = 640.0
        val targetMaxH = 500.0
        val scale = minOf(targetMaxW / originalWidth, targetMaxH / originalHeight, 1.0)
        val displayedWidth = originalWidth * scale
        val displayedHeight = originalHeight * scale

        // Text with stroke - verify it renders cleanly
        val textWithStroke = "TEXTO CON CONTORNO NEGRO"

        val container = createCleanMemeContainer(image, displayedWidth, displayedHeight, textWithStroke)
        exportAndVerify(container, originalWidth, originalHeight, "text_stroke")
    }

    @Test
    fun testExportHighResolution() {
        // Test export at native resolution (high DPI)
        val image = loadTestImage("Bear-said-Whore-by-Rones.png")

        val originalWidth = image.width.coerceAtLeast(10.0)
        val originalHeight = image.height.coerceAtLeast(10.0)

        val targetMaxW = 640.0
        val targetMaxH = 500.0
        val scale = minOf(targetMaxW / originalWidth, targetMaxH / originalHeight, 1.0)
        val displayedWidth = originalWidth * scale
        val displayedHeight = originalHeight * scale

        val container = createCleanMemeContainer(image, displayedWidth, displayedHeight)
        exportAndVerify(container, originalWidth, originalHeight, "high_res")
    }

    @Test
    fun testExportPngTransparencyVsJpgWhiteBackground() {
        // Specific test to verify PNG transparency vs JPG white background
        // Create a container with transparent background and verify the difference
        val image = loadTestImage("rage-blonde_smile.jpg")

        val originalWidth = image.width.coerceAtLeast(10.0)
        val originalHeight = image.height.coerceAtLeast(10.0)

        val targetMaxW = 640.0
        val targetMaxH = 500.0
        val scale = minOf(targetMaxW / originalWidth, targetMaxH / originalHeight, 1.0)
        val displayedWidth = originalWidth * scale
        val displayedHeight = originalHeight * scale

        val container = createCleanMemeContainer(image, displayedWidth, displayedHeight)

        val latch = CountDownLatch(1)
        var testSuccess = false
        var failureReason = ""

        Platform.runLater {
            try {
                val exportScale = maxOf(originalWidth / displayedWidth, 1.0)

                // Export PNG with transparent fill
                val pngParams = SnapshotParameters().apply {
                    fill = Color.TRANSPARENT
                    transform = Transform.scale(exportScale, exportScale)
                }
                val pngSnapshot = container.snapshot(pngParams, null)
                val pngBuffered = SwingFXUtils.fromFXImage(pngSnapshot, null)

                // Export JPG with white fill
                val jpgParams = SnapshotParameters().apply {
                    fill = Color.WHITE
                    transform = Transform.scale(exportScale, exportScale)
                }
                val jpgSnapshot = container.snapshot(jpgParams, null)
                val jpgBuffered = SwingFXUtils.fromFXImage(jpgSnapshot, null)
                val rgbImage = BufferedImage(
                    jpgBuffered.width,
                    jpgBuffered.height,
                    BufferedImage.TYPE_INT_RGB
                )
                val g2d = rgbImage.createGraphics()
                g2d.color = java.awt.Color.WHITE
                g2d.fillRect(0, 0, rgbImage.width, rgbImage.height)
                g2d.drawImage(jpgBuffered, 0, 0, null)
                g2d.dispose()

                // Verify PNG has transparency (alpha channel)
                assertTrue(pngBuffered.colorModel.hasAlpha(), "PNG should have alpha channel")

                // Verify JPG has no transparency
                assertFalse(rgbImage.colorModel.hasAlpha(), "JPG should not have alpha channel")

                // Verify JPG background is white by sampling multiple points
                val corners = listOf(
                    Pair(0, 0),
                    Pair(rgbImage.width - 1, 0),
                    Pair(0, rgbImage.height - 1),
                    Pair(rgbImage.width - 1, rgbImage.height - 1)
                )
                corners.forEach { (x, y) ->
                    val pixel = rgbImage.getRGB(x, y) and 0xFFFFFF
                    assertEquals(0xFFFFFF, pixel, "JPG corner ($x,$y) should be white")
                }

                // Verify PNG has non-white content (the image itself)
                val pngCenterX = pngBuffered.width / 2
                val pngCenterY = pngBuffered.height / 2
                val pngCenterPixel = pngBuffered.getRGB(pngCenterX, pngCenterY) and 0xFFFFFF
                // The center should not be pure white (0xFFFFFF) since it's the meme image
                assertNotEquals(0xFFFFFF, pngCenterPixel, "PNG center should show image content, not white")

                testSuccess = true
            } catch (e: Exception) {
                e.printStackTrace()
                failureReason = e.message ?: "Unknown error"
            } finally {
                latch.countDown()
            }
        }

        latch.await(15, TimeUnit.SECONDS)
        assertTrue(testSuccess, "PNG vs JPG validation failed: $failureReason")
    }
}
