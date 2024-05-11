package ru.scisolutions.scicmscore.security.provider;

import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.config.props.SecurityProps
import ru.scisolutions.scicmscore.engine.persistence.service.UserService
import ru.scisolutions.scicmscore.security.Oauth2AccessCodeAuthenticationToken
import ru.scisolutions.scicmscore.security.model.Oauth2AccessTokenRequest
import ru.scisolutions.scicmscore.security.model.Oauth2AccessTokenResponse
import ru.scisolutions.scicmscore.security.model.User
import ru.scisolutions.scicmscore.security.service.UserGroupManager
import ru.scisolutions.scicmscore.engine.util.Acl
import ru.scisolutions.scicmscore.util.Json
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers


@Component
class Oauth2AccessCodeAuthenticationProvider(
    private val securityProps: SecurityProps,
    private val userGroupManager: UserGroupManager,
    private val userService: UserService
) : AuthenticationProvider {
    override fun supports(authentication: Class<*>): Boolean = authentication == Oauth2AccessCodeAuthenticationToken::class.java

    override fun authenticate(authentication: Authentication): Authentication {
        val providerId = (authentication.principal as String?) ?: throw BadCredentialsException("Wrong OAuth2 provider ID.")
        val accessCode = (authentication.credentials as String?) ?: throw BadCredentialsException("Wrong OAuth2 access code.")
        val provider = securityProps.oauth2Providers.find { it.id == providerId } ?: throw BadCredentialsException("OAuth2 provider not found.")

        val accessTokenResponse = fetchAccessToken(provider, accessCode)
        val userInfo = fetchUserInfo(provider, accessTokenResponse)
        val username = provider.userInfoParser.parseUsername(userInfo)
        val roles = provider.userInfoParser.parseAuthorities(userInfo)
        if (!userService.existsByUsername(username)) {
            userGroupManager.createUserInGroups(
                username = username,
                rawPassword = Acl.randomPassword(),
                groupNames = setOf(Acl.GROUP_USERS)
            )
        }

        val userDetails = userGroupManager.loadUserByUsername(username)
        val authorities = AuthorityUtils.authorityListToSet(userDetails.authorities) + roles
        val userEntity = userService.findByUsername(username)
        val user = User(username, userDetails.password, AuthorityUtils.createAuthorityList(authorities), userEntity)

        return UsernamePasswordAuthenticationToken(user, user.password, user.authorities)
    }

    private fun fetchAccessToken(provider: SecurityProps.Oauth2Provider, accessCode: String): Oauth2AccessTokenResponse {
        val accessTokenRequest: HttpRequest = HttpRequest.newBuilder()
            .uri(URI(provider.accessTokenUrl))
            .headers(
                "Content-Type", "application/json;charset=UTF-8",
                "Accept", "application/json"
            )
            .POST(HttpRequest.BodyPublishers.ofString(
                Json.objectMapper.writeValueAsString(
                    Oauth2AccessTokenRequest(
                        clientId = provider.clientId,
                        clientSecret = provider.clientSecret,
                        accessCode = accessCode
                    )
                )
            ))
            .build()

        logger.trace("Fetching access token")
        val accessTokenRawResponse: HttpResponse<String> = HttpClient.newBuilder()
            .build()
            .send(accessTokenRequest, BodyHandlers.ofString())
        val accessTokenResponse = Json.objectMapper.readValue(accessTokenRawResponse.body(), Oauth2AccessTokenResponse::class.java)
        logger.trace("Access token fetched.")

        return accessTokenResponse
    }

    private fun fetchUserInfo(provider: SecurityProps.Oauth2Provider, accessTokenResponse: Oauth2AccessTokenResponse): Any {
        val userInfoRequest: HttpRequest = HttpRequest.newBuilder()
            .uri(URI(provider.apiUrl))
            .headers(
                "Authorization", "Bearer ${accessTokenResponse.accessToken}",
                "Accept", "application/json"
            )
            .POST(HttpRequest.BodyPublishers.noBody())
            .build()

        logger.trace("Fetching user info")
        val userInfoRawResponse: HttpResponse<String> = HttpClient.newBuilder()
            .build()
            .send(userInfoRequest, BodyHandlers.ofString())
        val userInfoResponse = Json.objectMapper.readValue(userInfoRawResponse.body(), Any::class.java)
        logger.trace("User info fetched.")

        return userInfoResponse
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Oauth2AccessCodeAuthenticationProvider::class.java)
    }
}
