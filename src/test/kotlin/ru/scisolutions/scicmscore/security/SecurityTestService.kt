package ru.scisolutions.scicmscore.security

import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class SecurityTestService {
    @PreAuthorize("hasRole('ROLE_TEST_ADMIN')")
    fun testHasRoleAdmin(): Boolean {
        logger.info("Access granted to hasRole('ROLE_TEST_ADMIN')")
        return true
    }

    @PreAuthorize("hasRole('ROLE_TEST_USER')")
    fun testHasRoleUser(): Boolean {
        logger.info("Access granted to hasRole('ROLE_TEST_USER')")
        return true
    }

    @PreAuthorize("hasRole('ROLE_TEST_GROUP')")
    fun testHasRoleGroup(): Boolean {
        logger.info("Access granted to hasRole('ROLE_TEST_GROUP')")
        return true
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SecurityTestService::class.java)
    }
}
