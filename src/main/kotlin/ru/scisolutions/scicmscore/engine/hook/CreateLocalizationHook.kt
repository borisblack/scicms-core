package ru.scisolutions.scicmscore.engine.hook

import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.model.input.CreateLocalizationInput
import ru.scisolutions.scicmscore.engine.model.response.Response

interface CreateLocalizationHook {
    fun beforeCreateLocalization(itemName: String, input: CreateLocalizationInput, data: ItemRec)

    fun afterCreateLocalization(itemName: String, response: Response)
}