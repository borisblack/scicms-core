package ru.scisolutions.scicmscore.model

class UserInfo(
    val id: String,
    val username: String,
    val roles: Set<String>,
    val authType: AuthType,
    val sessionData: Map<String, Any?>?
)