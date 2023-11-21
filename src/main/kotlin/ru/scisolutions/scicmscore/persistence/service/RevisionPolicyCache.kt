package ru.scisolutions.scicmscore.persistence.service

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.persistence.entity.RevisionPolicy
import java.util.concurrent.TimeUnit

@Service
class RevisionPolicyCache(
    dataProps: DataProps,
    private val revisionPolicyService: RevisionPolicyService
) {
    private val cache: Cache<String, RevisionPolicy> = CacheBuilder.newBuilder()
        .expireAfterWrite(dataProps.cacheExpirationMinutes, TimeUnit.MINUTES)
        .build()

    fun get(id: String): RevisionPolicy? {
        var revisionPolicy = cache.getIfPresent(id)
        if (revisionPolicy == null)
            revisionPolicy = revisionPolicyService.findById(id)

        if (revisionPolicy != null)
            cache.put(id, revisionPolicy)

        return revisionPolicy
    }

    fun getOrThrow(id: String): RevisionPolicy =
        get(id) ?: throw IllegalArgumentException("Revision Policy [$id] not found")

    fun getDefault(): RevisionPolicy = getOrThrow(RevisionPolicy.DEFAULT_REVISION_POLICY_ID)
}