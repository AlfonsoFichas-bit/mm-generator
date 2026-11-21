package com.buga.mmgenerator.model

import java.io.File

data class MemeTemplate(
    val title: String,
    val filename: String,
    val tags: List<String> = emptyList(),
    var isFavorite: Boolean = false,
    var isCustom: Boolean = false,
    var customFile: File? = null
)
