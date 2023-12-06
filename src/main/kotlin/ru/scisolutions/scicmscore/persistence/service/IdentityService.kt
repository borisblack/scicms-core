package ru.scisolutions.scicmscore.persistence.service

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.Identity
import ru.scisolutions.scicmscore.persistence.repository.IdentityRepository

@Service
@Repository
@Transactional
class IdentityService(private val identityRepository: IdentityRepository) {
    @Transactional
    fun findByUsername(username: String): Identity? =
        identityRepository.findByNameAndPrincipal(username, true)

    fun deleteByUsername(username: String): Int =
        identityRepository.deleteByNameAndPrincipal(username, true)
}