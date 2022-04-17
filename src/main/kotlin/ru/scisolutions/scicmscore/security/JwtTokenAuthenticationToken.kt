package ru.scisolutions.scicmscore.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken

class JwtTokenAuthenticationToken : PreAuthenticatedAuthenticationToken {
    constructor(aPrincipal: Any, aCredentials: Any?) : super(aPrincipal, aCredentials)

    constructor(aPrincipal: Any, aCredentials: Any?, anAuthorities: Collection<GrantedAuthority>) : super(aPrincipal, aCredentials, anAuthorities)
}