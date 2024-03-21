package ru.scisolutions.scicmscore.security

import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken

class JwtTokenAuthenticationToken(jwtToken: String) : PreAuthenticatedAuthenticationToken(jwtToken, null)