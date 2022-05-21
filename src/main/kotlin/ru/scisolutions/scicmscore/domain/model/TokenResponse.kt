package ru.scisolutions.scicmscore.domain.model

class TokenResponse(
    val jwt: String,
    val user: UserInfo,
    val expirationIntervalMillis: Long
)