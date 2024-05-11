package ru.scisolutions.scicmscore.engine.hook

import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.model.input.CreateInput
import ru.scisolutions.scicmscore.engine.model.response.Response

interface CreateHook {
    fun beforeCreate(itemName: String, input: CreateInput, data: ItemRec)

    fun create(itemName: String, input: CreateInput, data: ItemRec): ItemRec?

    fun afterCreate(itemName: String, response: Response)
}