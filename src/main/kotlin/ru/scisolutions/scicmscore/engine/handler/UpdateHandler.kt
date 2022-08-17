package ru.scisolutions.scicmscore.engine.handler

import ru.scisolutions.scicmscore.engine.model.input.UpdateInput
import ru.scisolutions.scicmscore.engine.model.response.Response

interface UpdateHandler {
    fun update(itemName: String, input: UpdateInput, selectAttrNames: Set<String>): Response
}