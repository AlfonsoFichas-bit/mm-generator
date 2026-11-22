package com.buga.mmgenerator

import com.buga.mmgenerator.model.MemeTemplate
import com.buga.mmgenerator.service.TemplateRepository
import com.buga.mmgenerator.ui.Icons
import javafx.animation.FadeTransition
import javafx.animation.ParallelTransition
import javafx.animation.PauseTransition
import javafx.animation.TranslateTransition
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.embed.swing.SwingFXUtils
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.SnapshotParameters
import javafx.scene.control.*
import javafx.scene.effect.BlurType
import javafx.scene.effect.DropShadow
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.input.ScrollEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import javafx.scene.shape.StrokeType
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.util.Duration
import java.awt.Desktop
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.math.abs

class MemesEditorController {

    @FXML
    private lateinit var editorRoot: StackPane

    @FXML
    private lateinit var mainLayout: BorderPane

    @FXML
    private lateinit var headerBar: HBox

    @FXML
    private lateinit var btnBack: Button

    @FXML
    private lateinit var backIconContainer: StackPane

    @FXML
    private lateinit var btnReset: Button

    @FXML
    private lateinit var resetIconContainer: StackPane

    @FXML
    private lateinit var lblTemplateTitle: Label

    @FXML
    private lateinit var btnQuickAddText: Button

    @FXML
    private lateinit var addIconContainer: StackPane

    @FXML
    private lateinit var btnExport: Button

    @FXML
    private lateinit var exportIconContainer: StackPane

    @FXML
    private lateinit var btnMinimize: Button

    @FXML
    private lateinit var minimizeIconContainer: StackPane

    @FXML
    private lateinit var btnMaximize: Button

    @FXML
    private lateinit var maximizeIconContainer: StackPane

    @FXML
    private lateinit var btnClose: Button

    @FXML
    private lateinit var closeIconContainer: StackPane

    @FXML
    private lateinit var workspaceRoot: StackPane

    @FXML
    private lateinit var workspaceScroll: ScrollPane

    @FXML
    private lateinit var workspacePane: StackPane

    @FXML
    private lateinit var zoomGroup: Group

    @FXML
    private lateinit var memeFrame: StackPane

    @FXML
    private lateinit var memeContainer: Pane

    @FXML
    private lateinit var imageView: ImageView

    @FXML
    private lateinit var zoomControlsPill: HBox

    @FXML
    private lateinit var btnZoomOut: Button

    @FXML
    private lateinit var lblZoomPercent: Label

    @FXML
    private lateinit var btnZoomIn: Button

    @FXML
    private lateinit var btnZoomReset: Button

    @FXML
    private lateinit var btnZoomFit: Button

    @FXML
    private lateinit var sidebar: VBox

    @FXML
    private lateinit var btnClassicPreset: Button

    @FXML
    private lateinit var textPropertiesGroup: VBox

    @FXML
    private lateinit var btnUppercase: Button

    @FXML
    private lateinit var textContentArea: TextArea

    @FXML
    private lateinit var fontComboBox: ComboBox<String>

    @FXML
    private lateinit var lblFontSize: Label

    @FXML
    private lateinit var btnFontDec: Button

    @FXML
    private lateinit var sliderFontSize: Slider

    @FXML
    private lateinit var btnFontInc: Button

    @FXML
    private lateinit var btnAlignLeft: Button

    @FXML
    private lateinit var btnAlignCenter: Button

    @FXML
    private lateinit var btnAlignRight: Button

    @FXML
    private lateinit var colorPickerFill: ColorPicker

    @FXML
    private lateinit var colorPresetsContainer: HBox

    @FXML
    private lateinit var comboStrokeWidth: ComboBox<String>

    @FXML
    private lateinit var btnDeleteText: Button

    @FXML
    private lateinit var trashIconContainer: StackPane

    @FXML
    private lateinit var lblLayerCount: Label

    @FXML
    private lateinit var layersListContainer: VBox

    @FXML
    private lateinit var lblEmptyLayers: Label

    @FXML
    private lateinit var toastContainer: HBox

    @FXML
    private lateinit var toastIconContainer: StackPane

    @FXML
    private lateinit var lblToastTitle: Label

    @FXML
    private lateinit var lblToastSubtitle: Label

    @FXML
    private lateinit var btnToastOpenFolder: Button

    @FXML
    private lateinit var btnToastClose: Button

    private var toastHidePause: PauseTransition? = null

    // State
    private var currentTemplate: MemeTemplate? = null
    private var currentStage: Stage? = null
    private var nextTextId = 1
    private val textItems = mutableListOf<MemeTextItem>()
    private var selectedItem: MemeTextItem? = null

    private var displayedWidth = 600.0
    private var displayedHeight = 450.0
    private var originalWidth = 600.0
    private var originalHeight = 450.0
    private var zoomFactor = 1.0

    private var xOffset = 0.0
    private var yOffset = 0.0

    // Alignment Guide Lines (Word / PowerPoint style)
    private val guideOverlay = Pane().apply {
        isMouseTransparent = true
    }

