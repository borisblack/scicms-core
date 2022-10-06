package ru.scisolutions.scicmscore.persistence.service.impl

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.persistence.entity.RevisionPolicy
import ru.scisolutions.scicmscore.persistence.service.RevisionPolicyCache
import ru.scisolutions.scicmscore.persistence.service.RevisionPolicyService
import java.util.concurrent.TimeUnit

@Service
class RevisionPolicyCacheImpl(
    dataProps: DataProps,
    private val revisionPolicyService: RevisionPolicyService
) : RevisionPolicyCache {
    private val cache: Cache<String, RevisionPolicy> = CacheBuilder.newBuilder()
        .expireAfterWrite(dataProps.cacheExpirationMinutes, TimeUnit.MINUTES)
        .build()

    override fun get(id: String): RevisionPolicy? {
        var revisionPolicy = cache.getIfPresent(id)
        if (revisionPolicy == null)
            revisionPolicy = revisionPolicyService.findById(id)

        if (revisionPolicy != null)
            cache.put(id, revisionPolicy)

        return revisionPolicy
    }

    override fun getOrThrow(id: String): RevisionPolicy =
        get(id) ?: throw IllegalArgumentException("Revision Policy [$id] not found")

    override fun getDefault(): RevisionPolicy = getOrThrow(RevisionPolicy.DEFAULT_REVISION_POLICY_ID)
}