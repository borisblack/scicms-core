package ru.scisolutions.scicmscore.api.graphql.datafetcher

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import org.springframework.security.core.context.SecurityContextHolder

@DgsComponent
class MeDataFetcher {
    @DgsQuery
    fun me(): UserInfo? {
        val authentication = SecurityContextHolder.getContext().authentication
        return if (authentication == null)
            null
        else UserInfo(
            username = authentication.name,
            roles = authentication.authorities.map { it.authority }.toSet()
        )
    }

    data class UserInfo(val username: String, val roles: Set<String>)
}