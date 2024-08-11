package ru.scisolutions.scicmscore.model

class SecurityConfigResponse(
    val oauth2Providers: List<Oauth2ProviderConfigResponse>
) {
    class Oauth2ProviderConfigResponse(
        val id: String,
        val name: String,
        val authUrl: String,
        val clientId: String
    )
}
