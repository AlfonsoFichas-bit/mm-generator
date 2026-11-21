
    /** Opens the meme editor for the selected template */
    @FXML
    fun openMemeEditor(template: MemeTemplate?) {
        // TODO: Implement editor opening - will be connected to the FXML view
        if (template != null) {
            // Navigate to editor view - this will be implemented when the editor FXML is loaded
            println("Opening editor for template: ${template.title}")
        }
    }
