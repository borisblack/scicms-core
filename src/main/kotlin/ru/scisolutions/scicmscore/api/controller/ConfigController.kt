package ru.scisolutions.scicmscore.api.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.scisolutions.scicmscore.config.props.SecurityProps
import ru.scisolutions.scicmscore.model.SecurityConfigResponse

@RestController
@RequestMapping("/api/config")
class ConfigController(
    private val securityProps: SecurityProps,
) {
    @GetMapping("/security")
    fun getSecurityConfig(): SecurityConfigResponse =
        SecurityConfigResponse(
            oauth2Providers = securityProps.oauth2Providers.map {
                SecurityConfigResponse.Oauth2ProviderConfigResponse(
                    id = it.id,
                    name = it.name,
                    authUrl = it.authUrl,
                    clientId = it.clientId
                )
            }
        )
}