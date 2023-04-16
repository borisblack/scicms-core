package ru.scisolutions.scicmscore.persistence.service.impl

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.Media
import ru.scisolutions.scicmscore.persistence.repository.MediaRepository
import ru.scisolutions.scicmscore.persistence.service.MediaService
import ru.scisolutions.scicmscore.persistence.service.PermissionCache
import ru.scisolutions.scicmscore.util.Acl.Mask

@Service
@Repository
@Transactional
class MediaServiceImpl(
    private val permissionCache: PermissionCache,
    private val mediaRepository: MediaRepository
) : MediaService {
    @Transactional(readOnly = true)
    override fun findById(id: String): Media? = mediaRepository.findById(id).orElse(null)

    @Transactional(readOnly = true)
    override fun findByIdForRead(id: String): Media? = findByIdFor(id, Mask.READ)

    @Transactional(readOnly = true)
    override fun findByIdForDelete(id: String): Media? = findByIdFor(id, Mask.DELETE)

    private fun findByIdFor(id: String, accessMask: Mask): Media? =
        mediaRepository.findByIdWithACL(id, permissionCache.idsByAccessMask(accessMask))

    @Transactional(readOnly = true)
    override fun getById(id: String): Media = mediaRepository.getById(id)

    override fun existsById(id: String): Boolean = mediaRepository.existsById(id)

    override fun save(media: Media): Media = mediaRepository.save(media)
}