    private val guideLineVertical = Line().apply {
        stroke = Color.web("#3584e4")
        strokeWidth = 1.5
        strokeDashArray.addAll(6.0, 4.0)
        effect = DropShadow(BlurType.ONE_PASS_BOX, Color.rgb(0, 0, 0, 0.75), 2.0, 0.0, 0.0, 0.0)
        isVisible = false
        isMouseTransparent = true
    }

    private val guideLineHorizontal = Line().apply {
        stroke = Color.web("#3584e4")
        strokeWidth = 1.5
        strokeDashArray.addAll(6.0, 4.0)
        effect = DropShadow(BlurType.ONE_PASS_BOX, Color.rgb(0, 0, 0, 0.75), 2.0, 0.0, 0.0, 0.0)
        isVisible = false
        isMouseTransparent = true
    }

    private val guideCenterDot = Circle(4.5).apply {
        fill = Color.web("#3584e4")
        stroke = Color.WHITE
        strokeWidth = 1.5
        effect = DropShadow(BlurType.ONE_PASS_BOX, Color.rgb(0, 0, 0, 0.8), 3.0, 0.0, 0.0, 0.0)
        isVisible = false
        isMouseTransparent = true
    }

    // Font Cache to eliminate lag on keystrokes
    object FontCache {
        private val cache = mutableMapOf<Pair<String, Double>, Font>()

        fun get(family: String, size: Double): Font {
            val key = Pair(family, size)
            return cache.getOrPut(key) {
                try {
                    Font.font(family, FontWeight.BOLD, size)
                } catch (e: Exception) {
                    Font.font(size)
                }
            }
        }
    }

    // Inner class representing a text overlay
    class MemeTextItem(
        val id: Int,
        var content: String,
        var fontName: String = "Impact",
        var fontSize: Double = 36.0,
        var isUppercase: Boolean = true,
        var fillColor: Color = Color.WHITE,
        var strokeColor: Color = Color.BLACK,
        var strokeWidth: Double = 2.5,
        var alignment: TextAlignment = TextAlignment.CENTER,
        var x: Double = 10.0,
        var y: Double = 10.0
    ) {
        val textNode = Text()
        val wrapper = StackPane(textNode)

        // References to UI row to avoid rebuilding DOM on typing
        var previewLabel: Label? = null
        var rowBox: HBox? = null

        init {
            wrapper.styleClass.add("meme-text-unselected")
            wrapper.isPickOnBounds = true
            wrapper.padding = Insets(4.0, 8.0, 4.0, 8.0)
        }

        /** Ultra-fast update when typing characters */
        fun updateTextOnly(canvasWidth: Double = 0.0) {
            textNode.text = if (isUppercase) content.uppercase() else content
            if (canvasWidth > 0.0) {
                textNode.wrappingWidth = 0.0
                val maxWidth = (canvasWidth - 32.0).coerceAtLeast(80.0)
                if (textNode.layoutBounds.width > maxWidth) {
                    textNode.wrappingWidth = maxWidth
                }
            }
            previewLabel?.text = if (content.isBlank()) "(vacío)" else content
        }

        fun refresh(canvasWidth: Double) {
            textNode.text = if (isUppercase) content.uppercase() else content
            textNode.font = FontCache.get(fontName, fontSize)
            textNode.fill = fillColor
            textNode.stroke = strokeColor
            textNode.strokeWidth = strokeWidth
            textNode.strokeType = StrokeType.OUTSIDE
            textNode.textAlignment = alignment

            textNode.wrappingWidth = 0.0
            val maxWidth = (canvasWidth - 32.0).coerceAtLeast(80.0)
            if (textNode.layoutBounds.width > maxWidth) {
                textNode.wrappingWidth = maxWidth
            }

            wrapper.layoutX = x
            wrapper.layoutY = y

            previewLabel?.text = if (content.isBlank()) "(vacío)" else content
        }
    }

    @FXML
    fun initialize() {
        setupIcons()
        setupFontFamilyList()
        setupColorPresets()
        setupStrokeOptions()
        setupEventHandlers()
        setupAlignmentButtons()
        setupZoomControls()
        setupToast()
        deselect()
    }

    private fun setupIcons() {
        backIconContainer.children.setAll(Icons.create(Icons.ARROW_BACK, Color.web("#deddda"), 14.0))
        resetIconContainer.children.setAll(Icons.create(Icons.REFRESH, Color.web("#9a9996"), 14.0))
        addIconContainer.children.setAll(Icons.create(Icons.PLUS, Color.web("#deddda"), 13.0))
        exportIconContainer.children.setAll(Icons.create(Icons.DOWNLOAD, Color.WHITE, 13.0))
        minimizeIconContainer.children.setAll(Icons.create(Icons.MINIMIZE, Color.web("#9a9996"), 11.0))
        maximizeIconContainer.children.setAll(Icons.create(Icons.MAXIMIZE, Color.web("#9a9996"), 11.0))
        closeIconContainer.children.setAll(Icons.create(Icons.CLOSE, Color.web("#9a9996"), 11.0))
        trashIconContainer.children.setAll(Icons.create(Icons.TRASH, Color.WHITE, 13.0))

        btnAlignLeft.graphic = Icons.create(Icons.ALIGN_LEFT, Color.web("#deddda"), 13.0)
        btnAlignCenter.graphic = Icons.create(Icons.ALIGN_CENTER, Color.web("#deddda"), 13.0)
        btnAlignRight.graphic = Icons.create(Icons.ALIGN_RIGHT, Color.web("#deddda"), 13.0)
    }

