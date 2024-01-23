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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityUserTest {
    @Autowired
    private lateinit var userGroupManager: UserGroupManager

    @Autowired
    private lateinit var customUserDetailsManager: CustomUserDetailsManager

    private var user: UserDetails? = null

    @BeforeEach
    fun setup() {
        userGroupManager.createUser(TEST_USERNAME, TEST_USERNAME, setOf(ROLE_TEST_USER))
        user = customUserDetailsManager.loadUserByUsername(TEST_USERNAME)
        userGroupManager.setAuthentication(TEST_USERNAME)
    }

    @AfterEach
    fun tearDown() {
        customUserDetailsManager.deleteUser(TEST_USERNAME)
    }

    @Test
    fun checkUser() {
        Assertions.assertNotNull(user)
        Assertions.assertTrue(user is UserDetails)
        val roleUserTest = SimpleGrantedAuthority(ROLE_TEST_USER)
        Assertions.assertTrue((user as UserDetails).authorities.contains(roleUserTest))
    }

    companion object {
        private const val TEST_USERNAME = "test"
        private const val ROLE_TEST_USER = "ROLE_TEST_USER"
    }
}