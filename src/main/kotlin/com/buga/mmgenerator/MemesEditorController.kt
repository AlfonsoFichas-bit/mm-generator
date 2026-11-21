package com.buga.mmgenerator

import com.buga.mmgenerator.model.MemeTemplate
import com.buga.mmgenerator.service.TemplateRepository
import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.embed.swing.SwingFXUtils
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.image.WritableImage
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.stage.FileChooser
import javafx.stage.Stage
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException

class MemesEditorController {

    // UI Components from FXML
    @FXML
    private lateinit var memeCanvas: AnchorPane

    @FXML
    private lateinit var imageView: ImageView

    @FXML
    private lateinit var textContentField: TextField

    @FXML
    private lateinit var fontSizeField: TextField

    @FXML
    private lateinit var posXField: TextField

    @FXML
    private lateinit var posYField: TextField

    @FXML
    private lateinit var selectedColorLabel: Label

    @FXML
    private lateinit var colorBtn: Button

    @FXML
    private lateinit var deleteTextBtn: Button

    @FXML
    private lateinit var exportBtn: Button

    @FXML
    private lateinit var textBoxesList: ListView<String>

    // State management
    private var currentTemplate: MemeTemplate? = null
    private var selectedTextBox: TextBoxData? = null
    private val textBoxes = mutableListOf<TextBoxData>()
    private val colorPicker = ColorPicker()

    // Data class to track text box properties
    data class TextBoxData(
        val id: Int = 0,
        val textNode: TextFlow = TextFlow(),
        val content: StringProperty = SimpleStringProperty(""),
        val fontSize: IntegerProperty = SimpleIntegerProperty(24),
        val textColor: StringProperty = SimpleStringProperty("#ffffff"),
        val translateX: IntegerProperty = SimpleIntegerProperty(0),
        val translateY: IntegerProperty = SimpleIntegerProperty(0)
    )

    // Initialize the controller
    @FXML
    fun initialize() {
        // Initialize color picker
        colorPicker.value = Color.web("#ffffff")
        colorPicker.setOnAction(EventHandler<ActionEvent> { _ ->
            updateSelectedColorPreview(colorPicker.value)
        })

        // Setup list view - listen to selection changes instead
        textBoxesList.selectionModelProperty().addListener { _, _, _ ->
            // Selection changed, but we don't need to update list here
        }

        // Setup default values
        fontSizeField.text = "24"
        posXField.text = "0"
        posYField.text = "0"

        // Canvas click handling for adding text
        memeCanvas.setOnMouseClicked(EventHandler<MouseEvent> { mouseEvent ->
            if (mouseEvent.button == MouseButton.PRIMARY && mouseEvent.clickCount == 1) {
                // Check if clicked on an existing text box
                if (selectedTextBox != null) {
                    // Deselect and allow new text box creation
                    deselectTextBox()
                } else {
                    // Add new text box at click position
                    // Convert scene coordinates to local canvas coordinates
                    val localPoint = memeCanvas.sceneToLocal(mouseEvent.sceneX, mouseEvent.sceneY)
                    addTextBoxAtPosition(localPoint.x, localPoint.y)
                }
            }
        })
    }

    // Set the current template and display the image
    fun setTemplate(template: MemeTemplate?) {
        currentTemplate = template

        if (template != null) {
            val image = TemplateRepository.getImage(template)
            if (image != null) {
                imageView.image = image
                imageView.fitWidth = memeCanvas.width
                imageView.fitHeight = memeCanvas.height
                imageView.setPreserveRatio(true)
            } else {
                // Placeholder when no image available
                imageView.image = null
                // Show placeholder text
                val placeholder = Text("No image found").apply {
                    layoutX = memeCanvas.width / 2 - 50
                    layoutY = memeCanvas.height / 2 - 10
                    fill = Color.GRAY
                }
                memeCanvas.children.add(placeholder)
            }
        } else {
            imageView.image = null
            memeCanvas.children.clear()
        }

        // Clear text boxes when new template is selected
        if (template == null) {
            clearAllTextBoxes()
        }
    }

    // Add a new text box at center (used by + button in overlay)
    @FXML
    fun addTextBox() {
        val centerX = memeCanvas.width / 2
        val centerY = memeCanvas.height / 2
        addTextBoxAtPosition(centerX, centerY)
    }

