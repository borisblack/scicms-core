package ru.scisolutions.scicmscore.service.impl

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.Media
import ru.scisolutions.scicmscore.persistence.repository.MediaRepository
import ru.scisolutions.scicmscore.service.MediaService

@Service
@Repository
@Transactional
class MediaServiceImpl(private val mediaRepository: MediaRepository) : MediaService {
    @Transactional(readOnly = true)
    override fun findById(id: String): Media? = mediaRepository.findById(id).orElse(null)

    @Transactional(readOnly = true)
    override fun getById(id: String): Media = mediaRepository.getById(id)

    override fun save(media: Media): Media = mediaRepository.save(media)
}