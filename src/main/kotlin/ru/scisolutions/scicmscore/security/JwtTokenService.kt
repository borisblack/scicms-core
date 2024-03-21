package ru.scisolutions.scicmscore.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import ru.scisolutions.scicmscore.config.props.SecurityProps
import ru.scisolutions.scicmscore.model.AuthType
import java.util.Date

class JwtTokenService(securityProps: SecurityProps) {
    private val jwtTokenProps = securityProps.jwtToken

    fun generateJwtToken(subject: String, authorities: Set<String>, authType: AuthType): String =
        Jwts.builder()
            .setId(jwtTokenProps.id)
            .setSubject(subject)
            .claim("authorities", authorities)
            .claim("authType", authType)
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