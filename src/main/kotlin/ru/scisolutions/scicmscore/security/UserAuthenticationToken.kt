package ru.scisolutions.scicmscore.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import ru.scisolutions.scicmscore.engine.model.AuthType

class UserAuthenticationToken(
    username: String,
    authorities: Collection<GrantedAuthority>,
    val authType: AuthType
) : PreAuthenticatedAuthenticationToken(username, null, authorities)
