package ru.scisolutions.scicmscore.engine.data.handler

import ru.scisolutions.scicmscore.engine.data.model.input.UpdateInput
import ru.scisolutions.scicmscore.engine.data.model.response.Response

interface UpdateHandler {
    fun update(itemName: String, input: UpdateInput, selectAttrNames: Set<String>): Response
}