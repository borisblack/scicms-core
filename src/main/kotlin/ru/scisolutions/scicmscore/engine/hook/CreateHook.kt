package ru.scisolutions.scicmscore.engine.hook

import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.model.input.CreateInput
import ru.scisolutions.scicmscore.engine.model.response.Response

interface CreateHook {
    /**
     * If this method returns not null, the engine will not create the data.
     */
    fun beforeCreate(itemName: String, input: CreateInput, data: ItemRec): ItemRec?

    fun afterCreate(itemName: String, response: Response)
}