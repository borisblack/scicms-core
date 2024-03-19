package ru.scisolutions.scicmscore.model

class TokenResponse(
    val jwt: String,
    val user: UserInfo,
    val expirationIntervalMillis: Long,
    val authType: AuthType
)