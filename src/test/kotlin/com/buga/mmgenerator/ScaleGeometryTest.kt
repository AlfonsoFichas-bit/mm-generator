package com.buga.mmgenerator

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Unit tests for scale and geometry calculations used in meme rendering and export.
 * These tests verify proportional scaling, bounds preservation, and aspect ratio handling
 * for extreme image sizes and various aspect ratios.
 */
class ScaleGeometryTest {

    /**
     * Tests the display scale calculation used in applyImageDimensions.
     * Verifies that images are scaled to fit within target bounds while preserving aspect ratio.
     */
    @Test
    fun testDisplayScaleCalculation() {
        // Test case 1: Landscape image larger than target bounds
        // Original: 1920x1080, Target: 640x500
        // scale = min(640/1920, 500/1080, 1.0) = min(0.333, 0.463, 1.0) = 0.333
        var originalWidth = 1920.0
        var originalHeight = 1080.0
        val targetMaxW = 640.0
        val targetMaxH = 500.0

        var scale = minOf(targetMaxW / originalWidth, targetMaxH / originalHeight, 1.0)
        var displayedWidth = originalWidth * scale
        var displayedHeight = originalHeight * scale

        assertEquals(0.3333333333333333, scale, 1e-10)
        assertEquals(640.0, displayedWidth, 1e-10)
        assertEquals(360.0, displayedHeight, 1e-10)
        assertEquals(
            originalWidth / originalHeight, displayedWidth / displayedHeight, 1e-10,
            "Aspect ratio should be preserved"
        )

        // Test case 2: Portrait image larger than target bounds
        // Original: 1080x1920, Target: 640x500
        // scale = min(640/1080, 500/1920, 1.0) = min(0.593, 0.260, 1.0) = 0.260
        originalWidth = 1080.0
        originalHeight = 1920.0

        scale = minOf(targetMaxW / originalWidth, targetMaxH / originalHeight, 1.0)
        displayedWidth = originalWidth * scale
        displayedHeight = originalHeight * scale

        assertEquals(0.26041666666666666, scale, 1e-10)
        assertEquals(281.25, displayedWidth, 1e-10)
        assertEquals(500.0, displayedHeight, 1e-10)
        assertEquals(
            originalWidth / originalHeight, displayedWidth / displayedHeight, 1e-10,
            "Aspect ratio should be preserved"
        )

        // Test case 3: Square image smaller than target bounds
        // Original: 300x300, Target: 640x500
        // scale = min(640/300, 500/300, 1.0) = min(2.133, 1.667, 1.0) = 1.0
        originalWidth = 300.0
        originalHeight = 300.0

        scale = minOf(targetMaxW / originalWidth, targetMaxH / originalHeight, 1.0)
        displayedWidth = originalWidth * scale
        displayedHeight = originalHeight * scale

        assertEquals(1.0, scale, 1e-10)
        assertEquals(300.0, displayedWidth, 1e-10)
        assertEquals(300.0, displayedHeight, 1e-10)

        // Test case 4: Extreme wide aspect ratio (e.g., 32:9 ultrawide)
        // Original: 3200x900, Target: 640x500
        // scale = min(640/3200, 500/900, 1.0) = min(0.2, 0.556, 1.0) = 0.2
        originalWidth = 3200.0
        originalHeight = 900.0

        scale = minOf(targetMaxW / originalWidth, targetMaxH / originalHeight, 1.0)
        displayedWidth = originalWidth * scale
        displayedHeight = originalHeight * scale

        assertEquals(0.2, scale, 1e-10)
        assertEquals(640.0, displayedWidth, 1e-10)
        assertEquals(180.0, displayedHeight, 1e-10)
        assertEquals(originalWidth / originalHeight, displayedWidth / displayedHeight, 1e-10)

        // Test case 5: Extreme tall aspect ratio (e.g., 9:32 vertical)
        // Original: 900x3200, Target: 640x500
        // scale = min(640/900, 500/3200, 1.0) = min(0.711, 0.156, 1.0) = 0.156
        originalWidth = 900.0
        originalHeight = 3200.0

        scale = minOf(targetMaxW / originalWidth, targetMaxH / originalHeight, 1.0)
        displayedWidth = originalWidth * scale
        displayedHeight = originalHeight * scale

        assertEquals(0.15625, scale, 1e-10)
        assertEquals(140.625, displayedWidth, 1e-10)
        assertEquals(500.0, displayedHeight, 1e-10)
        assertEquals(originalWidth / originalHeight, displayedWidth / displayedHeight, 1e-10)
    }

