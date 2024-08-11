package ru.scisolutions.scicmscore.security.service.impl

import ru.scisolutions.scicmscore.security.service.UserInfoParser

class DefaultUserInfoParser : UserInfoParser {
    override fun parseUsername(payload: Any): String {
        if (payload !is Map<*, *>) {
            throw IllegalArgumentException("Invalid payload type.")
        }

        val login = payload["login"]
        if (login !is String) {
            throw IllegalArgumentException("Invalid login type.")
        }

        return login
    }

    override fun parseAuthorities(payload: Any): Set<String> {
        return emptySet()
    }
}
