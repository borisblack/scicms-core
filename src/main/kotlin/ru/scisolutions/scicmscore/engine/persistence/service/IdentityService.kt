package ru.scisolutions.scicmscore.engine.persistence.service

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.engine.persistence.entity.Identity
import ru.scisolutions.scicmscore.engine.persistence.repository.IdentityRepository

@Service
@Repository
@Transactional
class IdentityService(private val identityRepository: IdentityRepository) {
    @Transactional
    fun findByUsername(username: String): Identity? = identityRepository.findByNameAndPrincipal(username, true)

    fun deleteByUsername(username: String): Int = identityRepository.deleteByNameAndPrincipal(username, true)
}