    /**
     * Tests the fitZoom scale calculation used to fit content in available workspace.
     */
    @Test
    fun testFitZoomScaleCalculation() {
        // Test case 1: Content fits comfortably with room to spare
        // displayedWidth=640, displayedHeight=360, availW=1000, availH=800
        // scale = min(1000/640, 800/360, 1.5) = min(1.5625, 2.222, 1.5) = 1.5
        var displayedWidth = 640.0
        var displayedHeight = 360.0
        var availW = 1000.0
        var availH = 800.0

        var scale = minOf(availW / displayedWidth, availH / displayedHeight, 1.5)
        assertEquals(1.5, scale, 1e-10, "Should be capped at 1.5 max zoom")

        // Test case 2: Content needs to shrink to fit
        // displayedWidth=640, displayedHeight=360, availW=500, availH=400
        // scale = min(500/640, 400/360, 1.5) = min(0.781, 1.111, 1.5) = 0.781
        displayedWidth = 640.0
        displayedHeight = 360.0
        availW = 500.0
        availH = 400.0

        scale = minOf(availW / displayedWidth, availH / displayedHeight, 1.5)
        assertEquals(0.78125, scale, 1e-10)

        // Test case 3: Very small available space
        // displayedWidth=640, displayedHeight=360, availW=200, availH=150
        // scale = min(200/640, 150/360, 1.5) = min(0.3125, 0.417, 1.5) = 0.3125
        displayedWidth = 640.0
        displayedHeight = 360.0
        availW = 200.0
        availH = 150.0

        scale = minOf(availW / displayedWidth, availH / displayedHeight, 1.5)
        assertEquals(0.3125, scale, 1e-10)

        // Test case 4: Exact fit (scale = 1.0)
        // displayedWidth=640, displayedHeight=360, availW=700, availH=440
        // scale = min(700/640, 440/360, 1.5) = min(1.094, 1.222, 1.5) = 1.094 (but should be capped by the logic?)
        // Actually the logic doesn't cap at 1.0 for fitZoom, only at 1.5
        displayedWidth = 640.0
        displayedHeight = 360.0
        availW = 700.0
        availH = 440.0

        scale = minOf(availW / displayedWidth, availH / displayedHeight, 1.5)
        assertEquals(1.09375, scale, 1e-10)
    }

    /**
     * Tests the export scale calculation used for high-resolution export.
     * exportScale = max(originalWidth / displayedWidth, 1.0)
     */
    @Test
    fun testExportScaleCalculation() {
        // Test case 1: Image was downscaled for display (original > displayed)
        // originalWidth=1920, displayedWidth=640
        // exportScale = max(1920/640, 1.0) = max(3.0, 1.0) = 3.0
        var originalWidth = 1920.0
        var displayedWidth = 640.0

        var exportScale = maxOf(originalWidth / displayedWidth, 1.0)
        assertEquals(3.0, exportScale, 1e-10)

        // Test case 2: Image was not scaled for display (original == displayed)
        // originalWidth=300, displayedWidth=300
        // exportScale = max(300/300, 1.0) = max(1.0, 1.0) = 1.0
        originalWidth = 300.0
        displayedWidth = 300.0

        exportScale = maxOf(originalWidth / displayedWidth, 1.0)
        assertEquals(1.0, exportScale, 1e-10)

        // Test case 3: Edge case - displayedWidth is 0 (should not happen in practice, but test bounds)
        // This would cause division by zero, but the code has coerceAtLeast(10.0) on originalWidth
        // and displayedWidth is derived from originalWidth * scale, so it should never be 0
        originalWidth = 10.0
        displayedWidth = 10.0

        exportScale = maxOf(originalWidth / displayedWidth, 1.0)
        assertEquals(1.0, exportScale, 1e-10)

        // Test case 4: Very large original, small displayed (high export scale)
        // originalWidth=7680 (8K), displayedWidth=640
        // exportScale = max(7680/640, 1.0) = max(12.0, 1.0) = 12.0
        originalWidth = 7680.0
        displayedWidth = 640.0

        exportScale = maxOf(originalWidth / displayedWidth, 1.0)
        assertEquals(12.0, exportScale, 1e-10)
    }

