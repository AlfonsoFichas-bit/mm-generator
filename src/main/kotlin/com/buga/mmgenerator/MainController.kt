package com.buga.mmgenerator

import com.buga.mmgenerator.model.MemeTemplate
import com.buga.mmgenerator.service.TemplateRepository
import com.buga.mmgenerator.ui.Icons
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.OverrunStyle
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextField
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import javafx.stage.Stage
import java.io.File

class MainController {

    @FXML private lateinit var rootContainer: StackPane
    @FXML private lateinit var mainLayout: BorderPane
    @FXML private lateinit var headerBar: HBox
    @FXML private lateinit var searchContainer: HBox
    @FXML private lateinit var searchIconContainer: StackPane
    @FXML private lateinit var searchField: TextField
    @FXML private lateinit var lblTitle: Label
    @FXML private lateinit var headerActions: HBox
    @FXML private lateinit var btnFolder: Button
    @FXML private lateinit var folderIconContainer: StackPane
    @FXML private lateinit var btnMinimize: Button
    @FXML private lateinit var minimizeIconContainer: StackPane
    @FXML private lateinit var btnMaximize: Button
    @FXML private lateinit var maximizeIconContainer: StackPane
    @FXML private lateinit var btnClose: Button
    @FXML private lateinit var closeIconContainer: StackPane
    @FXML private lateinit var sidebar: VBox
    @FXML private lateinit var galleryScroll: ScrollPane
    @FXML private lateinit var galleryFlow: FlowPane

    private var currentCategory = "Todas las plantillas"
    private var selectedTemplate: MemeTemplate? = null
    private val categoryButtons = mutableMapOf<String, Button>()

    private var xOffset = 0.0
    private var yOffset = 0.0

    @FXML
    fun initialize() {
        setupIcons()
        setupSidebar()
        setupSearch()
        setupFolderAction()
        setupWindowControls()

        // Load templates and select rage-Table-Flip-Guy by default (matches reference screenshot)
        val templates = TemplateRepository.loadTemplates()
        selectedTemplate = templates.find { it.filename.contains("Table-Flip", ignoreCase = true) }
            ?: templates.getOrNull(1)

        renderGallery()
    }

    private fun setupIcons() {
        searchIconContainer.children.setAll(Icons.create(Icons.SEARCH, Color.web("#9a9996"), 14.0))
        folderIconContainer.children.setAll(Icons.create(Icons.FOLDER_OPEN, Color.web("#deddda"), 16.0))
        minimizeIconContainer.children.setAll(Icons.create(Icons.MINIMIZE, Color.web("#9a9996"), 11.0))
        maximizeIconContainer.children.setAll(Icons.create(Icons.MAXIMIZE, Color.web("#9a9996"), 11.0))
        closeIconContainer.children.setAll(Icons.create(Icons.CLOSE, Color.web("#9a9996"), 11.0))
    }

    private fun setupSidebar() {
        val categories = listOf(
            "Todas las plantillas" to Icons.DASHBOARD,
            "Favoritos" to Icons.STAR,
            "Personalizadas" to Icons.IMAGE,
            "Rage Comics" to Icons.EMOJI_MEME,
            "Animales" to Icons.ANIMAL_PAW,
            "Personas" to Icons.PEOPLE,
            "Caricaturas" to Icons.SKETCH,
            "Otras" to Icons.MORE
        )

        sidebar.children.clear()

        categories.forEach { (catName, iconPath) ->
            val btn = Button(catName).apply {
                maxWidth = Double.MAX_VALUE
                styleClass.add("category-button")
                graphic = Icons.create(iconPath, Color.web("#c0bfbc"), 15.0)
                graphicTextGap = 12.0
                alignment = Pos.CENTER_LEFT

                setOnAction {
                    selectCategory(catName)
                }
            }
            categoryButtons[catName] = btn
            sidebar.children.add(btn)
        }

        updateSidebarStyles()
    }

    private fun selectCategory(catName: String) {
        currentCategory = catName
        updateSidebarStyles()
        renderGallery()
    }

    private fun updateSidebarStyles() {
        categoryButtons.forEach { (catName, btn) ->
            btn.styleClass.remove("category-button-active")
            val iconPath = when (catName) {
                "Todas las plantillas" -> Icons.DASHBOARD
                "Favoritos" -> Icons.STAR
                "Personalizadas" -> Icons.IMAGE
                "Rage Comics" -> Icons.EMOJI_MEME
                "Animales" -> Icons.ANIMAL_PAW
                "Personas" -> Icons.PEOPLE
                "Caricaturas" -> Icons.SKETCH
                "Otras" -> Icons.MORE
                else -> Icons.DASHBOARD
            }

            if (catName == currentCategory) {
                btn.styleClass.add("category-button-active")
                btn.graphic = Icons.create(iconPath, Color.WHITE, 15.0)
            } else {
                btn.graphic = Icons.create(iconPath, Color.web("#c0bfbc"), 15.0)
            }
        }
    }

    private fun setupSearch() {
        searchField.textProperty().addListener { _, _, _ ->
            renderGallery()
        }
    }

