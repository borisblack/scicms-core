package ru.scisolutions.scicmscore.engine.hook

import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.model.input.CreateVersionInput
import ru.scisolutions.scicmscore.engine.model.response.Response

interface CreateVersionHook {
    fun beforeCreateVersion(itemName: String, input: CreateVersionInput, data: ItemRec)

    fun afterCreateVersion(itemName: String, response: Response)
}