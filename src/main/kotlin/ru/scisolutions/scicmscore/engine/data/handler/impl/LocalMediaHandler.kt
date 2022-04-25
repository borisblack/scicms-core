package ru.scisolutions.scicmscore.engine.data.handler.impl

import com.google.common.hash.Hashing
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ByteArrayResource
import org.springframework.security.access.annotation.Secured
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.scisolutions.scicmscore.engine.data.handler.MediaHandler
import ru.scisolutions.scicmscore.engine.data.model.MediaInfo
import ru.scisolutions.scicmscore.persistence.entity.Media
import ru.scisolutions.scicmscore.service.MediaService
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID
import com.google.common.io.Files as GFiles

@Service
class LocalMediaHandler(
    @Value("\${scicms-core.media.provider}")
    private val provider: String?,
    @Value("\${scicms-core.media.provider-options.base-path}")
    private val basePath: String?,
    @Value("\${scicms-core.media.provider-options.create-directories:true}")
    private val createDirectories: Boolean,
    private val mediaService: MediaService
) : MediaHandler {
    init {
        if (provider != PROVIDER_LOCAL)
            logger.warn("Local provider is not configured")

        if (basePath.isNullOrBlank())
            logger.warn("Local provider storage path is not configured")
    }

    @Secured("ROLE_UPLOAD", "ROLE_ADMIN")
    override fun upload(file: MultipartFile): MediaInfo {
        val filename = file.originalFilename ?: throw IllegalArgumentException("Filename is null")
        val mimetype = file.contentType ?: throw IllegalArgumentException("Content type is null")
        val filePath = "${UUID.randomUUID()}.${filename.substringAfterLast(".")}"
        val fullPath = buildFullPath(filePath)
        val fileToSave = File(fullPath)
        if (!fileToSave.parentFile.exists() && createDirectories)
            fileToSave.parentFile.mkdirs()

        file.transferTo(fileToSave) // try to save

        val md5 = GFiles.asByteSource(fileToSave).hash(Hashing.md5())
        val media = Media(
            filename = filename,
            fileSize = file.size,
            mimetype = mimetype,
            path = filePath,
            checksum = md5.toString()
        )

        return mediaMapper.map(
            mediaService.save(media)
        )
    }

    private fun buildFullPath(filePath: String): String {
        if (basePath.isNullOrBlank())
            throw IllegalStateException("Local provider storage base path is not configured")

        return "${basePath}/${filePath}"
    }

    @Secured("ROLE_UPLOAD", "ROLE_ADMIN")
    override fun uploadMultiple(files: List<MultipartFile>): List<MediaInfo> =
        files.map { upload(it) }

    override fun download(media: Media): ByteArrayResource {
        val fullPath = buildFullPath(media.path)
        val data = Files.readAllBytes(Paths.get(fullPath))

        return ByteArrayResource(data)
    }

    override fun delete(media: Media) {
        val fullPath = buildFullPath(media.path)
        Files.delete(Paths.get(fullPath))
    }

    companion object {
        private const val PROVIDER_LOCAL = "local"

        private val logger = LoggerFactory.getLogger(LocalMediaHandler::class.java)
        private val mediaMapper = MediaMapper()
    }
}