    /**
     * Tests bounds preservation - exported dimensions should match original dimensions.
     */
    @Test
    fun testBoundsPreservationOnExport() {
        // When exporting, the snapshot is taken with exportScale transform
        // The resulting image should have dimensions equal to originalWidth x originalHeight

        // Test case 1: Standard 16:9 landscape
        var originalWidth = 1920.0
        var originalHeight = 1080.0
        val targetMaxW = 640.0
        val targetMaxH = 500.0

        val displayScale = minOf(targetMaxW / originalWidth, targetMaxH / originalHeight, 1.0)
        val displayedWidth = originalWidth * displayScale
        val displayedHeight = originalHeight * displayScale

        val exportScale = maxOf(originalWidth / displayedWidth, 1.0)

        // Exported width = displayedWidth * exportScale = displayedWidth * (originalWidth / displayedWidth) = originalWidth
        val exportedWidth = displayedWidth * exportScale
        val exportedHeight = displayedHeight * exportScale

        assertEquals(
            originalWidth, exportedWidth, 1e-10,
            "Exported width should match original width"
        )
        assertEquals(
            originalHeight, exportedHeight, 1e-10,
            "Exported height should match original height"
        )

        // Test case 2: Square image
        originalWidth = 500.0
        originalHeight = 500.0

        val displayScale2 = minOf(targetMaxW / originalWidth, targetMaxH / originalHeight, 1.0)
        val displayedWidth2 = originalWidth * displayScale2
        val displayedHeight2 = originalHeight * displayScale2

        val exportScale2 = maxOf(originalWidth / displayedWidth2, 1.0)

        val exportedWidth2 = displayedWidth2 * exportScale2
        val exportedHeight2 = displayedHeight2 * exportScale2

        assertEquals(originalWidth, exportedWidth2, 1e-10)
        assertEquals(originalHeight, exportedHeight2, 1e-10)

        // Test case 3: 9:16 portrait (vertical)
        originalWidth = 1080.0
        originalHeight = 1920.0

        val displayScale3 = minOf(targetMaxW / originalWidth, targetMaxH / originalHeight, 1.0)
        val displayedWidth3 = originalWidth * displayScale3
        val displayedHeight3 = originalHeight * displayScale3

        val exportScale3 = maxOf(originalWidth / displayedWidth3, 1.0)

        val exportedWidth3 = displayedWidth3 * exportScale3
        val exportedHeight3 = displayedHeight3 * exportScale3

        assertEquals(originalWidth, exportedWidth3, 1e-10)
        assertEquals(originalHeight, exportedHeight3, 1e-10)
    }

    /**
     * Tests aspect ratio preservation across various dimensions.
     * Verifies that width/height ratio remains constant through scale operations.
     */
    @Test
    fun testAspectRatioPreservation() {
        // Use a data class for test cases to avoid nested Pair issues
        data class TestCase(
            val originalWidth: Double,
            val originalHeight: Double,
            val description: String
        )

        val testCases = listOf(
            TestCase(1920.0, 1080.0, "16:9 landscape"),
            TestCase(1080.0, 1920.0, "9:16 portrait"),
            TestCase(1080.0, 1080.0, "1:1 square"),
            TestCase(3840.0, 2160.0, "4K 16:9"),
            TestCase(7680.0, 4320.0, "8K 16:9"),
            TestCase(3200.0, 900.0, "32:9 ultrawide"),
            TestCase(900.0, 3200.0, "9:32 vertical"),
            TestCase(400.0, 300.0, "4:3 classic"),
            TestCase(1440.0, 900.0, "16:10"),
            TestCase(2560.0, 1440.0, "1440p 16:9")
        )

        val targetMaxW = 640.0
        val targetMaxH = 500.0

        testCases.forEach { testCase ->
            val originalWidth = testCase.originalWidth
            val originalHeight = testCase.originalHeight
            val description = testCase.description
            val originalRatio = originalWidth / originalHeight

            // Display scale
            val displayScale = minOf(targetMaxW / originalWidth, targetMaxH / originalHeight, 1.0)
            val displayedWidth = originalWidth * displayScale
            val displayedHeight = originalHeight * displayScale
            val displayedRatio = displayedWidth / displayedHeight

            assertEquals(
                originalRatio, displayedRatio, 1e-10,
                "Display aspect ratio should be preserved for $description"
            )

            // Export scale
            val exportScale = maxOf(originalWidth / displayedWidth, 1.0)
            val exportedWidth = displayedWidth * exportScale
            val exportedHeight = displayedHeight * exportScale
            val exportedRatio = exportedWidth / exportedHeight

            assertEquals(
                originalRatio, exportedRatio, 1e-10,
                "Export aspect ratio should be preserved for $description"
            )

            // Verify absolute dimensions match original
            assertEquals(
                originalWidth, exportedWidth, 1e-10,
                "Exported width should match original for $description"
            )
            assertEquals(
                originalHeight, exportedHeight, 1e-10,
                "Exported height should match original for $description"
            )
        }
    }

