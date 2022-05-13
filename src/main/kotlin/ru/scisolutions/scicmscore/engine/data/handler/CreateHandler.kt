package ru.scisolutions.scicmscore.engine.data.handler

import ru.scisolutions.scicmscore.engine.data.model.input.CreateInput
import ru.scisolutions.scicmscore.engine.data.model.response.Response

interface CreateHandler {
    fun create(itemName: String, input: CreateInput, selectAttrNames: Set<String>): Response
}