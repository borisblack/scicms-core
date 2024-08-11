package ru.scisolutions.scicmscore.engine.mapper

import ru.scisolutions.scicmscore.engine.model.MediaInfo
import ru.scisolutions.scicmscore.engine.persistence.entity.Media

class MediaMapper {
    fun map(media: Media) = MediaInfo(
        id = media.id,
        filename = media.filename,
        fileSize = media.fileSize,
        mimetype = media.mimetype,
        checksum = media.checksum,
        createdAt = media.createdAt,
    )
}
