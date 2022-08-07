package ru.scisolutions.scicmscore.engine.data.handler

import ru.scisolutions.scicmscore.domain.model.UserInfo

interface UserHandler {
    fun me(): UserInfo?
}