package ru.scisolutions.scicmscore.persistence.service

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.RevisionPolicy
import ru.scisolutions.scicmscore.persistence.repository.RevisionPolicyRepository

@Service
@Repository
@Transactional
class RevisionPolicyService(
    private val revisionPolicyRepository: RevisionPolicyRepository
) {
    @Transactional(readOnly = true)
    fun findById(id: String): RevisionPolicy? = revisionPolicyRepository.findById(id).orElse(null)
}