    private fun setupFontFamilyList() {
        val bundledFonts = listOf(
            "assets/bundled/00_Pressuru.otf",
            "assets/bundled/01_ComicRelief.ttf",
            "assets/bundled/Roboto Bold.ttf",
            "assets/bundled/SourceSans Pro.otf"
        )
        bundledFonts.forEach { fontPath ->
            try {
                val stream = javaClass.getResourceAsStream("/$fontPath")
                    ?: File(fontPath).takeIf { it.exists() }?.inputStream()
                stream?.use { Font.loadFont(it, 16.0) }
            } catch (_: Exception) {}
        }

        val allFamilies = Font.getFamilies()
        val preferredMemeFonts = listOf(
            "Impact",
            "Arial Black",
            "Arial",
            "Comic Sans MS",
            "Roboto",
            "Cantarell",
            "Trebuchet MS",
            "Verdana",
            "Comic Relief",
            "Pressuru"
        )

        val availablePreferred = preferredMemeFonts.filter { fontName ->
            allFamilies.any { it.equals(fontName, ignoreCase = true) }
        }

        val finalList = if (availablePreferred.isNotEmpty()) {
            availablePreferred
        } else {
            listOf("Impact", "Arial", "sans-serif")
        }

        fontComboBox.items = FXCollections.observableArrayList(finalList)
        fontComboBox.value = finalList.firstOrNull { it.equals("Impact", ignoreCase = true) } ?: finalList.first()

        fontComboBox.setOnAction {
            val selected = fontComboBox.value ?: return@setOnAction
            selectedItem?.let {
                it.fontName = selected
                it.refresh(displayedWidth)
            }
        }
    }

    private fun setupColorPresets() {
        val presetColors = listOf(
            Color.WHITE to "#ffffff",
            Color.BLACK to "#000000",
            Color.web("#f5c211") to "#f5c211", // Meme Yellow
            Color.web("#e01b24") to "#e01b24", // Red
            Color.web("#3584e4") to "#3584e4", // Blue
            Color.web("#33d17a") to "#33d17a"  // Lime Green
        )

        colorPresetsContainer.children.clear()
        presetColors.forEach { (color, hex) ->
            val circle = Button().apply {
                styleClass.add("color-preset-circle")
                style = "-fx-background-color: $hex;"
                setOnAction {
                    colorPickerFill.value = color
                    selectedItem?.let { item ->
                        item.fillColor = color
                        item.refresh(displayedWidth)
                    }
                }
            }
            colorPresetsContainer.children.add(circle)
        }

        colorPickerFill.value = Color.WHITE
        colorPickerFill.setOnAction {
            val chosen = colorPickerFill.value
            selectedItem?.let { item ->
                item.fillColor = chosen
                item.refresh(displayedWidth)
            }
        }
    }

    private fun setupStrokeOptions() {
        val strokeOptions = listOf(
            "Grueso (4px)",
            "Normal (2.5px)",
            "Fino (1.5px)",
            "Ninguno"
        )
        comboStrokeWidth.items = FXCollections.observableArrayList(strokeOptions)
        comboStrokeWidth.value = "Normal (2.5px)"

        comboStrokeWidth.setOnAction {
            val width = when (comboStrokeWidth.value) {
                "Grueso (4px)" -> 4.0
                "Normal (2.5px)" -> 2.5
                "Fino (1.5px)" -> 1.5
                "Ninguno" -> 0.0
                else -> 2.5
            }
            selectedItem?.let { item ->
                item.strokeWidth = width
                item.refresh(displayedWidth)
            }
        }
    }

    private fun setupAlignmentButtons() {
        btnAlignLeft.setOnAction {
            selectedItem?.let {
                it.alignment = TextAlignment.LEFT
                it.refresh(displayedWidth)
            }
        }
        btnAlignCenter.setOnAction {
            selectedItem?.let {
                it.alignment = TextAlignment.CENTER
                val boxW = if (it.wrapper.width > 0) it.wrapper.width else it.textNode.layoutBounds.width + 16.0
                it.x = ((displayedWidth - boxW) / 2.0).coerceAtLeast(0.0)
                it.wrapper.layoutX = it.x
                it.refresh(displayedWidth)
            }
        }
        btnAlignRight.setOnAction {
            selectedItem?.let {
                it.alignment = TextAlignment.RIGHT
                it.refresh(displayedWidth)
            }
        }
    }

