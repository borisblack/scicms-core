package ru.scisolutions.scicmscore.engine.data.handler.impl

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.data.handler.UserHandler
import ru.scisolutions.scicmscore.engine.data.model.UserInfo

@Service
class UserHandlerImpl : UserHandler {
    override fun me(): UserInfo? {
        val authentication = SecurityContextHolder.getContext().authentication
        return if (authentication == null)
            null
        else UserInfo(
            username = authentication.name,
            roles = authentication.authorities.map { it.authority }.toSet()
        )
    }
}