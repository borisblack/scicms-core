package ru.scisolutions.scicmscore.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import ru.scisolutions.scicmscore.model.AuthType

class UserAuthenticationToken(
    username: String,
    authorities: Collection<GrantedAuthority>,
    val authType: AuthType
) : PreAuthenticatedAuthenticationToken(username, null, authorities)