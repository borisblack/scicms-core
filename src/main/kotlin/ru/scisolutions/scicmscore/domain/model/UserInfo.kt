package ru.scisolutions.scicmscore.domain.model

class UserInfo(
    val id: String,
    val username: String,
    val roles: Set<String>
)