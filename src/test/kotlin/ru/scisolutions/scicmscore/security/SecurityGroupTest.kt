package ru.scisolutions.scicmscore.security

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import ru.scisolutions.scicmscore.security.service.UserGroupManager

@SpringBootTest
class SecurityGroupTest {
    @Autowired
    private lateinit var userGroupManager: UserGroupManager

    @Autowired
    private lateinit var customUserDetailsManager: CustomUserDetailsManager

    @Autowired
    private lateinit var securityTestService: SecurityTestService

    private var user: UserDetails? = null
    private val authorities = listOf<GrantedAuthority>(SimpleGrantedAuthority(ROLE_TEST_GROUP))

    @BeforeEach
    fun setup() {
        customUserDetailsManager.createGroup(TEST_GROUP_NAME, authorities)
        userGroupManager.createUser(TEST_USERNAME, TEST_USERNAME, setOf(ROLE_TEST_USER))
        user = customUserDetailsManager.loadUserByUsername(TEST_USERNAME)
    }

    @AfterEach
    fun tearDown() {
        customUserDetailsManager.deleteUser(TEST_USERNAME)
        customUserDetailsManager.deleteGroup(TEST_GROUP_NAME)
        SecurityContextHolder.getContext().authentication = null
    }

    @Test
    fun testAddAuthorityToGroup() {
        val authority: GrantedAuthority = SimpleGrantedAuthority(ROLE_TEST)
        customUserDetailsManager.addGroupAuthority(TEST_GROUP_NAME, authority)
        val foundAuthorities = customUserDetailsManager.findGroupAuthorities(TEST_GROUP_NAME)
        Assertions.assertEquals(2, foundAuthorities.size)
        for (grantedAuthority in foundAuthorities) {
            if (ROLE_TEST == grantedAuthority.authority) {
                Assertions.assertEquals(grantedAuthority, authority)
            }
        }
    }

    @Test
    fun testAddMemberToGroup() {
        customUserDetailsManager.addUserToGroup(user!!.username, TEST_GROUP_NAME)
        val foundUsers = customUserDetailsManager.findUsersInGroup(TEST_GROUP_NAME)
        Assertions.assertTrue(foundUsers.contains((user as UserDetails).username))
    }

    @Test
    fun testUserInGroupHasAccessToRoleUserMethod() {
        customUserDetailsManager.addUserToGroup((user as UserDetails).username, TEST_GROUP_NAME)
        userGroupManager.setAuthentication((user as UserDetails).username)
        Assertions.assertTrue(securityTestService.testHasRoleUser())
    }

    @Test
    fun testUserInGroupHasNoAccessToRoleAdminMethod() {
        customUserDetailsManager.addUserToGroup((user as UserDetails).username, TEST_GROUP_NAME)
        userGroupManager.setAuthentication((user as UserDetails).username)
        assertThrows<AccessDeniedException> { securityTestService.testHasRoleAdmin() }
    }

    @Test
    fun testUserInGroupHasAccessToRoleUserAndRoleGroup() {
        customUserDetailsManager.addUserToGroup((user as UserDetails).username, TEST_GROUP_NAME)
        userGroupManager.setAuthentication((user as UserDetails).username)
        Assertions.assertTrue(securityTestService.testHasRoleUser())
        Assertions.assertTrue(securityTestService.testHasRoleGroup())
    }

    companion object {
        private const val TEST_GROUP_NAME = "Test"
        private const val TEST_USERNAME = "test"
        private const val ROLE_TEST_GROUP = "ROLE_TEST_GROUP"
        private const val ROLE_TEST_USER = "ROLE_TEST_USER"
        private const val ROLE_TEST = "ROLE_TEST"
    }
}