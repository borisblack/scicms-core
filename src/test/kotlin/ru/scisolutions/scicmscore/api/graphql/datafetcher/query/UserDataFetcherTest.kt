package ru.scisolutions.scicmscore.api.graphql.datafetcher.query

import com.netflix.graphql.dgs.DgsQueryExecutor
import com.netflix.graphql.dgs.autoconfig.DgsAutoConfiguration
import com.netflix.graphql.dgs.autoconfig.DgsExtendedScalarsAutoConfiguration
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import ru.scisolutions.scicmscore.engine.data.DataEngine
import ru.scisolutions.scicmscore.engine.data.model.UserInfo

@SpringBootTest(classes = [DgsAutoConfiguration::class, DgsExtendedScalarsAutoConfiguration::class, UserDataFetcher::class])
class UserDataFetcherTest {
    @Autowired
    private lateinit var dgsQueryExecutor: DgsQueryExecutor

    @MockBean
    lateinit var dataEngine: DataEngine

    @BeforeEach
    fun before() {
        Mockito.`when`(dataEngine.me()).thenAnswer {
            UserInfo(
                username = TEST_USER,
                roles = setOf(ROLE_TEST)
            )
        }
    }

    @Test
    fun testUsername() {
        val username : String = dgsQueryExecutor.executeAndExtractJsonPath("""
            {
                me {
		            username
		            roles
	            }
            }
        """.trimIndent(), "data.me.username")

        Assertions.assertEquals(TEST_USER, username)
    }

    @Test
    fun testRoles() {
        val roles : List<String> = dgsQueryExecutor.executeAndExtractJsonPath("""
            {
                me {
		            username
		            roles
	            }
            }
        """.trimIndent(), "data.me.roles")

        Assertions.assertTrue(ROLE_TEST in roles)
    }

    companion object {
        const val TEST_USER = "testUser"
        const val ROLE_TEST = "ROLE_TEST"
    }
}