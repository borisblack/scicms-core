package ru.scisolutions.scicmscore.security.provider

import io.jsonwebtoken.JwtException
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import ru.scisolutions.scicmscore.engine.model.AuthType
import ru.scisolutions.scicmscore.security.JwtTokenAuthenticationToken
import ru.scisolutions.scicmscore.security.JwtTokenService
import ru.scisolutions.scicmscore.security.UserAuthenticationToken

class JwtTokenAuthenticationProvider(private val jwtTokenService: JwtTokenService) : AuthenticationProvider {
    override fun supports(authentication: Class<*>): Boolean = authentication == JwtTokenAuthenticationToken::class.java

    override fun authenticate(authentication: Authentication): Authentication {
        val jwtToken = (authentication.principal as String?) ?: throw BadCredentialsException("Wrong JWT token")

        val claims =
            try {
                jwtTokenService.parseToken(jwtToken)
            } catch (e: JwtException) {
                throw BadCredentialsException(e.message)
            }

        val authorities: List<String> = claims["authorities"] as List<String>
        val authType = claims["authType"] as String?
        return UserAuthenticationToken(
            claims.subject,
            authorities.map { SimpleGrantedAuthority(it) },
            if (authType == null) AuthType.LOCAL else AuthType.valueOf(authType)
        )
    }
}
