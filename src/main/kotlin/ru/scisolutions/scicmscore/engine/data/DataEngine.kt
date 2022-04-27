package ru.scisolutions.scicmscore.engine.data

import org.springframework.core.io.ByteArrayResource
import org.springframework.web.multipart.MultipartFile
import ru.scisolutions.scicmscore.engine.data.model.CustomMethodInput
import ru.scisolutions.scicmscore.engine.data.model.CustomMethodResponse
import ru.scisolutions.scicmscore.engine.data.model.MediaInfo
import ru.scisolutions.scicmscore.engine.data.model.UserInfo
import ru.scisolutions.scicmscore.persistence.entity.Media

interface DataEngine {
    fun me(): UserInfo?

    fun upload(file: MultipartFile): MediaInfo

    fun uploadMultiple(files: List<MultipartFile>): List<MediaInfo>

    fun download(media: Media): ByteArrayResource

    fun getCustomMethods(itemName: String): Set<String>

    fun callCustomMethod(itemName: String, methodName: String, customMethodInput: CustomMethodInput): CustomMethodResponse
}