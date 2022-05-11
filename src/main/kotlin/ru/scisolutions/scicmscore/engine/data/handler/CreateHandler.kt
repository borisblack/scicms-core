package ru.scisolutions.scicmscore.engine.data.handler

import ru.scisolutions.scicmscore.engine.data.model.input.ItemInput
import ru.scisolutions.scicmscore.engine.data.model.response.Response

interface CreateHandler {
    fun create(itemName: String, input: ItemInput, selectAttrNames: Set<String>): Response
}