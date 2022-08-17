package ru.scisolutions.scicmscore.engine.handler

import ru.scisolutions.scicmscore.model.UserInfo

interface UserHandler {
    fun me(): UserInfo?
}