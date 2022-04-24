package ru.scisolutions.scicmscore.engine.data.handler

import ru.scisolutions.scicmscore.engine.data.model.UserInfo

interface UserHandler {
    fun me(): UserInfo?
}