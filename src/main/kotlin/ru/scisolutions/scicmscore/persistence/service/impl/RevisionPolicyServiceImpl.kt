package ru.scisolutions.scicmscore.persistence.service.impl

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.RevisionPolicy
import ru.scisolutions.scicmscore.persistence.repository.RevisionPolicyRepository
import ru.scisolutions.scicmscore.persistence.service.RevisionPolicyService

@Service
@Repository
@Transactional
class RevisionPolicyServiceImpl(
    private val revisionPolicyRepository: RevisionPolicyRepository
) : RevisionPolicyService {
    @Transactional(readOnly = true)
    override fun findById(id: String): RevisionPolicy = revisionPolicyRepository.findById(id).orElse(null)
}