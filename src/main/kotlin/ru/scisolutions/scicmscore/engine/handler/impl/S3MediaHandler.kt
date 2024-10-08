package ru.scisolutions.scicmscore.engine.handler.impl

import io.minio.GetObjectArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.io.ByteArrayResource
import org.springframework.security.access.annotation.Secured
import org.springframework.stereotype.Service
import org.springframework.util.DigestUtils
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
import java.util.UUID

@Service
@ConditionalOnProperty(
    prefix = "scicms-core.media",
    name = ["provider"],
    havingValue = "s3",
    matchIfMissing = false
)
class S3MediaHandler(
    private val mediaProps: MediaProps,
    private val mediaService: MediaService,
    private val itemService: ItemService,
    private val permissionManager: PermissionManager
) : MediaHandler {
    init {
        if (mediaProps.provider != MediaProps.PROVIDER_S3) {
            logger.warn("S3 provider is not configured")
        }

        if (mediaProps.providerOptions.s3.endpoint.isNullOrBlank()) {
            logger.warn("S3 provider endpoint is not configured")
        }
    }

    @Secured("ROLE_UPLOAD", "ROLE_ADMIN")
    override fun upload(file: MultipartFile): MediaInfo {
        val filename = file.originalFilename ?: throw IllegalArgumentException("Filename is null")
        val mimetype = file.contentType ?: throw IllegalArgumentException("Content type is null")
        val filenameToStore = "${UUID.randomUUID()}.${filename.substringAfterLast(".")}"

        // Try to save
        minioClient().putObject(
            PutObjectArgs
                .builder()
                .bucket(mediaProps.providerOptions.s3.defaultBucket)
                .`object`(filenameToStore)
                .contentType(mimetype)
                .stream(file.inputStream, file.size, DEFAULT_PART_SIZE)
                .build()
        )

        val media =
            Media(
                filename = filename,
                fileSize = file.size,
                mimetype = mimetype,
                path = filenameToStore,
                checksum = DigestUtils.md5DigestAsHex(file.inputStream)
            )

        return mediaMapper.map(
            mediaService.save(media)
        )
    }

    private fun minioClient() = MinioClient.builder()
        .endpoint(mediaProps.providerOptions.s3.endpoint)
        .credentials(
            mediaProps.providerOptions.s3.accessKey,
            mediaProps.providerOptions.s3.secretKey
        )
        .build()

    @Secured("ROLE_UPLOAD", "ROLE_ADMIN")
    override fun uploadMultiple(files: List<MultipartFile>): List<MediaInfo> = files.map { upload(it) }

    @Secured("ROLE_UPLOAD", "ROLE_ADMIN")
    override fun uploadData(uploadInput: UploadInput): MediaInfo {
        val file = uploadInput.file
        val filename = file.submittedFileName ?: throw IllegalArgumentException("Filename is null")
        val mimetype = file.contentType ?: throw IllegalArgumentException("Content type is null")
        val filenameToStore = "${UUID.randomUUID()}.${filename.substringAfterLast(".")}"

        // Try to save
        minioClient().putObject(
            PutObjectArgs
                .builder()
                .bucket(mediaProps.providerOptions.s3.defaultBucket)
                .`object`(filenameToStore)
                .contentType(mimetype)
                .stream(file.inputStream, file.size, DEFAULT_PART_SIZE)
                .build()
        )

        val media =
            Media(
                filename = filename,
                label = uploadInput.label,
                description = uploadInput.description,
                fileSize = file.size,
                mimetype = mimetype,
                path = filenameToStore,
                checksum = DigestUtils.md5DigestAsHex(file.inputStream)
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

        val data =
            minioClient().getObject(
                GetObjectArgs
                    .builder()
                    .bucket(mediaProps.providerOptions.s3.defaultBucket)
                    .`object`(media.path)
                    .build()
            )
                .readAllBytes()

        return ByteArrayResource(data)
    }

    override fun deleteById(id: String) {
        val media =
            mediaService.findByIdForDelete(id)
                ?: throw IllegalArgumentException("Media with ID [$id] not found")

        mediaService.delete(media)

        minioClient().removeObject(
            RemoveObjectArgs
                .builder()
                .bucket(mediaProps.providerOptions.s3.defaultBucket)
                .`object`(media.path)
                .build()
        )
    }

    companion object {
        private const val DEFAULT_PART_SIZE: Long = 10485760 // 10 * 1024 * 1024

        private val logger = LoggerFactory.getLogger(S3MediaHandler::class.java)
        private val mediaMapper = MediaMapper()
    }
}
