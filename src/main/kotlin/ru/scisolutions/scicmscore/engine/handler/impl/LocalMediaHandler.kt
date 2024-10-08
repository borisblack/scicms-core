package ru.scisolutions.scicmscore.engine.handler.impl

import com.google.common.hash.Hashing
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.io.ByteArrayResource
import org.springframework.security.access.annotation.Secured
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import ru.scisolutions.scicmscore.config.props.MediaProps
import ru.scisolutions.scicmscore.engine.handler.MediaHandler
import ru.scisolutions.scicmscore.engine.mapper.MediaMapper
import ru.scisolutions.scicmscore.engine.model.MediaInfo
import ru.scisolutions.scicmscore.engine.model.input.UploadInput
import ru.scisolutions.scicmscore.engine.persistence.entity.Media
import ru.scisolutions.scicmscore.engine.persistence.service.ItemService
import ru.scisolutions.scicmscore.engine.persistence.service.MediaService
import ru.scisolutions.scicmscore.engine.service.PermissionManager
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import com.google.common.io.Files as GFiles

@Service
@ConditionalOnProperty(
    prefix = "scicms-core.media",
    name = ["provider"],
    havingValue = "local",
    matchIfMissing = false
)
class LocalMediaHandler(
    private val mediaProps: MediaProps,
    private val mediaService: MediaService,
    private val itemService: ItemService,
    private val permissionManager: PermissionManager
) : MediaHandler {
    init {
        if (mediaProps.provider != MediaProps.PROVIDER_LOCAL) {
            logger.warn("Local provider is not configured")
        }

        if (mediaProps.providerOptions.local.basePath.isNullOrBlank()) {
            logger.warn("Local provider storage path is not configured")
        }
    }

    @Secured("ROLE_UPLOAD", "ROLE_ADMIN")
    override fun upload(file: MultipartFile): MediaInfo {
        val filename = file.originalFilename ?: throw IllegalArgumentException("Filename is null")
        val mimetype = file.contentType ?: throw IllegalArgumentException("Content type is null")
        val filePath = "${UUID.randomUUID()}.${filename.substringAfterLast(".")}"
        val fullPath = buildFullPath(filePath)
        val fileToSave = File(fullPath)
        if (!fileToSave.parentFile.exists() && mediaProps.providerOptions.local.createDirectories) {
            fileToSave.parentFile.mkdirs()
        }

        file.transferTo(fileToSave) // try to save

        val md5 = GFiles.asByteSource(fileToSave).hash(Hashing.md5())
        val media =
            Media(
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
        if (mediaProps.providerOptions.local.basePath.isNullOrBlank()) {
            throw IllegalStateException("Local provider storage base path is not configured")
        }

        return "${mediaProps.providerOptions.local.basePath}/$filePath"
    }

    @Secured("ROLE_UPLOAD", "ROLE_ADMIN")
    override fun uploadMultiple(files: List<MultipartFile>): List<MediaInfo> = files.map { upload(it) }

    @Secured("ROLE_UPLOAD", "ROLE_ADMIN")
    override fun uploadData(uploadInput: UploadInput): MediaInfo {
        val file = uploadInput.file
        val filename = file.submittedFileName ?: throw IllegalArgumentException("Filename is null")
        val mimetype = file.contentType ?: throw IllegalArgumentException("Content type is null")
        val filePath = "${UUID.randomUUID()}.${filename.substringAfterLast(".")}"
        val fullPath = buildFullPath(filePath)
        val fileToSave = File(fullPath)
        if (!fileToSave.parentFile.exists() && mediaProps.providerOptions.local.createDirectories) {
            fileToSave.parentFile.mkdirs()
        }

        file.write(fullPath) // try to save

        val md5 = GFiles.asByteSource(fileToSave).hash(Hashing.md5())
        val media =
            Media(
                filename = filename,
                label = uploadInput.label,
                description = uploadInput.description,
                fileSize = file.size,
                mimetype = mimetype,
                path = filePath,
                checksum = md5.toString()
            ).apply {
                permissionId = permissionManager.checkPermissionId(itemService.getMedia(), uploadInput.permissionId)
            }

        return mediaMapper.map(
            mediaService.save(media)
        )
    }

    @Secured("ROLE_UPLOAD", "ROLE_ADMIN")
    override fun uploadDataMultiple(uploadInputList: List<UploadInput>): List<MediaInfo> = uploadInputList.map { uploadData(it) }

    override fun downloadById(id: String): ByteArrayResource {
        val media =
            mediaService.findByIdForRead(id)
                ?: throw IllegalArgumentException("Media with ID [$id] not found")

        val fullPath = buildFullPath(media.path)
        val data = Files.readAllBytes(Paths.get(fullPath))

        return ByteArrayResource(data)
    }

    override fun deleteById(id: String) {
        val media =
            mediaService.findByIdForDelete(id)
                ?: throw IllegalArgumentException("Media with ID [$id] not found")

        mediaService.delete(media)

        val fullPath = buildFullPath(media.path)
        Files.delete(Paths.get(fullPath))
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LocalMediaHandler::class.java)
        private val mediaMapper = MediaMapper()
    }
}
