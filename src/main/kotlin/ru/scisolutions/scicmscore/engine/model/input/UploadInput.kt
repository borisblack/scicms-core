package ru.scisolutions.scicmscore.engine.model.input

import jakarta.servlet.http.Part

class UploadInput(
    val file: Part,
    val label: String?,
    val description: String?,
    val permissionId: String?,
)
