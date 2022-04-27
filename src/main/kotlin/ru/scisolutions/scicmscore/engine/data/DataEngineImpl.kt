package ru.scisolutions.scicmscore.engine.data

import org.springframework.core.io.ByteArrayResource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.scisolutions.scicmscore.engine.data.handler.CustomMethodHandler
import ru.scisolutions.scicmscore.engine.data.handler.MediaHandler
import ru.scisolutions.scicmscore.engine.data.handler.UserHandler
import ru.scisolutions.scicmscore.engine.data.model.CustomMethodInput
import ru.scisolutions.scicmscore.engine.data.model.MediaInfo
import ru.scisolutions.scicmscore.engine.data.model.UserInfo
import ru.scisolutions.scicmscore.persistence.entity.Media

@Service
class DataEngineImpl(
    private val userHandler: UserHandler,
    private val mediaHandler: MediaHandler,
    private val customMethodHandler: CustomMethodHandler
) : DataEngine {
    override fun me(): UserInfo? = userHandler.me()

    override fun upload(file: MultipartFile): MediaInfo = mediaHandler.upload(file)

    override fun uploadMultiple(files: List<MultipartFile>): List<MediaInfo> = mediaHandler.uploadMultiple(files)

    override fun download(media: Media): ByteArrayResource = mediaHandler.download(media)

    override fun getCustomMethods(itemName: String): Set<String> = customMethodHandler.getCustomMethods(itemName)

    override fun callCustomMethod(itemName: String, methodName: String, customMethodInput: CustomMethodInput) =
        customMethodHandler.callCustomMethod(itemName, methodName, customMethodInput)
}