package ru.scisolutions.scicmscore.engine.handler

import org.springframework.core.io.ByteArrayResource
import org.springframework.web.multipart.MultipartFile
import ru.scisolutions.scicmscore.engine.model.MediaInfo
import ru.scisolutions.scicmscore.engine.model.input.UploadInput

interface MediaHandler {
    fun upload(file: MultipartFile): MediaInfo

    fun uploadMultiple(files: List<MultipartFile>): List<MediaInfo>

    fun uploadData(uploadInput: UploadInput): MediaInfo

    fun uploadDataMultiple(uploadInputList: List<UploadInput>): List<MediaInfo>

    fun downloadById(id: String): ByteArrayResource

    fun deleteById(id: String)
}
