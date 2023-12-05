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

    @Transactional(readOnly = true)
    fun getById(id: String): RevisionPolicy = revisionPolicyRepository.findById(id).orElseThrow {
        IllegalArgumentException("Revision policy [$id] not found.")
    }

    @Transactional(readOnly = true)
    fun getDefault(): RevisionPolicy = findById(RevisionPolicy.DEFAULT_REVISION_POLICY_ID)
        ?: throw IllegalArgumentException("Default revision policy not found.")
}