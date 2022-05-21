package ru.scisolutions.scicmscore.config.props

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "scicms-core.security")
class SecurityProps {
    var jwtToken: JwtToken = JwtToken()
    var registrationDisabled: Boolean = false

    class JwtToken {
        var id: String = DEFAULT_ID
        var secret: String = DEFAULT_SECRET
        var expirationIntervalMillis: Long = DEFAULT_EXPIRATION_INTERVAL_MILLIS
    }

    companion object {
        const val DEFAULT_ID: String = "scisolutionsJWT"
        const val DEFAULT_SECRET: String = "scisolutionsSecretKey"
        const val DEFAULT_EXPIRATION_INTERVAL_MILLIS: Long = 1_800_000
    }
}