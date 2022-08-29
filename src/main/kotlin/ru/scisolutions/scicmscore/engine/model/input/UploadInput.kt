package ru.scisolutions.scicmscore.engine.model.input

import org.springframework.web.multipart.MultipartFile

class UploadInput(
    val file: MultipartFile,
    val label: String?,
    val description: String?,
    val permissionId: String?
)