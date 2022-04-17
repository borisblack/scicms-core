package ru.scisolutions.scicmscore.security.provider

import io.jsonwebtoken.JwtException
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import ru.scisolutions.scicmscore.security.JwtTokenAuthenticationToken
import ru.scisolutions.scicmscore.security.JwtTokenService

class JwtTokenAuthenticationProvider(private val jwtTokenService: JwtTokenService) : AuthenticationProvider {
    override fun supports(authentication: Class<*>): Boolean = authentication == JwtTokenAuthenticationToken::class.java

    override fun authenticate(authentication: Authentication): Authentication {
        val jwtToken = (authentication.principal as String?) ?: throw BadCredentialsException("Wrong JWT token")

        val claims = try {
            jwtTokenService.parseToken(jwtToken)
        } catch (e: JwtException) {
            throw BadCredentialsException(e.message)
        }

        val authorities: List<String> = claims["authorities"] as List<String>
        return UsernamePasswordAuthenticationToken(
            claims.subject,
            null,
            authorities.map { SimpleGrantedAuthority(it) }
        )
    }
}