    private fun setupFolderAction() {
        btnFolder.setOnAction {
            val stage = rootContainer.scene?.window as? Stage ?: return@setOnAction
            val chooser = FileChooser().apply {
                title = "Importar plantilla de meme"
                extensionFilters.addAll(
                    FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"),
                    FileChooser.ExtensionFilter("Todos los archivos", "*.*")
                )
            }
            val file: File? = chooser.showOpenDialog(stage)
            if (file != null) {
                val newTemplate = TemplateRepository.addCustomTemplate(file)
                selectedTemplate = newTemplate
                selectCategory("Personalizadas")
            }
        }
    }

    private fun setupWindowControls() {
        Platform.runLater {
            val stage = rootContainer.scene?.window as? Stage ?: return@runLater

            btnMinimize.setOnAction {
                stage.isIconified = true
            }

            btnMaximize.setOnAction {
                stage.isMaximized = !stage.isMaximized
            }

            btnClose.setOnAction {
                stage.close()
            }

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
    }

    private fun renderGallery() {
        galleryFlow.children.clear()

        val query = searchField.text?.trim()?.lowercase() ?: ""
        val allTemplates = TemplateRepository.loadTemplates()

        val filtered = allTemplates.filter { template ->
            val matchesCat = TemplateRepository.matchesCategory(template, currentCategory)
            val matchesQuery = if (query.isEmpty()) {
                true
            } else {
                template.title.lowercase().contains(query) ||
                        template.tags.any { it.lowercase().contains(query) }
            }
            matchesCat && matchesQuery
        }

        filtered.forEach { template ->
            val card = createMemeCard(template)
            galleryFlow.children.add(card)
        }
    }

    private fun createMemeCard(template: MemeTemplate): StackPane {
        val isSelected = (template == selectedTemplate)

        // Card size: ~340px width, ~235px height (2 columns layout matching screenshot)
        val cardWidth = 340.0
        val cardHeight = 232.0

        val cardStack = StackPane().apply {
            prefWidth = cardWidth
            prefHeight = cardHeight
            minWidth = cardWidth
            minHeight = cardHeight
            maxWidth = cardWidth
            maxHeight = cardHeight
            styleClass.add("meme-card")
            if (isSelected) {
                styleClass.add("meme-card-selected")
            }
        }

        val cardContent = VBox().apply {
            alignment = Pos.CENTER
            spacing = 4.0
        }

        // Image Container
        val imgContainer = StackPane().apply {
            prefHeight = 185.0
            maxHeight = 185.0
            alignment = Pos.CENTER
        }

        val imageView = ImageView().apply {
            fitWidth = 270.0
            fitHeight = 175.0
            isPreserveRatio = true
            isSmooth = true
        }

        val img = TemplateRepository.getImage(template)
        if (img != null) {
            imageView.image = img
        }

        imgContainer.children.add(imageView)

        // Bottom Bar (Title + Star Button)
        val bottomBar = HBox().apply {
            alignment = Pos.CENTER_LEFT
            spacing = 8.0
            padding = Insets(2.0, 6.0, 2.0, 6.0)
        }

        val titleLabel = Label(template.title).apply {
            styleClass.add("card-title")
            maxWidth = 270.0
            textOverrun = OverrunStyle.ELLIPSIS
        }

        val spacer = Region().apply {
            HBox.setHgrow(this, Priority.ALWAYS)
        }

        val starBtn = Button().apply {
            styleClass.add("star-btn")
            graphic = if (template.isFavorite) {
                Icons.create(Icons.STAR, Color.web("#f5c211"), 15.0)
            } else {
                Icons.create(Icons.STAR_OUTLINED, Color.web("#77767b"), 15.0)
            }

            setOnAction { e ->
                e.consume()
                template.isFavorite = !template.isFavorite
                graphic = if (template.isFavorite) {
                    Icons.create(Icons.STAR, Color.web("#f5c211"), 15.0)
                } else {
                    Icons.create(Icons.STAR_OUTLINED, Color.web("#77767b"), 15.0)
                }
                if (currentCategory == "Favoritos") {
                    renderGallery()
                }
            }
        }

        bottomBar.children.addAll(titleLabel, spacer, starBtn)
        cardContent.children.addAll(imgContainer, bottomBar)
        cardStack.children.add(cardContent)

        // Selected Overlay Badge (like in reference image)
        if (isSelected) {
            val overlay = VBox().apply {
                styleClass.add("selection-overlay")
                spacing = 2.0
                maxWidth = Region.USE_PREF_SIZE
                maxHeight = Region.USE_PREF_SIZE
                isMouseTransparent = true

                val lblName = Label(template.title).apply {
                    styleClass.add("overlay-title")
                }
                val tagsText = if (template.tags.isNotEmpty()) {
                    "Etiquetas: " + template.tags.joinToString(", ")
                } else {
                    "Etiquetas: other"
                }
                val lblTags = Label(tagsText).apply {
                    styleClass.add("overlay-tags")
                }
                children.addAll(lblName, lblTags)
            }

            StackPane.setAlignment(overlay, Pos.CENTER_LEFT)
            StackPane.setMargin(overlay, Insets(0.0, 0.0, 30.0, 14.0))
            cardStack.children.add(overlay)
        }

        // Card Click Selection
        cardStack.setOnMouseClicked {
            selectedTemplate = template
            renderGallery()
        }

        return cardStack
    }
}
