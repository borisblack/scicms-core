package ru.scisolutions.scicmscore.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.security.core.Authentication
import ru.scisolutions.scicmscore.config.props.JwtTokenProps
import java.util.Date

class JwtTokenService(private val jwtTokenProps: JwtTokenProps) {
    fun generateJwtToken(authentication: Authentication): String =
        Jwts.builder()
            .setId(jwtTokenProps.id)
            .setSubject(authentication.name)
            .claim(
                "authorities",
                authentication.authorities.map { it.authority }
            )
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + jwtTokenProps.expirationIntervalMillis))
            .signWith(SignatureAlgorithm.HS512, jwtTokenProps.secret.toByteArray())
            .compact()

    fun parseToken(jwtToken: String): Claims =
        Jwts.parser()
            .setSigningKey(jwtTokenProps.secret.toByteArray())
            .parseClaimsJws(jwtToken)
            .body
}