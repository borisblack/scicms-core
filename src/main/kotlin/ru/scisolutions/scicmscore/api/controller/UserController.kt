package ru.scisolutions.scicmscore.api.controller

import org.springframework.mail.javamail.JavaMailSender
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.scisolutions.scicmscore.config.props.SecurityProps
import ru.scisolutions.scicmscore.model.RegistrationRequest
import ru.scisolutions.scicmscore.model.TokenResponse
import ru.scisolutions.scicmscore.model.UserInfo
import ru.scisolutions.scicmscore.persistence.service.UserService
import ru.scisolutions.scicmscore.security.JwtTokenService
import ru.scisolutions.scicmscore.security.service.UserGroupManager
import ru.scisolutions.scicmscore.util.Acl

@RestController
@RequestMapping("/api/auth/local")
class UserController(
    private val securityProps: SecurityProps,
    private val userGroupManager: UserGroupManager,
    private val userService: UserService,
    private val jwtTokenService: JwtTokenService,
    private val javaMailSender: JavaMailSender
) {
    @PostMapping("/register")
    fun register(@RequestBody registrationRequest: RegistrationRequest): TokenResponse {
        if (securityProps.registrationDisabled)
            throw AccessDeniedException("Users registration is disabled.")

        userGroupManager.createUserInGroups(
            username = registrationRequest.username,
            rawPassword = registrationRequest.password,
            groupNames = setOf(Acl.GROUP_USERS)
        )

        val userDetails = userGroupManager.loadUserByUsername(registrationRequest.username)
        val authorities = AuthorityUtils.authorityListToSet(userDetails.authorities)
        val user = userService.getByUsername(registrationRequest.username)

        return TokenResponse(
            jwt = jwtTokenService.generateJwtToken(userDetails.username, authorities),
            expirationIntervalMillis = securityProps.jwtToken.expirationIntervalMillis,
            user = UserInfo(
                id = user.id,
                username = userDetails.username,
                roles = authorities,
                sessionData = user.sessionData
            )
        )
    }
}