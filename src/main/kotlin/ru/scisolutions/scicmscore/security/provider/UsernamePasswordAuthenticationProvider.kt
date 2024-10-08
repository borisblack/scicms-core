package ru.scisolutions.scicmscore.security.provider

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.persistence.service.UserService
import ru.scisolutions.scicmscore.security.CustomUserDetailsManager
import ru.scisolutions.scicmscore.security.model.User
import ru.scisolutions.scicmscore.security.service.impl.UserGroupManagerImpl

@Component
class UsernamePasswordAuthenticationProvider(
    customUserDetailsManager: CustomUserDetailsManager,
    private val userService: UserService
) : DaoAuthenticationProvider() {
    init {
        userDetailsService = customUserDetailsManager
        passwordEncoder = UserGroupManagerImpl.passwordEncoder
    }

    override fun authenticate(authentication: Authentication?): Authentication {
        val authenticated = super.authenticate(authentication)
        val principal = authenticated.principal as org.springframework.security.core.userdetails.User
        val username = principal.username
        val userEntity = userService.getByUsername(username)
        val user = User(username, principal.password, principal.authorities, userEntity)
        return UsernamePasswordAuthenticationToken(user, authenticated.credentials, user.authorities)
    }
}
