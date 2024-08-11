package ru.scisolutions.scicmscore.security.service

interface UserInfoParser {
    fun parseUsername(payload: Any): String

    fun parseAuthorities(payload: Any): Set<String>
}
