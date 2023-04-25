package ru.scisolutions.scicmscore.engine.handler

import ru.scisolutions.scicmscore.engine.model.response.SessionDataResponse
import ru.scisolutions.scicmscore.model.UserInfo

interface UserHandler {
    fun me(): UserInfo?

    fun updateSessionData(sessionData: Map<String, Any?>?): SessionDataResponse
}