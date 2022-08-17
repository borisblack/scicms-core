package ru.scisolutions.scicmscore.engine.handler

import ru.scisolutions.scicmscore.engine.model.input.CreateInput
import ru.scisolutions.scicmscore.engine.model.response.Response

interface CreateHandler {
    fun create(itemName: String, input: CreateInput, selectAttrNames: Set<String>): Response
}