package ru.scisolutions.scicmscore.config.props

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.security.service.UserInfoParser
import ru.scisolutions.scicmscore.security.service.impl.DefaultUserInfoParser
import ru.scisolutions.scicmscore.service.ClassService

@Component
@ConfigurationProperties(prefix = "scicms-core.security")
class SecurityProps {
    @Autowired
    private lateinit var classService: ClassService

    @NestedConfigurationProperty
    var jwtToken: JwtToken = JwtToken()

    var registrationDisabled: Boolean = false
    var passwordPattern: Regex = DEFAULT_PASSWORD_PATTERN.toRegex()
    var clearAccessOnUserDelete: Boolean = false

    @NestedConfigurationProperty
    var oauth2Providers: List<Oauth2Provider> = emptyList()

    @PostConstruct
    fun setup() {
        for (provider in oauth2Providers) {
            provider.userInfoParser = classService.getInstance(provider.userInfoParserClass)
        }
    }

    class JwtToken {
        var id: String = DEFAULT_ID
        var secret: String = DEFAULT_SECRET
        var expirationIntervalMillis: Long = DEFAULT_EXPIRATION_INTERVAL_MILLIS
    }

    class Oauth2Provider {
        var id: String = ""
        var name: String = ""
        var authUrl: String = ""
        var accessTokenUrl: String = ""
        var apiUrl: String = ""
        var clientId: String = ""
        var clientSecret: String = ""
        var userInfoParserClass: Class<out UserInfoParser> = DefaultUserInfoParser::class.java
        lateinit var userInfoParser: UserInfoParser
    }

    companion object {
        private const val DEFAULT_PASSWORD_PATTERN: String = "^\\w{6,16}$"
        private const val DEFAULT_ID: String = "scisolutionsJWT"
        private const val DEFAULT_SECRET: String = "scisolutionsSecretKey"
        private const val DEFAULT_EXPIRATION_INTERVAL_MILLIS: Long = 86_400_000
    }
}