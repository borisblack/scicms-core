package ru.scisolutions.scicmscore.engine.data.mapper

import ru.scisolutions.scicmscore.engine.data.model.MediaInfo
import ru.scisolutions.scicmscore.persistence.entity.Media

class MediaMapper {
    fun map(media: Media) = MediaInfo(
        id = media.id,
        filename = media.filename,
        fileSize = media.fileSize,
        mimetype = media.mimetype,
        checksum = media.checksum,
        // createdAt = media.createdAt.atZone(ZoneOffset.systemDefault()).toOffsetDateTime()
        createdAt = media.createdAt
    )
}