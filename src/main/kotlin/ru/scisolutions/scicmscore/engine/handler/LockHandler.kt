package ru.scisolutions.scicmscore.engine.handler

import ru.scisolutions.scicmscore.engine.model.response.FlaggedResponse

interface LockHandler {
    fun lock(itemName: String, id: String, selectAttrNames: Set<String>): FlaggedResponse

    fun unlock(itemName: String, id: String, selectAttrNames: Set<String>): FlaggedResponse
}