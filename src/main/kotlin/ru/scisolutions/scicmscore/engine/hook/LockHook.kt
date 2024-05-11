package ru.scisolutions.scicmscore.engine.hook

import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.model.response.FlaggedResponse

interface LockHook {
    fun beforeLock(itemName: String, id: String, data: ItemRec)

    fun afterLock(itemName: String, response: FlaggedResponse)

    fun beforeUnlock(itemName: String, id: String, data: ItemRec)

    fun afterUnlock(itemName: String, response: FlaggedResponse)
}