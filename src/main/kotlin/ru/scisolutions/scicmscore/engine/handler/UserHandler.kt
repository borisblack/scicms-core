package ru.scisolutions.scicmscore.engine.handler

import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.SecurityProps
import ru.scisolutions.scicmscore.engine.model.AuthType
import ru.scisolutions.scicmscore.engine.model.ChangePasswordRequest
import ru.scisolutions.scicmscore.engine.model.RegistrationRequest
import ru.scisolutions.scicmscore.engine.model.UserInfo
import ru.scisolutions.scicmscore.engine.model.response.SessionDataResponse
import ru.scisolutions.scicmscore.engine.model.response.TokenResponse
import ru.scisolutions.scicmscore.engine.persistence.service.UserService
import ru.scisolutions.scicmscore.security.JwtTokenService
import ru.scisolutions.scicmscore.security.UserAuthenticationToken
import ru.scisolutions.scicmscore.security.service.UserGroupManager
import ru.scisolutions.scicmscore.security.service.impl.UserGroupManagerImpl
import ru.scisolutions.scicmscore.engine.util.Acl

@Service
class UserHandler(
    private val securityProps: SecurityProps,
    private val userGroupManager: UserGroupManager,
    private val jwtTokenService: JwtTokenService,
    private val userService: UserService
) {
    fun register(registrationRequest: RegistrationRequest): TokenResponse {
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
            jwt = jwtTokenService.generateJwtToken(userDetails.username, authorities, AuthType.LOCAL),
            expirationIntervalMillis = securityProps.jwtToken.expirationIntervalMillis,
            user = UserInfo(
                id = user.id,
                username = userDetails.username,
                roles = authorities,
                authType = AuthType.LOCAL,
                sessionData = user.sessionData
            )
        )
    }

    fun changePassword(changePasswordRequest: ChangePasswordRequest) {
        val user = userService.getCurrent()
        val passwordEncoder = UserGroupManagerImpl.passwordEncoder
        if (!passwordEncoder.matches(changePasswordRequest.oldPassword, user.password)) {
            throw AccessDeniedException("Password mismatch.")
        }

        validatePassword(changePasswordRequest.oldPassword, changePasswordRequest.newPassword)
        user.password = passwordEncoder.encode(changePasswordRequest.newPassword)
        userService.save(user)
    }

    private fun validatePassword(oldPassword: String, newPassword: String) {
        if (newPassword == oldPassword)
            throw IllegalArgumentException("The new and old passwords must not be the same.")

        if (!securityProps.passwordPattern.matches(newPassword))
            throw IllegalArgumentException("Password must match te pattern: ${securityProps.passwordPattern}.")
    }

    fun me(): UserInfo? {
        val authentication = SecurityContextHolder.getContext().authentication as UserAuthenticationToken?
        return if (authentication == null) {
            null
        } else {
            val username = authentication.name
            val user = userService.getByUsername(username)
            UserInfo(
                id = user.id,
                username = authentication.name,
                roles = AuthorityUtils.authorityListToSet(authentication.authorities),
                sessionData = user.sessionData,
                authType = authentication.authType
            )
        }
    }

    fun updateSessionData(sessionData: Map<String, Any?>?): SessionDataResponse {
        val user = userService.getCurrent()
        user.sessionData = sessionData
        val savedUser = userService.save(user)

        return SessionDataResponse(savedUser.sessionData)
    }
}