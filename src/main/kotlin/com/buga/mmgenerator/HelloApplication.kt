package com.buga.mmgenerator

import atlantafx.base.theme.PrimerDark
import com.buga.mmgenerator.ui.ResizeHelper
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.paint.Color
import javafx.stage.Stage
import javafx.stage.StageStyle

class HelloApplication : Application() {
    override fun start(stage: Stage) {
        // Set application icon
        val iconStream = HelloApplication::class.java.getResourceAsStream("icon.png")
        if (iconStream != null) {
            stage.icons.add(Image(iconStream))
        }

        // Apply AtlantaFX PrimerDark base
        Application.setUserAgentStylesheet(PrimerDark().userAgentStylesheet)

        val fxmlLoader = FXMLLoader(HelloApplication::class.java.getResource("main-view.fxml"))
        val root = fxmlLoader.load<Parent>()

        val scene = Scene(root, 980.0, 640.0)
        scene.fill = Color.TRANSPARENT

        // Apply custom GTK Adwaita stylesheet
        val customCss = HelloApplication::class.java.getResource("style.css")
        if (customCss != null) {
            scene.stylesheets.add(customCss.toExternalForm())
        }

        stage.initStyle(StageStyle.TRANSPARENT)
        stage.title = "Creador de Memes"
        stage.scene = scene
        stage.minWidth = 800.0
        stage.minHeight = 520.0

        ResizeHelper.addResizeListener(stage)

        stage.show()
    }
}