    // Add a new text box at the given position (used by canvas click)
    @FXML
    fun addTextBoxAtPosition(x: Double, y: Double) {
        val textFlow = TextFlow().apply {
            styleClass.add("editable-text")
            setPickOnBounds(true)
            layoutX = x - 100 // Center text at click position approximately
            layoutY = y - 12 / 2
            maxWidth = 200.0
            // wrapText is not a property of TextFlow, handled by Text nodes
        }

        val textContent = Text("Nuevo texto").apply {
            style = "-fx-font-size: 24px; -fx-text-fill: #ffffff;"
            wrappingWidth = 200.0
        }
        textFlow.children.add(textContent)

        // Make text draggable
        makeDraggable(textFlow)

        // Add to canvas
        memeCanvas.children.add(textFlow)

        // Create data tracking
        val textData = TextBoxData(
            id = textBoxes.size + 1,
            textNode = textFlow,
            content = SimpleStringProperty("Nuevo texto"),
            fontSize = SimpleIntegerProperty(24),
            textColor = SimpleStringProperty("#ffffff"),
            translateX = SimpleIntegerProperty(textFlow.layoutX.toInt()),
            translateY = SimpleIntegerProperty(textFlow.layoutY.toInt())
        )
        textBoxes.add(textData)

        // Select the new text box
        selectTextBox(textData)

        // Update list
        updateTextBoxesList()
    }

    // Make a text flow draggable
    private fun makeDraggable(node: Node) {
        var dragX = 0.0
        var dragY = 0.0
        var isDragging = false

        node.onMousePressed = EventHandler<MouseEvent> { event ->
            isDragging = true
            dragX = event.sceneX - node.layoutX
            dragY = event.sceneY - node.layoutY
            node.toFront()
        }

        node.onMouseDragged = EventHandler<MouseEvent> { event ->
            if (isDragging) {
                node.layoutX = event.sceneX - dragX
                node.layoutY = event.sceneY - dragY

                // Update data
                selectedTextBox?.let { data ->
                    data.translateX.set(node.layoutX.toInt())
                    data.translateY.set(node.layoutY.toInt())
                }
            }
        }

        node.onMouseReleased = EventHandler<MouseEvent> { _ ->
            isDragging = false
        }
    }

    // Select a text box for editing
    private fun selectTextBox(textData: TextBoxData) {
        selectedTextBox = textData

        // Update UI fields
        textContentField.text = textData.content.get()
        fontSizeField.text = textData.fontSize.get().toString()
        posXField.text = textData.translateX.get().toString()
        posYField.text = textData.translateY.get().toString()
        updateSelectedColorPreview(Color.web(textData.textColor.get()))

        // Highlight in list
        val selectionModel = textBoxesList.selectionModel
        selectionModel?.select(textData.id - 1)

        // Add style class to visualize selection
        textData.textNode.styleClass.add("selected-text")
    }

    // Deselect the current text box
    private fun deselectTextBox() {
        selectedTextBox?.let { textData ->
            textData.textNode.styleClass.remove("selected-text")
        }
        selectedTextBox = null

        // Clear UI fields
        textContentField.clear()
        fontSizeField.clear()
        posXField.clear()
        posYField.clear()
        selectedColorLabel.text = "Ningún color"
        selectedColorLabel.style = ""

        // Clear list selection
        val selectionModel = textBoxesList.selectionModel
        selectionModel?.clearSelection()
    }

    // Update the preview of selected color
    private fun updateSelectedColorPreview(color: Color) {
        val hexColor = String.format(
            "#%02X%02X%02X",
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt()
        )
        selectedColorLabel.style =
            "-fx-background-color: $hexColor; -fx-background-radius: 4px; -fx-min-width: 30; -fx-min-height: 30;"
        selectedColorLabel.text = ""
    }

    // Update text content
    @FXML
    fun updateSelectedText() {
        selectedTextBox?.let { textData ->
            textData.content.set(textContentField.text)
            // Update the actual text node
            textData.textNode.children.forEach { node ->
                if (node is Text) {
                    node.text = textContentField.text
                }
            }
        }
    }

    // Update font size
    @FXML
    fun updateFontSize() {
        try {
            val size = fontSizeField.text.toInt().coerceAtLeast(8).coerceAtMost(128)
            selectedTextBox?.let { textData ->
                textData.fontSize.set(size)
                updateTextNodeFontSize(textData)
            }
        } catch (e: NumberFormatException) {
            // Ignore invalid input
        }
    }

