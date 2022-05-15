package ru.scisolutions.scicmscore.engine.data.handler

import ru.scisolutions.scicmscore.engine.data.model.input.PromoteInput
import ru.scisolutions.scicmscore.engine.data.model.response.Response

interface PromoteHandler {
    fun promote(itemName: String, input: PromoteInput, selectAttrNames: Set<String>): Response
}