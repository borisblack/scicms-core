package ru.scisolutions.scicmscore.engine.persistence.service

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.engine.persistence.repository.AccessRepository

@Service
@Repository
@Transactional
class AccessService(private val accessRepository: AccessRepository) {
    fun deleteAllByIdentityId(identityId: String): Int = accessRepository.deleteAllByTargetId(identityId)
}
