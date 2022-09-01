package ru.scisolutions.scicmscore.engine.model

import ru.scisolutions.scicmscore.engine.model.response.FlaggedResponse

interface LockHook {
    fun beforeLock(itemName: String, id: String)

    fun afterLock(itemName: String, response: FlaggedResponse)

    fun beforeUnlock(itemName: String, id: String)

    fun afterUnlock(itemName: String, response: FlaggedResponse)
}