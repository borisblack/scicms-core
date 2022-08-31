package ru.scisolutions.scicmscore.engine.model

import ru.scisolutions.scicmscore.engine.model.response.FlaggedResponse
import java.util.UUID

interface LockHook {
    fun beforeLock(itemName: String, id: UUID)

    fun afterLock(itemName: String, response: FlaggedResponse)

    fun beforeUnlock(itemName: String, id: UUID)

    fun afterUnlock(itemName: String, response: FlaggedResponse)
}