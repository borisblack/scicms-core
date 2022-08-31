package ru.scisolutions.scicmscore.engine.handler

import org.springframework.core.io.ByteArrayResource
import ru.scisolutions.scicmscore.engine.model.MediaInfo
import ru.scisolutions.scicmscore.engine.model.input.UploadInput
import java.util.UUID

interface MediaHandler {
    fun upload(uploadInput: UploadInput): MediaInfo

    fun uploadMultiple(uploadInputList: List<UploadInput>): List<MediaInfo>

    fun downloadById(id: UUID): ByteArrayResource

    fun deleteById(id: UUID)
}