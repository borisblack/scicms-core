package ru.scisolutions.scicmscore.engine.data.model

import ru.scisolutions.scicmscore.engine.data.model.response.Response

interface LockHook {
    fun beforeLock(itemName: String, id: String)

    fun afterLock(itemName: String, response: Response)

    fun beforeUnlock(itemName: String, id: String)

    fun afterUnlock(itemName: String, response: Response)
}