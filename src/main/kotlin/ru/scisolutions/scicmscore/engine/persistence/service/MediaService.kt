package ru.scisolutions.scicmscore.engine.persistence.service

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.engine.persistence.entity.Media
import ru.scisolutions.scicmscore.engine.persistence.repository.MediaRepository
import ru.scisolutions.scicmscore.engine.util.Acl

@Service
@Repository
@Transactional
class MediaService(
    private val permissionService: PermissionService,
    private val mediaRepository: MediaRepository
) {
    @Transactional(readOnly = true)
    fun findById(id: String): Media? = mediaRepository.findById(id).orElse(null)

    @Transactional(readOnly = true)
    fun findByIdForRead(id: String): Media? = findByIdFor(id, Acl.Mask.READ)

    @Transactional(readOnly = true)
    fun findByIdForDelete(id: String): Media? = findByIdFor(id, Acl.Mask.DELETE)

    private fun findByIdFor(id: String, accessMask: Acl.Mask): Media? =
        mediaRepository.findByIdWithACL(id, permissionService.idsByAccessMask(accessMask))

    @Transactional(readOnly = true)
    fun getById(id: String): Media = mediaRepository.findById(id).orElseThrow {
        IllegalArgumentException("Media [$id] not found")
    }

    fun existsById(id: String): Boolean = mediaRepository.existsById(id)

    fun save(media: Media): Media = mediaRepository.save(media)

    fun delete(media: Media) = mediaRepository.delete(media)
}