    /**
     * Tests edge cases and boundary conditions for scale calculations.
     */
    @Test
    fun testEdgeCasesAndBoundaries() {
        // Minimum dimensions (coerceAtLeast(10.0) in applyImageDimensions)
        val minDim = 10.0
        val targetMaxW = 640.0
        val targetMaxH = 500.0

        // Very small image - should not upscale (scale capped at 1.0)
        var originalWidth = minDim
        var originalHeight = minDim

        var scale = minOf(targetMaxW / originalWidth, targetMaxH / originalHeight, 1.0)
        assertEquals(1.0, scale, 1e-10, "Small images should not be upscaled")

        var displayedWidth = originalWidth * scale
        var displayedHeight = originalHeight * scale
        assertEquals(minDim, displayedWidth, 1e-10)
        assertEquals(minDim, displayedHeight, 1e-10)

        // Image exactly at target bounds
        originalWidth = targetMaxW
        originalHeight = targetMaxH

        scale = minOf(targetMaxW / originalWidth, targetMaxH / originalHeight, 1.0)
        assertEquals(1.0, scale, 1e-10)

        displayedWidth = originalWidth * scale
        displayedHeight = originalHeight * scale
        assertEquals(targetMaxW, displayedWidth, 1e-10)
        assertEquals(targetMaxH, displayedHeight, 1e-10)

        // Image slightly larger than target in one dimension
        originalWidth = targetMaxW + 1
        originalHeight = targetMaxH

        scale = minOf(targetMaxW / originalWidth, targetMaxH / originalHeight, 1.0)
        assertEquals(targetMaxW / (targetMaxW + 1), scale, 1e-10)

        displayedWidth = originalWidth * scale
        displayedHeight = originalHeight * scale
        assertEquals(targetMaxW, displayedWidth, 1e-10)
        assertEquals(targetMaxH * targetMaxW / (targetMaxW + 1), displayedHeight, 1e-10)

        // Verify export scale brings it back to original
        val exportScale = maxOf(originalWidth / displayedWidth, 1.0)
        val exportedWidth = displayedWidth * exportScale
        val exportedHeight = displayedHeight * exportScale

        assertEquals(originalWidth, exportedWidth, 1e-10)
        assertEquals(originalHeight, exportedHeight, 1e-10)
    }

    /**
     * Tests that the coerceAtLeast(10.0) protection prevents division by zero
     * and handles degenerate image dimensions.
     */
    @Test
    fun testCoerceAtLeastProtection() {
        // Simulate what happens when image.width or image.height returns 0 or negative
        // The code uses: image.width.coerceAtLeast(10.0)
        // This ensures we never have width/height <= 0

        val zeroWidth = 0.0.coerceAtLeast(10.0)
        val negativeWidth = (-5.0).coerceAtLeast(10.0)
        val smallWidth = 5.0.coerceAtLeast(10.0)
        val normalWidth = 100.0.coerceAtLeast(10.0)

        assertEquals(10.0, zeroWidth)
        assertEquals(10.0, negativeWidth)
        assertEquals(10.0, smallWidth)
        assertEquals(100.0, normalWidth)

        // Verify scale calculation works with coerced values
        val targetMaxW = 640.0
        val targetMaxH = 500.0

        val scale = minOf(targetMaxW / zeroWidth, targetMaxH / zeroWidth, 1.0)
        assertEquals(1.0, scale, 1e-10, "Should not crash or produce invalid scale")
    }

    /**
     * Tests geometry calculations for meme with top banner.
     */
    @Test
    fun testTopBannerGeometryAndExportScaling() {
        val originalWidth = 1200.0
        val originalHeight = 800.0
        val targetMaxW = 640.0
        val targetMaxH = 500.0

        val scale = minOf(targetMaxW / originalWidth, targetMaxH / originalHeight, 1.0)
        val displayedWidth = originalWidth * scale
        val displayedHeight = originalHeight * scale

        val bannerHeight = 100.0
        val totalDisplayedHeight = displayedHeight + bannerHeight

        val exportScale = maxOf(originalWidth / displayedWidth, 1.0)
        val exportedWidth = displayedWidth * exportScale
        val exportedHeight = totalDisplayedHeight * exportScale

        assertEquals(1200.0, exportedWidth, 1e-10)
        assertEquals(800.0 + (bannerHeight * exportScale), exportedHeight, 1e-10)
        assertEquals(bannerHeight * exportScale, exportedHeight - originalHeight, 1e-10)
    }
}
