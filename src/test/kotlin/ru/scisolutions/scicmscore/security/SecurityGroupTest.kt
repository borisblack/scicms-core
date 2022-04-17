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
    private val authorities = listOf<GrantedAuthority>(SimpleGrantedAuthority(ROLE_GROUP_TEST))

    @BeforeEach
    fun setup() {
        customUserDetailsManager.createGroup(GROUP_TEST, authorities)
        userGroupManager.createUserWithAuthority(USER_TEST, ROLE_USER_TEST)
        user = customUserDetailsManager.loadUserByUsername(USER_TEST)
    }

    @AfterEach
    fun tearDown() {
        customUserDetailsManager.deleteUser(USER_TEST)
        customUserDetailsManager.deleteGroup(GROUP_TEST)
        SecurityContextHolder.getContext().authentication = null
    }

    @Test
    fun testAddAuthorityToGroup() {
        val authority: GrantedAuthority = SimpleGrantedAuthority(ROLE_TEST)
        customUserDetailsManager.addGroupAuthority(GROUP_TEST, authority)
        val foundAuthorities = customUserDetailsManager.findGroupAuthorities(GROUP_TEST)
        Assertions.assertEquals(2, foundAuthorities.size)
        for (grantedAuthority in foundAuthorities) {
            if (ROLE_TEST == grantedAuthority.authority) {
                Assertions.assertEquals(grantedAuthority, authority)
            }
        }
    }

    @Test
    fun testAddMemberToGroup() {
        customUserDetailsManager.addUserToGroup(user!!.username, GROUP_TEST)
        val foundUsers = customUserDetailsManager.findUsersInGroup(GROUP_TEST)
        Assertions.assertTrue(foundUsers.contains((user as UserDetails).username))
    }

    @Test
    fun testUserInGroupHasAccessToRoleUserMethod() {
        customUserDetailsManager.addUserToGroup((user as UserDetails).username, GROUP_TEST)
        userGroupManager.setAuthentication((user as UserDetails).username)
        Assertions.assertTrue(securityTestService.testHasRoleUser())
    }

    @Test
    fun testUserInGroupHasNoAccessToRoleAdminMethod() {
        customUserDetailsManager.addUserToGroup((user as UserDetails).username, GROUP_TEST)
        userGroupManager.setAuthentication((user as UserDetails).username)
        assertThrows<AccessDeniedException> { securityTestService.testHasRoleAdmin() }
    }

    @Test
    fun testUserInGroupHasAccessToRoleUserAndRoleGroup() {
        customUserDetailsManager.addUserToGroup((user as UserDetails).username, GROUP_TEST)
        userGroupManager.setAuthentication((user as UserDetails).username)
        Assertions.assertTrue(securityTestService.testHasRoleUser())
        Assertions.assertTrue(securityTestService.testHasRoleGroup())
    }

    companion object {
        private const val GROUP_TEST = "Test"
        private const val USER_TEST = "test"
        private const val ROLE_GROUP_TEST = "ROLE_GROUP_TEST"
        private const val ROLE_USER_TEST = "ROLE_USER_TEST"
        private const val ROLE_TEST = "ROLE_TEST"
    }
}