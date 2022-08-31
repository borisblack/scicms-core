package ru.scisolutions.scicmscore.persistence.service.impl

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.persistence.entity.RevisionPolicy
import ru.scisolutions.scicmscore.persistence.repository.RevisionPolicyRepository
import ru.scisolutions.scicmscore.persistence.service.RevisionPolicyService
import java.util.concurrent.TimeUnit

@Service
@Repository
@Transactional
class RevisionPolicyServiceImpl(
    dataProps: DataProps,
    private val revisionPolicyRepository: RevisionPolicyRepository
) : RevisionPolicyService {
    private val revisionPolicyCache: Cache<String, RevisionPolicy> = CacheBuilder.newBuilder()
        .expireAfterWrite(dataProps.cacheExpirationMinutes, TimeUnit.MINUTES)
        .build()

    @Transactional(readOnly = true)
    override fun getDefaultRevisionPolicy(): RevisionPolicy = getById(RevisionPolicy.DEFAULT_REVISION_POLICY_ID.toString())

    @Transactional(readOnly = true)
    override fun getById(id: String): RevisionPolicy = revisionPolicyCache.get(id) { revisionPolicyRepository.getById(id) }
}