    private fun setupZoomControls() {
        btnZoomIn.setOnAction { zoomIn() }
        btnZoomOut.setOnAction { zoomOut() }
        btnZoomReset.setOnAction { resetZoom() }
        btnZoomFit.setOnAction { fitZoom() }

        // Ctrl + Mouse Wheel Zoom anywhere on workspace
        workspaceRoot.addEventFilter(ScrollEvent.SCROLL) { event ->
            if (event.isControlDown || event.isMetaDown) {
                event.consume()
                if (event.deltaY > 0) {
                    zoomIn()
                } else if (event.deltaY < 0) {
                    zoomOut()
                }
            }
        }
    }

    private fun setZoom(newZoom: Double) {
        zoomFactor = newZoom.coerceIn(0.25, 3.5)
        memeFrame.scaleX = zoomFactor
        memeFrame.scaleY = zoomFactor
        lblZoomPercent.text = "${(zoomFactor * 100).toInt()}%"
    }

    private fun zoomIn() {
        setZoom(zoomFactor * 1.15)
    }

    private fun zoomOut() {
        setZoom(zoomFactor / 1.15)
    }

    private fun resetZoom() {
        setZoom(1.0)
    }

    private fun fitZoom() {
        val availW = workspaceScroll.viewportBounds.width - 60.0
        val availH = workspaceScroll.viewportBounds.height - 80.0
        if (availW > 100.0 && availH > 100.0 && displayedWidth > 0 && displayedHeight > 0) {
            val scale = minOf(availW / displayedWidth, availH / displayedHeight, 1.5)
            setZoom(scale)
        }
    }

    private fun setupEventHandlers() {
        // Quick add button in header
        btnQuickAddText.setOnAction {
            val item = addTextItem("NUEVA FRASE", 0.0, 0.0)
            val w = if (item.wrapper.width > 0) item.wrapper.width else item.textNode.layoutBounds.width + 16.0
            val h = if (item.wrapper.height > 0) item.wrapper.height else item.textNode.layoutBounds.height + 8.0
            item.x = ((displayedWidth - w) / 2.0).coerceAtLeast(10.0)
            item.y = ((displayedHeight - h) / 2.0).coerceAtLeast(10.0)
            item.wrapper.layoutX = item.x
            item.wrapper.layoutY = item.y
        }

        // Classic meme preset (Top and Bottom)
        btnClassicPreset.setOnAction {
            applyClassicPreset()
        }

        // Reset button
        btnReset.setOnAction {
            applyClassicPreset()
        }

        // Uppercase toggle
        btnUppercase.setOnAction {
            selectedItem?.let { item ->
                item.isUppercase = !item.isUppercase
                btnUppercase.text = if (item.isUppercase) "MAYÚS" else "Minús"
                item.updateTextOnly(displayedWidth)
            }
        }

        // Fast typing without rebuilding layers list DOM
        textContentArea.textProperty().addListener { _, _, newText ->
            selectedItem?.let { item ->
                item.content = newText ?: ""
                item.updateTextOnly(displayedWidth)
            }
        }

        // Safety listeners to ensure guides always disappear on mouse release
        memeContainer.setOnMouseReleased { hideAllGuides() }
        workspaceRoot.setOnMouseReleased { hideAllGuides() }

        // Font size slider & stepper
        sliderFontSize.valueProperty().addListener { _, _, newVal ->
            val size = newVal.toDouble()
            lblFontSize.text = "${size.toInt()} px"
            selectedItem?.let { item ->
                item.fontSize = size
                item.refresh(displayedWidth)
            }
        }

        btnFontDec.setOnAction {
            sliderFontSize.value = (sliderFontSize.value - 2.0).coerceAtLeast(sliderFontSize.min)
        }

        btnFontInc.setOnAction {
            sliderFontSize.value = (sliderFontSize.value + 2.0).coerceAtMost(sliderFontSize.max)
        }

        // Delete text button
        btnDeleteText.setOnAction {
            selectedItem?.let { deleteItem(it) }
        }

        // Export button
        btnExport.setOnAction {
            exportMeme()
        }

        // Click directly on meme canvas (with local coordinates mapped across zoom transforms)
        memeContainer.setOnMouseClicked { e ->
            if (e.button == MouseButton.PRIMARY && e.clickCount == 1) {
                if (selectedItem != null) {
                    deselect()
                } else {
                    val localPt = memeContainer.sceneToLocal(e.sceneX, e.sceneY)
                    val clickX = (localPt.x - 80.0).coerceIn(10.0, displayedWidth - 100.0)
                    val clickY = (localPt.y - 20.0).coerceIn(10.0, displayedHeight - 40.0)
                    addTextItem("TEXTO AQUÍ", clickX, clickY)
                }
            }
        }
    }

