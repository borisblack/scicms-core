package ru.scisolutions.scicmscore.security.model

import com.fasterxml.jackson.annotation.JsonProperty

class Oauth2AccessTokenRequest(
    @JsonProperty("grant_type")
    val authorizationCode: String = "authorization_code",
    @JsonProperty("client_id")
    val clientId: String,
    @JsonProperty("client_secret")
    val clientSecret: String,
    @JsonProperty("code")
    val accessCode: String,
)
