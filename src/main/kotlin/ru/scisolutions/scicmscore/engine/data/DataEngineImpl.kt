package ru.scisolutions.scicmscore.engine.data

import org.springframework.core.io.ByteArrayResource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.scisolutions.scicmscore.engine.data.handler.MediaHandler
import ru.scisolutions.scicmscore.engine.data.handler.UserHandler
import ru.scisolutions.scicmscore.engine.data.model.UploadedFile
import ru.scisolutions.scicmscore.engine.data.model.UserInfo
import ru.scisolutions.scicmscore.persistence.entity.Media

@Service
class DataEngineImpl(
    private val userHandler: UserHandler,
    private val mediaHandler: MediaHandler
) : DataEngine {
    override fun me(): UserInfo? = userHandler.me()

    override fun upload(file: MultipartFile): UploadedFile = mediaHandler.upload(file)

    override fun uploadMultiple(files: List<MultipartFile>): List<UploadedFile> = mediaHandler.uploadMultiple(files)

    override fun download(media: Media): ByteArrayResource = mediaHandler.download(media)
}