    fun setStage(stage: Stage) {
        currentStage = stage

        btnMinimize.setOnAction { stage.isIconified = true }
        btnMaximize.setOnAction { stage.isMaximized = !stage.isMaximized }
        btnClose.setOnAction { stage.close() }
        btnBack.setOnAction { stage.close() }

        headerBar.setOnMousePressed { event ->
            xOffset = event.sceneX
            yOffset = event.sceneY
        }

        headerBar.setOnMouseDragged { event ->
            if (!stage.isMaximized) {
                stage.x = event.screenX - xOffset
                stage.y = event.screenY - yOffset
            }
        }

        headerBar.setOnMouseClicked { event ->
            if (event.clickCount == 2) {
                stage.isMaximized = !stage.isMaximized
            }
        }
    }

    fun setTemplate(template: MemeTemplate) {
        currentTemplate = template
        lblTemplateTitle.text = template.title

        val image = TemplateRepository.getImage(template)
        if (image != null) {
            imageView.image = image
            if (image.progress < 1.0) {
                image.progressProperty().addListener { _, _, progress ->
                    if (progress.toDouble() >= 1.0) {
                        Platform.runLater {
                            applyImageDimensions(image)
                        }
                    }
                }
            } else {
                applyImageDimensions(image)
            }
        }
    }

    private fun applyImageDimensions(image: Image) {
        originalWidth = image.width.coerceAtLeast(10.0)
        originalHeight = image.height.coerceAtLeast(10.0)

        val targetMaxW = 640.0
        val targetMaxH = 500.0
        val scale = minOf(targetMaxW / originalWidth, targetMaxH / originalHeight, 1.0)

        // Strict aspect ratio match so there are NO empty black bars on any side
        displayedWidth = originalWidth * scale
        displayedHeight = originalHeight * scale

        imageView.fitWidth = displayedWidth
        imageView.fitHeight = displayedHeight

        memeFrame.prefWidth = displayedWidth
        memeFrame.prefHeight = displayedHeight
        memeFrame.minWidth = displayedWidth
        memeFrame.minHeight = displayedHeight
        memeFrame.maxWidth = displayedWidth
        memeFrame.maxHeight = displayedHeight

        memeContainer.prefWidth = displayedWidth
        memeContainer.prefHeight = displayedHeight
        memeContainer.minWidth = displayedWidth
        memeContainer.minHeight = displayedHeight
        memeContainer.maxWidth = displayedWidth
        memeContainer.maxHeight = displayedHeight

        // Clip texts inside meme bounds without corner cutouts
        memeContainer.clip = Rectangle(displayedWidth, displayedHeight)

        if (!memeFrame.children.contains(guideOverlay)) {
            guideOverlay.children.setAll(guideLineVertical, guideLineHorizontal, guideCenterDot)
            memeFrame.children.add(guideOverlay)
        }
        guideOverlay.prefWidth = displayedWidth
        guideOverlay.prefHeight = displayedHeight
        guideOverlay.minWidth = displayedWidth
        guideOverlay.minHeight = displayedHeight
        guideOverlay.maxWidth = displayedWidth
        guideOverlay.maxHeight = displayedHeight

        resetZoom()
        applyClassicPreset()
    }

    private fun applyClassicPreset() {
        clearAllTexts()

        val topItem = addTextItem(
            content = "TEXTO SUPERIOR",
            x = 0.0,
            y = 12.0,
            fontSize = 38.0
        )
        val topW = topItem.textNode.layoutBounds.width + 16.0
        topItem.x = ((displayedWidth - topW) / 2.0).coerceAtLeast(10.0)
        topItem.wrapper.layoutX = topItem.x

        val bottomItem = addTextItem(
            content = "TEXTO INFERIOR",
            x = 0.0,
            y = (displayedHeight - 65.0).coerceAtLeast(10.0),
            fontSize = 38.0
        )
        val bottomW = bottomItem.textNode.layoutBounds.width + 16.0
        bottomItem.x = ((displayedWidth - bottomW) / 2.0).coerceAtLeast(10.0)
        bottomItem.wrapper.layoutX = bottomItem.x

        selectItem(topItem)
    }

    private fun addTextItem(
        content: String,
        x: Double,
        y: Double,
        fontSize: Double = 36.0,
        fontFamily: String = fontComboBox.value ?: "Impact"
    ): MemeTextItem {
        val item = MemeTextItem(
            id = nextTextId++,
            content = content,
            fontName = fontFamily,
            fontSize = fontSize,
            isUppercase = true,
            fillColor = colorPickerFill.value ?: Color.WHITE,
            strokeColor = Color.BLACK,
            strokeWidth = 2.5,
            alignment = TextAlignment.CENTER,
            x = x,
            y = y
        )

        attachDragAndClick(item)
        item.refresh(displayedWidth)

        textItems.add(item)
        memeContainer.children.add(item.wrapper)

        renderLayersList()
        selectItem(item)

        return item
    }

