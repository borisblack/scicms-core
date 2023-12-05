package ru.scisolutions.scicmscore.engine.handler

import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.model.response.SessionDataResponse
import ru.scisolutions.scicmscore.model.UserInfo
import ru.scisolutions.scicmscore.persistence.service.UserService

@Service
class UserHandler(private val userService: UserService) {
    fun me(): UserInfo? {
        val authentication = SecurityContextHolder.getContext().authentication
        return if (authentication == null) {
            null
        } else {
            val username = authentication.name
            val user = userService.getByUsername(username)
            UserInfo(
                id = user.id,
                username = authentication.name,
                roles = AuthorityUtils.authorityListToSet(authentication.authorities),
                sessionData = user.sessionData
            )
        }
    }

    fun updateSessionData(sessionData: Map<String, Any?>?): SessionDataResponse {
        val user = userService.getCurrent()
        user.sessionData = sessionData
        val savedUser = userService.save(user)

        return SessionDataResponse(savedUser.sessionData)
    }
}