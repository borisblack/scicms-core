package ru.scisolutions.scicmscore.engine.model.response

import ru.scisolutions.scicmscore.engine.model.UserInfo

class TokenResponse(
    val jwt: String,
    val user: UserInfo,
    val expirationIntervalMillis: Long,
)
