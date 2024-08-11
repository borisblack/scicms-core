package ru.scisolutions.scicmscore.engine.model

import java.time.OffsetDateTime

class MediaInfo(
    val id: String,
    val filename: String,
    val label: String? = null,
    val description: String? = null,
    val fileSize: Long,
    val mimetype: String?,
    val checksum: String,
    val createdAt: OffsetDateTime,
)
