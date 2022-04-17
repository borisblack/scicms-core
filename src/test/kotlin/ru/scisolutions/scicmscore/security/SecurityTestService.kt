package ru.scisolutions.scicmscore.security

import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service

@Service
class SecurityTestService {
    @PreAuthorize("hasRole('ROLE_ADMIN_TEST')")
    fun testHasRoleAdmin(): Boolean {
        logger.info("Access granted to hasRole('ROLE_ADMIN_TEST')")
        return true
    }

    @PreAuthorize("hasRole('ROLE_USER_TEST')")
    fun testHasRoleUser(): Boolean {
        logger.info("Access granted to hasRole('ROLE_USER_TEST')")
        return true
    }

    @PreAuthorize("hasRole('ROLE_GROUP_TEST')")
    fun testHasRoleGroup(): Boolean {
        logger.info("Access granted to hasRole('ROLE_GROUP_TEST')")
        return true
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SecurityTestService::class.java)
    }
}