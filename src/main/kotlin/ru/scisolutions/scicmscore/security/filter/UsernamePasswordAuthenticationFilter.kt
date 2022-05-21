package ru.scisolutions.scicmscore.security.filter

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.GenericFilterBean
import org.springframework.web.util.UrlPathHelper
import ru.scisolutions.scicmscore.config.props.SecurityProps
import ru.scisolutions.scicmscore.domain.model.AuthRequest
import ru.scisolutions.scicmscore.domain.model.TokenResponse
import ru.scisolutions.scicmscore.domain.model.UserInfo
import ru.scisolutions.scicmscore.security.JwtTokenService
import ru.scisolutions.scicmscore.security.User
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class UsernamePasswordAuthenticationFilter(
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenService: JwtTokenService,
    private val securityProps: SecurityProps
) : GenericFilterBean() {
    override fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
        req as HttpServletRequest
        res as HttpServletResponse
        try {
            if (postToAuthenticate(req)) {
                val authRequest = getAuthRequest(req)
                logger.debug("Trying to authenticate user by username [${authRequest.identifier}]")
                processUsernamePasswordAuthentication(req, res, authRequest.identifier, authRequest.password)
                return // don't proceed with filter chain
            }
            logger.debug("Passing request down the filter chain")
            chain.doFilter(req, res)
        } catch (e: InternalAuthenticationServiceException) {
            SecurityContextHolder.clearContext()
            logger.error("Internal authentication service exception. ${e.message}")
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.message)
        } catch (e: AuthenticationException) {
            SecurityContextHolder.clearContext()
            logger.error("Authentication exception. ${e.message}")
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.message)
        }
    }

    private fun getAuthRequest(req: HttpServletRequest): AuthRequest =
        try {
            objectMapper.readValue(req.inputStream, AuthRequest::class.java)
        } catch (e: Exception) {
            throw BadCredentialsException(e.message)
        }

    private fun postToAuthenticate(req: HttpServletRequest): Boolean {
        val resourcePath = pathHelper.getPathWithinApplication(req)
        return resourcePath == "/api/auth/local" && req.method == "POST" // Strapi compatible
    }

    private fun processUsernamePasswordAuthentication(req: HttpServletRequest, res: HttpServletResponse, username: String, password: String) {
        val resultAuthentication = tryToAuthenticateWithUsernameAndPassword(username, password)
        SecurityContextHolder.getContext().authentication = resultAuthentication

        // Create JWT token
        val jwtToken = jwtTokenService.generateJwtToken(
            resultAuthentication.name,
            AuthorityUtils.authorityListToSet(resultAuthentication.authorities)
        )
        sendJWTTokenResponse(req, res, jwtToken, resultAuthentication)
    }

    private fun tryToAuthenticateWithUsernameAndPassword(username: String, password: String): Authentication {
        val authentication = UsernamePasswordAuthenticationToken(username, password)
        return tryToAuthenticate(authentication)
    }

    private fun tryToAuthenticate(authentication: Authentication): Authentication {
        val resultAuthentication = authenticationManager.authenticate(authentication)
        if (resultAuthentication == null || !resultAuthentication.isAuthenticated)
            throw InternalAuthenticationServiceException("Unable to authenticate user for provided credentials")

        logger.debug("User successfully authenticated")
        return resultAuthentication
    }

    private fun sendJWTTokenResponse(req: HttpServletRequest, res: HttpServletResponse, jwtToken: String, authentication: Authentication) {
        val user = (authentication.principal as User).user ?: throw IllegalArgumentException("Authentication has not user entity")
        val tokenResponse = TokenResponse(
            jwt = jwtToken,
            expirationIntervalMillis = securityProps.jwtToken.expirationIntervalMillis,
            user = UserInfo(
                id = user.id,
                username = user.username,
                roles = AuthorityUtils.authorityListToSet(authentication.authorities)
            )
        )
        val jsonResponse = objectMapper.writeValueAsString(tokenResponse)
        res.status = HttpServletResponse.SC_OK
        res.addHeader("Content-Type", "application/json")
//        res.addHeader("Access-Control-Allow-Credentials", "true");
//        res.addHeader("Access-Control-Allow-Origin", req.getHeader("Origin") ?: "*")
        res.writer.print(jsonResponse)
    }

    companion object {
        private val pathHelper = UrlPathHelper()

        private val objectMapper = jacksonObjectMapper().apply {
            this.registerModule(JavaTimeModule())
            this.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            this.setSerializationInclusion(Include.NON_NULL)
        }
    }
}