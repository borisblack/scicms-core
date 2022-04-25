package ru.scisolutions.scicmscore.engine.data.handler

import org.springframework.core.io.ByteArrayResource
import org.springframework.web.multipart.MultipartFile
import ru.scisolutions.scicmscore.engine.data.model.MediaInfo
import ru.scisolutions.scicmscore.persistence.entity.Media

interface MediaHandler {
    fun upload(file: MultipartFile): MediaInfo

    fun uploadMultiple(files: List<MultipartFile>): List<MediaInfo>

    fun download(media: Media): ByteArrayResource

    fun delete(media: Media)
}