    // Update text node font size
    private fun updateTextNodeFontSize(textData: TextBoxData) {
        val fontSize = textData.fontSize.get().toDouble()
        textData.textNode.children.forEach { node ->
            if (node is Text) {
                node.font = Font.font(null, fontSize)
            }
        }
    }

    // Update position
    @FXML
    fun updatePosition() {
        selectedTextBox?.let { textData ->
            try {
                val x = posXField.text.toInt()
                val y = posYField.text.toInt()
                textData.translateX.set(x)
                textData.translateY.set(y)

                // Update the actual text flow position
                textData.textNode.layoutX = x.toDouble()
                textData.textNode.layoutY = y.toDouble()
            } catch (e: NumberFormatException) {
                // Ignore invalid input
            }
        }
    }

    // Delete selected text box
    @FXML
    fun deleteSelectedText() {
        selectedTextBox?.let { textData ->
            // Remove from canvas
            memeCanvas.children.remove(textData.textNode)

            // Remove from tracking
            textBoxes.remove(textData)

            // Deselect
            deselectTextBox()

            // Update list
            updateTextBoxesList()
        }
    }

    // Export the meme as an image
    @FXML
    fun exportMeme() {
        // Take snapshot of the canvas
        val snapshot = memeCanvas.snapshot(null, WritableImage(memeCanvas.width.toInt(), memeCanvas.height.toInt()))

        if (snapshot != null) {
            val fileChooser = FileChooser().apply {
                title = "Guardar meme como"
                extensionFilters.addAll(
                    FileChooser.ExtensionFilter("PNG", "*.png"),
                    FileChooser.ExtensionFilter("JPEG", "*.jpg"),
                    FileChooser.ExtensionFilter("All files", "*.*")
                )
            }

            val stage = memeCanvas.scene?.window as? Stage ?: return
            val file: File? = fileChooser.showSaveDialog(stage)

            if (file != null) {
                try {
                    val format = if (file.name.lowercase().endsWith(".jpg") ||
                        file.name.lowercase().endsWith(".jpeg")
                    ) "jpg" else "png"

                    // Convert JavaFX Image to BufferedImage for ImageIO
                    val bufferedImage: BufferedImage = SwingFXUtils.fromFXImage(snapshot, null)
                    ImageIO.write(bufferedImage, format, file)

                    // Show success indication
                    val alert = Alert(Alert.AlertType.INFORMATION)
                    alert.title = "Éxito"
                    alert.contentText = "Meme guardado exitosamente en: ${file.absolutePath}"
                    alert.showAndWait()
                } catch (e: IOException) {
                    e.printStackTrace()
                    val alert = Alert(Alert.AlertType.ERROR)
                    alert.title = "Error"
                    alert.contentText = "No se pudo guardar el meme: ${e.message}"
                    alert.showAndWait()
                }
            }
        } else {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.title = "Error"
            alert.contentText = "No se pudo capturar la imagen del editor"
            alert.showAndWait()
        }
    }

    // Select color for text
    @FXML
    fun selectColor() {
        colorPicker.show()
    }

    // Update text boxes list view
    private fun updateTextBoxesList() {
        textBoxesList.items?.clear()
        textBoxes.forEach { textData ->
            textBoxesList.items?.add("Texto ${textData.id}: ${textData.content.get() ?: "vacío"}")
        }
    }

    // Clear all text boxes
    private fun clearAllTextBoxes() {
        memeCanvas.children.clear()
        textBoxes.clear()
        textBoxesList.items?.clear()
        deselectTextBox()
    }


    /** Decrease font size */
    @FXML
    fun decreaseFontSize() {
        selectedTextBox?.let { textData ->
            var currentSize = textData.fontSize.get()
            if (currentSize > 8) {
                currentSize--
                textData.fontSize.set(currentSize)
                updateTextNodeFontSize(textData)
                fontSizeField.text = currentSize.toString()
            }
        }
    }

    /** Increase font size */
    @FXML
    fun increaseFontSize() {
        selectedTextBox?.let { textData ->
            var currentSize = textData.fontSize.get()
            if (currentSize < 128) {
                currentSize++
                textData.fontSize.set(currentSize)
                updateTextNodeFontSize(textData)
                fontSizeField.text = currentSize.toString()
            }
        }
    }
}
