package ru.scisolutions.scicmscore.engine.handler

import ru.scisolutions.scicmscore.engine.model.input.PromoteInput
import ru.scisolutions.scicmscore.engine.model.response.Response

interface PromoteHandler {
    fun promote(itemName: String, input: PromoteInput, selectAttrNames: Set<String>): Response
}