package ru.scisolutions.scicmscore.api.graphql.datafetcher.query

import com.netflix.graphql.dgs.DgsQueryExecutor
import com.netflix.graphql.dgs.autoconfig.DgsAutoConfiguration
import com.netflix.graphql.dgs.autoconfig.DgsExtendedScalarsAutoConfiguration
import com.netflix.graphql.dgs.scalars.UploadScalar
import com.netflix.graphql.dgs.test.EnableDgsTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.aot.DisabledInAotMode
import ru.scisolutions.scicmscore.api.graphql.CustomScalarsRegistration
import ru.scisolutions.scicmscore.engine.Engine
import ru.scisolutions.scicmscore.engine.model.AuthType
import ru.scisolutions.scicmscore.engine.model.UserInfo
import java.util.UUID

@SpringBootTest(
    classes = [
        DgsAutoConfiguration::class,
        DgsExtendedScalarsAutoConfiguration::class,
        CustomScalarsRegistration::class,
        UploadScalar::class,
        UserDataFetcher::class
    ]
)
@EnableDgsTest
@DisabledInAotMode
class UserDataFetcherTest {
    @Autowired
    private lateinit var dgsQueryExecutor: DgsQueryExecutor

    @MockBean
    lateinit var engine: Engine

    @BeforeEach
    fun before() {
        Mockito.`when`(engine.me()).thenAnswer {
            UserInfo(
                id = UUID.randomUUID().toString(),
                username = TEST_USER,
                roles = setOf(ROLE_TEST),
                sessionData = emptyMap(),
                authType = AuthType.LOCAL
            )
        }
    }

    @Test
    fun testUsername() {
        val username: String =
            dgsQueryExecutor.executeAndExtractJsonPath(
                """
                    {
                        me {
                            username
                            roles
                        }
                }
                """.trimIndent(),
                "data.me.username"
            )

        Assertions.assertEquals(TEST_USER, username)
    }

    @Test
    fun testRoles() {
        val roles: List<String> =
            dgsQueryExecutor.executeAndExtractJsonPath(
                "{ me { username roles } }",
                "data.me.roles"
            )

        Assertions.assertTrue(ROLE_TEST in roles)
    }

    companion object {
        const val TEST_USER = "testUser"
        const val ROLE_TEST = "ROLE_TEST"
    }
}
