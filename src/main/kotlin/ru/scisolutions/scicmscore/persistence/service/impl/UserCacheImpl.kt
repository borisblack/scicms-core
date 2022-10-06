package ru.scisolutions.scicmscore.persistence.service.impl

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.persistence.entity.User
import ru.scisolutions.scicmscore.persistence.service.UserCache
import ru.scisolutions.scicmscore.persistence.service.UserService
import java.util.concurrent.TimeUnit

@Service
class UserCacheImpl(
    dataProps: DataProps,
    private val userService: UserService
) : UserCache {
    private val cache: Cache<String, User> = CacheBuilder.newBuilder()
        .expireAfterWrite(dataProps.cacheExpirationMinutes, TimeUnit.MINUTES)
        .build()

    override operator fun get(username: String): User? {
        var user = cache.getIfPresent(username)
        if (user == null)
            user = userService.findByUsername(username)

        if (user != null)
            cache.put(username, user)

        return user
    }

    override fun getOrThrow(username: String): User =
        get(username)  ?: throw IllegalArgumentException("User [$username] not found")

    override fun getCurrent(): User {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw AccessDeniedException("User is not authenticated")

        return getOrThrow(authentication.name)
    }
}