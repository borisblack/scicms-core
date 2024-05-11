package ru.scisolutions.scicmscore.engine.hook

import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.model.input.UpdateInput
import ru.scisolutions.scicmscore.engine.model.response.Response

interface UpdateHook {
    fun beforeUpdate(itemName: String, input: UpdateInput, data: ItemRec)

    fun update(itemName: String, input: UpdateInput, data: ItemRec): ItemRec?

    fun afterUpdate(itemName: String, response: Response)
}