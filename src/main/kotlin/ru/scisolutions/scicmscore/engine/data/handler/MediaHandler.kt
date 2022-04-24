package ru.scisolutions.scicmscore.engine.data.handler

import org.springframework.core.io.ByteArrayResource
import org.springframework.web.multipart.MultipartFile
import ru.scisolutions.scicmscore.engine.data.model.UploadedFile
import ru.scisolutions.scicmscore.persistence.entity.Media

interface MediaHandler {
    fun upload(file: MultipartFile): UploadedFile

    fun uploadMultiple(files: List<MultipartFile>): List<UploadedFile>

    fun download(media: Media): ByteArrayResource
}