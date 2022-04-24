package ru.scisolutions.scicmscore.engine.data.model

import java.time.OffsetDateTime

class UploadedFile(
    val id: String,
    val filename: String,
    val displayName: String? = null,
    val description: String? = null,
    val fileSize: Long,
    val mimetype: String?,
    val checksum: String,
    val createdAt: OffsetDateTime
)