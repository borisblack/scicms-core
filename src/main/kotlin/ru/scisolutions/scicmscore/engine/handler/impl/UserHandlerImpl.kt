package ru.scisolutions.scicmscore.engine.handler.impl

import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.handler.UserHandler
import ru.scisolutions.scicmscore.model.UserInfo
import ru.scisolutions.scicmscore.persistence.service.UserCache

@Service
class UserHandlerImpl(private val userCache: UserCache) : UserHandler {
    override fun me(): UserInfo? {
        val authentication = SecurityContextHolder.getContext().authentication
        return if (authentication == null) {
            null
        } else {
            val username = authentication.name
            val user = userCache.getOrThrow(username)
            UserInfo(
                id = user.id,
                username = authentication.name,
                roles = AuthorityUtils.authorityListToSet(authentication.authorities),
                sessionData = user.sessionData
            )
        }
    }

    override fun updateSessionData(sessionData: Map<String, Any?>?): Map<String, Any?>? {
        val user = userCache.getCurrent()
        user.sessionData = sessionData
        val savedUser = userCache.save(user)

        return savedUser.sessionData
    }
}