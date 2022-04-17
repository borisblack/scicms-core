package ru.scisolutions.scicmscore.security

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import ru.scisolutions.scicmscore.security.service.UserGroupManager

@SpringBootTest
class SecurityUserTest {
    @Autowired
    private lateinit var userGroupManager: UserGroupManager

    @Autowired
    private lateinit var customUserDetailsManager: CustomUserDetailsManager

    private var user: UserDetails? = null

    @BeforeEach
    fun setup() {
        userGroupManager.createUserWithAuthority(USER_TEST, ROLE_USER_TEST)
        user = customUserDetailsManager.loadUserByUsername(USER_TEST)
        userGroupManager.setAuthentication(USER_TEST)
    }

    @AfterEach
    fun tearDown() {
        customUserDetailsManager.deleteUser(USER_TEST)
    }

    @Test
    fun checkUser() {
        Assertions.assertNotNull(user)
        Assertions.assertTrue(user is UserDetails)
        val roleUserTest = SimpleGrantedAuthority(ROLE_USER_TEST)
        Assertions.assertTrue((user as UserDetails).authorities.contains(roleUserTest))
    }

    companion object {
        private const val USER_TEST = "test"
        private const val ROLE_USER_TEST = "ROLE_USER_TEST"
    }
}