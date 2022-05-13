package ru.scisolutions.scicmscore.engine.data.handler

import ru.scisolutions.scicmscore.engine.data.model.input.CreateLocalizationInput
import ru.scisolutions.scicmscore.engine.data.model.response.Response

interface CreateLocalizationHandler {
    fun createLocalization(itemName: String, input: CreateLocalizationInput, selectAttrNames: Set<String>): Response
}