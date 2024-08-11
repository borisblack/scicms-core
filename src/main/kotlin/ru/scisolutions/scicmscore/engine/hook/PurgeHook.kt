package ru.scisolutions.scicmscore.engine.hook

import ru.scisolutions.scicmscore.engine.model.input.DeleteInput
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.model.response.ResponseCollection

interface PurgeHook {
    fun beforePurge(itemName: String, input: DeleteInput, data: ItemRec)

    fun afterPurge(itemName: String, response: ResponseCollection)
}
