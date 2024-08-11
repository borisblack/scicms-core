package ru.scisolutions.scicmscore.config.props

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "scicms-core.media")
class MediaProps {
    var provider: String = PROVIDER_DEFAULT

    @NestedConfigurationProperty
    var providerOptions: ProviderOptions = ProviderOptions()

    class ProviderOptions {
        var local: LocalProviderOptions = LocalProviderOptions()
        var s3: S3ProviderOptions = S3ProviderOptions()
    }

    class LocalProviderOptions {
        var basePath: String? = null
        var createDirectories: Boolean = true
    }

    class S3ProviderOptions {
        var endpoint: String? = null
        var accessKey: String? = null
        var secretKey: String? = null
        var defaultBucket: String? = null
    }

    companion object {
        const val PROVIDER_LOCAL = "local"
        const val PROVIDER_S3 = "s3"
        private const val PROVIDER_DEFAULT = PROVIDER_LOCAL
    }
}
