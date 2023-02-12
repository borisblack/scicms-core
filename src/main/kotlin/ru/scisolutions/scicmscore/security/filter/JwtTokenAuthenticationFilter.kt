package ru.scisolutions.scicmscore.security.filter

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.GenericFilterBean
import ru.scisolutions.scicmscore.security.JwtTokenAuthenticationToken
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtTokenAuthenticationFilter(private val authenticationManager: AuthenticationManager) : GenericFilterBean() {
    override fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
        req as HttpServletRequest
        res as HttpServletResponse
        val jwtToken = getJwtToken(req)
        try {
            if (jwtToken != null) {
                logger.trace("Trying to authenticate user by JWT token [$jwtToken]")
                processJwtTokenAuthentication(jwtToken)
            }
            logger.trace("Passing request down the filter chain")
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

    private fun getJwtToken(req: HttpServletRequest) = req.getHeader(HEADER)?.replace(PREFIX, "")

    private fun processJwtTokenAuthentication(jwtToken: String) {
        val resultAuthentication = tryToAuthenticateWithJwtToken(jwtToken)
        SecurityContextHolder.getContext().authentication = resultAuthentication
    }

    private fun tryToAuthenticateWithJwtToken(jwtToken: String): Authentication {
        val authentication = JwtTokenAuthenticationToken(jwtToken, null)
        return tryToAuthenticate(authentication)
    }

    private fun tryToAuthenticate(authentication: Authentication): Authentication {
        val resultAuthentication = authenticationManager.authenticate(authentication)
        if (resultAuthentication == null || !resultAuthentication.isAuthenticated)
            throw InternalAuthenticationServiceException("Unable to authenticate user for provided credentials")

        logger.trace("User successfully authenticated")
        return resultAuthentication
    }

    companion object {
        private const val HEADER = "Authorization"
        private const val PREFIX = "Bearer "
    }
}