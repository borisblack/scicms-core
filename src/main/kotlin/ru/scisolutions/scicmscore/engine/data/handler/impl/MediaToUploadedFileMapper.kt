package ru.scisolutions.scicmscore.engine.data.handler.impl

import ru.scisolutions.scicmscore.engine.data.model.UploadedFile
import ru.scisolutions.scicmscore.persistence.entity.Media
import java.time.OffsetDateTime
import java.time.ZoneOffset

class MediaToUploadedFileMapper {
    fun map(media: Media) = UploadedFile(
        id = media.id,
        filename = media.filename,
        fileSize = media.fileSize,
        mimetype = media.mimetype,
        checksum = media.checksum,
        createdAt = OffsetDateTime.of(media.createdAt, ZoneOffset.UTC)
    )
}