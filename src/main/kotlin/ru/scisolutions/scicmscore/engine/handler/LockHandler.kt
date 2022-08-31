package ru.scisolutions.scicmscore.engine.handler

import ru.scisolutions.scicmscore.engine.model.response.FlaggedResponse
import java.util.UUID

interface LockHandler {
    fun lock(itemName: String, id: UUID, selectAttrNames: Set<String>): FlaggedResponse

    fun unlock(itemName: String, id: UUID, selectAttrNames: Set<String>): FlaggedResponse
}