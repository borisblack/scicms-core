package ru.scisolutions.scicmscore.engine.handler

import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.model.response.SessionDataResponse
import ru.scisolutions.scicmscore.model.UserInfo
import ru.scisolutions.scicmscore.persistence.service.UserCache

@Service
class UserHandler(private val userCache: UserCache) {
    fun me(): UserInfo? {
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

    fun updateSessionData(sessionData: Map<String, Any?>?): SessionDataResponse {
        val user = userCache.getCurrent()
        user.sessionData = sessionData
        val savedUser = userCache.save(user)

        return SessionDataResponse(savedUser.sessionData)
    }
}