    private fun attachDragAndClick(item: MemeTextItem) {
        var dragStartX = 0.0
        var dragStartY = 0.0

        item.wrapper.setOnMousePressed { event ->
            if (event.button == MouseButton.PRIMARY) {
                // Map scene coordinates to local unscaled coordinate space of memeContainer
                val localPt = memeContainer.sceneToLocal(event.sceneX, event.sceneY)
                dragStartX = localPt.x - item.wrapper.layoutX
                dragStartY = localPt.y - item.wrapper.layoutY
                item.wrapper.toFront()
                selectItem(item)
                event.consume()
            }
        }

        item.wrapper.setOnMouseDragged { event ->
            if (event.button == MouseButton.PRIMARY) {
                val localPt = memeContainer.sceneToLocal(event.sceneX, event.sceneY)
                var newX = localPt.x - dragStartX
                var newY = localPt.y - dragStartY

                val boxW = if (item.wrapper.width > 0) item.wrapper.width else item.textNode.layoutBounds.width + 16.0
                val boxH = if (item.wrapper.height > 0) item.wrapper.height else item.textNode.layoutBounds.height + 8.0

                val canvasCenterX = displayedWidth / 2.0
                val canvasCenterY = displayedHeight / 2.0

                val snapThreshold = 8.0

                val currentCenterX = newX + boxW / 2.0
                val currentCenterY = newY + boxH / 2.0

                // 1. Horizontal Centering & Guides (Vertical Line)
                var snappedX = false
                if (abs(currentCenterX - canvasCenterX) <= snapThreshold) {
                    newX = canvasCenterX - boxW / 2.0
                    showVerticalGuide(canvasCenterX)
                    snappedX = true
                } else {
                    val otherX = textItems.asSequence()
                        .filter { it != item }
                        .map { other ->
                            val otherW = if (other.wrapper.width > 0) other.wrapper.width else other.textNode.layoutBounds.width + 16.0
                            other.wrapper.layoutX + otherW / 2.0
                        }
                        .firstOrNull { abs(currentCenterX - it) <= snapThreshold }

                    if (otherX != null) {
                        newX = otherX - boxW / 2.0
                        showVerticalGuide(otherX)
                        snappedX = true
                    }
                }

                if (!snappedX) {
                    hideVerticalGuide()
                }

                // 2. Vertical Centering & Guides (Horizontal Line)
                var snappedY = false
                if (abs(currentCenterY - canvasCenterY) <= snapThreshold) {
                    newY = canvasCenterY - boxH / 2.0
                    showHorizontalGuide(canvasCenterY)
                    snappedY = true
                } else {
                    val otherY = textItems.asSequence()
                        .filter { it != item }
                        .map { other ->
                            val otherH = if (other.wrapper.height > 0) other.wrapper.height else other.textNode.layoutBounds.height + 8.0
                            other.wrapper.layoutY + otherH / 2.0
                        }
                        .firstOrNull { abs(currentCenterY - it) <= snapThreshold }

                    if (otherY != null) {
                        newY = otherY - boxH / 2.0
                        showHorizontalGuide(otherY)
                        snappedY = true
                    }
                }

                if (!snappedY) {
                    hideHorizontalGuide()
                }

                // Clamping so text doesn't escape entirely
                newX = newX.coerceIn(-boxW + 20.0, displayedWidth - 20.0)
                newY = newY.coerceIn(0.0, displayedHeight - 20.0)

                item.x = newX
                item.y = newY
                item.wrapper.layoutX = newX
                item.wrapper.layoutY = newY
                event.consume()
            }
        }

        item.wrapper.setOnMouseReleased { event ->
            hideAllGuides()
            event.consume()
        }

        item.wrapper.setOnMouseClicked { event ->
            event.consume()
        }
    }

    private fun showVerticalGuide(x: Double) {
        guideLineVertical.startX = x
        guideLineVertical.endX = x
        guideLineVertical.startY = 0.0
        guideLineVertical.endY = displayedHeight
        guideLineVertical.isVisible = true
        updateCenterDot()
    }

    private fun hideVerticalGuide() {
        guideLineVertical.isVisible = false
        updateCenterDot()
    }

    private fun showHorizontalGuide(y: Double) {
        guideLineHorizontal.startX = 0.0
        guideLineHorizontal.endX = displayedWidth
        guideLineHorizontal.startY = y
        guideLineHorizontal.endY = y
        guideLineHorizontal.isVisible = true
        updateCenterDot()
    }

    private fun hideHorizontalGuide() {
        guideLineHorizontal.isVisible = false
        updateCenterDot()
    }

    private fun hideAllGuides() {
        guideLineVertical.isVisible = false
        guideLineHorizontal.isVisible = false
        guideCenterDot.isVisible = false
    }

    private fun updateCenterDot() {
        if (guideLineVertical.isVisible && guideLineHorizontal.isVisible) {
            guideCenterDot.centerX = guideLineVertical.startX
            guideCenterDot.centerY = guideLineHorizontal.startY
            guideCenterDot.isVisible = true
        } else {
            guideCenterDot.isVisible = false
        }
    }

