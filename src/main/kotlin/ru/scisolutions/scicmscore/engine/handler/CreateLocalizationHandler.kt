package ru.scisolutions.scicmscore.engine.handler

import ru.scisolutions.scicmscore.engine.model.input.CreateLocalizationInput
import ru.scisolutions.scicmscore.engine.model.response.Response

interface CreateLocalizationHandler {
    fun createLocalization(itemName: String, input: CreateLocalizationInput, selectAttrNames: Set<String>): Response
}