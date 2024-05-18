package ru.scisolutions.scicmscore.engine.hook

import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.model.input.UpdateInput
import ru.scisolutions.scicmscore.engine.model.response.Response

interface UpdateHook {
    /**
     * If this method returns not null, the engine will not update the data.
     */
    fun beforeUpdate(itemName: String, input: UpdateInput, data: ItemRec): ItemRec?

    fun afterUpdate(itemName: String, response: Response)
}