    private fun selectItem(item: MemeTextItem) {
        // Deselect previous
        selectedItem?.wrapper?.let {
            it.styleClass.remove("meme-text-selected")
            if (!it.styleClass.contains("meme-text-unselected")) {
                it.styleClass.add("meme-text-unselected")
            }
        }

        selectedItem = item

        // Select current
        item.wrapper.styleClass.remove("meme-text-unselected")
        if (!item.wrapper.styleClass.contains("meme-text-selected")) {
            item.wrapper.styleClass.add("meme-text-selected")
        }

        // Populate sidebar
        textPropertiesGroup.isDisable = false
        textContentArea.text = item.content
        btnUppercase.text = if (item.isUppercase) "MAYÚS" else "Minús"
        fontComboBox.value = item.fontName
        sliderFontSize.value = item.fontSize
        lblFontSize.text = "${item.fontSize.toInt()} px"
        colorPickerFill.value = item.fillColor

        comboStrokeWidth.value = when (item.strokeWidth) {
            4.0 -> "Grueso (4px)"
            2.5 -> "Normal (2.5px)"
            1.5 -> "Fino (1.5px)"
            0.0 -> "Ninguno"
            else -> "Normal (2.5px)"
        }

        updateLayerSelectionStyles()
    }

    private fun deselect() {
        selectedItem?.wrapper?.let {
            it.styleClass.remove("meme-text-selected")
            if (!it.styleClass.contains("meme-text-unselected")) {
                it.styleClass.add("meme-text-unselected")
            }
        }
        selectedItem = null
        textPropertiesGroup.isDisable = true
        textContentArea.clear()
        updateLayerSelectionStyles()
    }

    private fun deleteItem(item: MemeTextItem) {
        memeContainer.children.remove(item.wrapper)
        textItems.remove(item)

        if (selectedItem == item) {
            val next = textItems.lastOrNull()
            if (next != null) {
                selectItem(next)
            } else {
                deselect()
            }
        }
        renderLayersList()
    }

    private fun clearAllTexts() {
        textItems.forEach { memeContainer.children.remove(it.wrapper) }
        textItems.clear()
        deselect()
        renderLayersList()
    }

    private fun updateLayerSelectionStyles() {
        textItems.forEach { item ->
            val isSelected = (item == selectedItem)
            item.rowBox?.let { box ->
                box.styleClass.remove("layer-row-selected")
                if (isSelected) {
                    box.styleClass.add("layer-row-selected")
                }
            }
        }
    }

    private fun renderLayersList() {
        layersListContainer.children.clear()
        lblLayerCount.text = "${textItems.size} ${if (textItems.size == 1) "capa" else "capas"}"
        lblEmptyLayers.isVisible = textItems.isEmpty()
        lblEmptyLayers.isManaged = textItems.isEmpty()

        textItems.forEachIndexed { index, item ->
            val isSelected = (item == selectedItem)
            val row = HBox().apply {
                styleClass.add("layer-row")
                if (isSelected) {
                    styleClass.add("layer-row-selected")
                }
                spacing = 8.0
                alignment = Pos.CENTER_LEFT

                val badge = Label("#${index + 1}").apply {
                    styleClass.add("layer-badge")
                }

                val titleText = if (item.content.isBlank()) "(vacío)" else item.content
                val preview = Label(titleText).apply {
                    styleClass.add("layer-title")
                    maxWidth = 180.0
                    textOverrun = OverrunStyle.ELLIPSIS
                    HBox.setHgrow(this, Priority.ALWAYS)
                }
                item.previewLabel = preview

                val spacer = Region().apply {
                    HBox.setHgrow(this, Priority.ALWAYS)
                }

                val btnDel = Button().apply {
                    styleClass.add("gtk-button-flat")
                    graphic = Icons.create(Icons.TRASH, Color.web("#c0bfbc"), 12.0)
                    setOnAction { e ->
                        e.consume()
                        deleteItem(item)
                    }
                }

                children.addAll(badge, preview, spacer, btnDel)

                setOnMouseClicked {
                    selectItem(item)
                }
            }
            item.rowBox = row
            layersListContainer.children.add(row)
        }
    }

