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
import javafx.scene.shape.StrokeType
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import javafx.scene.transform.Transform
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

class ExportTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun initToolkit() {
            try {
                Platform.startup {}
            } catch (_: IllegalStateException) {}
        }
    }

    @Test
    fun testExportDokiAtFullResolutionWithoutBlackBars() {
        val latch = CountDownLatch(1)
        var testSuccess = false
        var failureReason = ""

        Platform.runLater {
            try {
                val dokiSourceFile = File("C:/Users/alfon/IdeaProjects/mm-generator/img-tests/doki.jpg")
                val image = if (dokiSourceFile.exists()) {
                    Image(dokiSourceFile.toURI().toString())
                } else {
                    val templates = TemplateRepository.loadTemplates()
                    TemplateRepository.getImage(templates.first())!!
                }

                val originalWidth = image.width.coerceAtLeast(10.0)
                val originalHeight = image.height.coerceAtLeast(10.0)

                val targetMaxW = 640.0
                val targetMaxH = 480.0
                val scale = minOf(targetMaxW / originalWidth, targetMaxH / originalHeight, 1.0)

                // Exact aspect ratio preserved - NO arbitrary coerceAtLeast breaking the ratio!
                val displayedWidth = originalWidth * scale
                val displayedHeight = originalHeight * scale

                // Clean container without borders/shadows
                val cleanMemeContainer = Pane().apply {
                    prefWidth = displayedWidth
                    prefHeight = displayedHeight
                    minWidth = displayedWidth
                    minHeight = displayedHeight
                    maxWidth = displayedWidth
                    maxHeight = displayedHeight
                    style = "-fx-background-color: transparent; -fx-border-width: 0;"
                }

                val imageView = ImageView(image).apply {
                    fitWidth = displayedWidth
                    fitHeight = displayedHeight
                    isPreserveRatio = true
                    isSmooth = true
                }
                cleanMemeContainer.children.add(imageView)

                // Add bottom text like user wrote
                val bottomText = Text("de momento negamos la\nunion entre la mecanica cuantica y la\nrelatividad general").apply {
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

                val dummyScene = Scene(cleanMemeContainer)

                val exportScale = maxOf(originalWidth / displayedWidth, 1.0)

                val outDir = File("C:/Users/alfon/IdeaProjects/mm-generator/img-tests")

                // Export PNG
                val pngParams = SnapshotParameters().apply {
                    fill = Color.TRANSPARENT
                    transform = Transform.scale(exportScale, exportScale)
                }
                val pngSnapshot = cleanMemeContainer.snapshot(pngParams, null)
                val pngBuffered = SwingFXUtils.fromFXImage(pngSnapshot, null)
                val pngFile = File(outDir, "doki_fixed.png")
                ImageIO.write(pngBuffered, "png", pngFile)

                // Export JPG
                val jpgParams = SnapshotParameters().apply {
                    fill = Color.WHITE
                    transform = Transform.scale(exportScale, exportScale)
                }
                val jpgSnapshot = cleanMemeContainer.snapshot(jpgParams, null)
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
                val jpgFile = File(outDir, "doki_fixed.jpg")
                ImageIO.write(rgbImage, "jpg", jpgFile)

                println("Fixed PNG exported: ${pngBuffered.width} x ${pngBuffered.height} at ${pngFile.absolutePath}")
                println("Fixed JPG exported: ${rgbImage.width} x ${rgbImage.height} at ${jpgFile.absolutePath}")

                assertEquals(originalWidth.toInt(), pngBuffered.width)
                testSuccess = true
            } catch (e: Exception) {
                e.printStackTrace()
                failureReason = e.message ?: "Unknown error"
            } finally {
                latch.countDown()
            }
        }

        latch.await(15, TimeUnit.SECONDS)
        assertTrue(testSuccess, "Export failed: $failureReason")
    }
}
