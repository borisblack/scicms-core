package ru.scisolutions.scicmscore.engine.data.model

import ru.scisolutions.scicmscore.engine.data.model.input.CreateLocalizationInput
import ru.scisolutions.scicmscore.engine.data.model.response.Response

interface CreateLocalizationHook {
    fun beforeCreateLocalization(itemName: String, input: CreateLocalizationInput)

    fun afterCreateLocalization(itemName: String, response: Response)
}