package ru.scisolutions.scicmscore.engine.handler

import org.springframework.core.io.ByteArrayResource
import ru.scisolutions.scicmscore.engine.model.MediaInfo
import ru.scisolutions.scicmscore.engine.model.input.UploadInput

interface MediaHandler {
    fun upload(uploadInput: UploadInput): MediaInfo

    fun uploadMultiple(uploadInputList: List<UploadInput>): List<MediaInfo>

    fun downloadById(id: String): ByteArrayResource

    fun deleteById(id: String)
}