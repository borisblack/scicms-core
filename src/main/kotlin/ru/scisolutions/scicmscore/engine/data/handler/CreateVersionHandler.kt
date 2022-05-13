package ru.scisolutions.scicmscore.engine.data.handler

import ru.scisolutions.scicmscore.engine.data.model.input.CreateVersionInput
import ru.scisolutions.scicmscore.engine.data.model.response.Response

interface CreateVersionHandler {
    fun createVersion(itemName: String, input: CreateVersionInput, selectAttrNames: Set<String>): Response
}