    @FXML
    fun exportMeme() {
        val previouslySelected = selectedItem
        deselect()

        Platform.runLater {
            try {
                val ownerStage = currentStage ?: memeContainer.scene?.window as? Stage

                val fileChooser = FileChooser().apply {
                    title = "Exportar Meme"
                    val defaultName = currentTemplate?.title?.replace(" ", "_") ?: "meme"
                    initialFileName = "$defaultName.png"
                    extensionFilters.addAll(
                        FileChooser.ExtensionFilter("Imagen PNG (*.png)", "*.png"),
                        FileChooser.ExtensionFilter("Imagen JPEG (*.jpg)", "*.jpg", "*.jpeg")
                    )
                }

                val targetFile = fileChooser.showSaveDialog(ownerStage)

                if (targetFile != null) {
                    val format = if (targetFile.name.lowercase().endsWith(".jpg") ||
                        targetFile.name.lowercase().endsWith(".jpeg")
                    ) "jpg" else "png"

                    // Reset zoom temporarily on memeFrame so export is 1:1
                    val prevScaleX = memeFrame.scaleX
                    val prevScaleY = memeFrame.scaleY
                    memeFrame.scaleX = 1.0
                    memeFrame.scaleY = 1.0

                    // Calculate high-resolution scale factor based on original image dimensions
                    val exportScale = maxOf(originalWidth / displayedWidth, 1.0)

                    val params = SnapshotParameters().apply {
                        fill = if (format == "jpg") Color.WHITE else Color.TRANSPARENT
                        transform = javafx.scene.transform.Transform.scale(exportScale, exportScale)
                    }

                    // Snapshot ONLY the clean memeContainer (NO borders, NO shadows, NO editor frame)
                    val snapshot = memeContainer.snapshot(params, null)

                    // Restore editor zoom scale
                    memeFrame.scaleX = prevScaleX
                    memeFrame.scaleY = prevScaleY

                    val bufferedImage = SwingFXUtils.fromFXImage(snapshot, null)

                    if (format == "jpg") {
                        // JPEG does not support alpha channel: paint on solid white background to avoid black borders
                        val rgbImage = java.awt.image.BufferedImage(
                            bufferedImage.width,
                            bufferedImage.height,
                            java.awt.image.BufferedImage.TYPE_INT_RGB
                        )
                        val g2d = rgbImage.createGraphics()
                        g2d.color = java.awt.Color.WHITE
                        g2d.fillRect(0, 0, rgbImage.width, rgbImage.height)
                        g2d.drawImage(bufferedImage, 0, 0, null)
                        g2d.dispose()
                        ImageIO.write(rgbImage, "jpg", targetFile)
                    } else {
                        ImageIO.write(bufferedImage, "png", targetFile)
                    }

                    showToastNotification(targetFile)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showGtkErrorAlert(currentStage, e.message ?: "Ocurrió un error inesperado al procesar la imagen.")
            } finally {
                previouslySelected?.let { selectItem(it) }
            }
        }
    }

    private fun setupToast() {
        toastIconContainer.children.setAll(Icons.create(Icons.CHECK, Color.web("#33d17a"), 16.0))
        btnToastOpenFolder.graphic = Icons.create(Icons.FOLDER_OPEN, Color.WHITE, 12.0)
        btnToastClose.graphic = Icons.create(Icons.CLOSE, Color.web("#9a9996"), 9.0)
        btnToastClose.setOnAction { hideToast() }

        toastContainer.setOnMouseEntered {
            toastHidePause?.pause()
        }
        toastContainer.setOnMouseExited {
            toastHidePause?.play()
        }
    }

    private fun showToastNotification(file: File) {
        toastHidePause?.stop()

        lblToastTitle.text = "¡Meme guardado con éxito!"
        lblToastSubtitle.text = "${file.name}  •  ${originalWidth.toInt()} × ${originalHeight.toInt()} px"

        btnToastOpenFolder.setOnAction {
            openInFileManager(file)
        }

        toastContainer.isVisible = true
        toastContainer.translateY = 20.0
        toastContainer.opacity = 0.0

        val fadeIn = FadeTransition(Duration.millis(220.0), toastContainer).apply {
            fromValue = 0.0
            toValue = 1.0
        }
        val slideIn = TranslateTransition(Duration.millis(220.0), toastContainer).apply {
            fromY = 20.0
            toY = 0.0
        }
        ParallelTransition(fadeIn, slideIn).play()

        toastHidePause = PauseTransition(Duration.seconds(5.0)).apply {
            setOnFinished { hideToast() }
            play()
        }
    }

    private fun hideToast() {
        toastHidePause?.stop()
        if (!toastContainer.isVisible) return

        val fadeOut = FadeTransition(Duration.millis(180.0), toastContainer).apply {
            fromValue = toastContainer.opacity
            toValue = 0.0
        }
        val slideOut = TranslateTransition(Duration.millis(180.0), toastContainer).apply {
            fromY = toastContainer.translateY
            toY = 20.0
        }
        ParallelTransition(fadeOut, slideOut).apply {
            setOnFinished {
                toastContainer.isVisible = false
            }
            play()
        }
    }

    private fun openInFileManager(file: File) {
        try {
            val os = System.getProperty("os.name").lowercase()
            if (os.contains("win")) {
                ProcessBuilder("explorer.exe", "/select,", file.absolutePath).start()
            } else if (os.contains("mac")) {
                ProcessBuilder("open", "-R", file.absolutePath).start()
            } else {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(file.parentFile)
                } else {
                    ProcessBuilder("xdg-open", file.parentFile.absolutePath).start()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showGtkSuccessAlert(owner: Stage?, file: File) {
        showToastNotification(file)
    }

    private fun showGtkErrorAlert(owner: Stage?, message: String) {
        val alert = Alert(Alert.AlertType.ERROR).apply {
            owner?.let { initOwner(it) }
            title = "Error al exportar"
            headerText = "No se pudo guardar la imagen"
            contentText = message

            val css = HelloApplication::class.java.getResource("style.css")
            if (css != null) {
                dialogPane.stylesheets.add(css.toExternalForm())
            }
        }
        alert.showAndWait()
    }
}
