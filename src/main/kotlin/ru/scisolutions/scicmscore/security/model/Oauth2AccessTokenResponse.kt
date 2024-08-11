package ru.scisolutions.scicmscore.security.model

import com.fasterxml.jackson.annotation.JsonProperty

class Oauth2AccessTokenResponse(
    @JsonProperty("token_type")
    val tokenType: String = "Bearer",
    @JsonProperty("expires_in")
    val expiresIn: Long,
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("refresh_token")
    val refreshToken: String
)
