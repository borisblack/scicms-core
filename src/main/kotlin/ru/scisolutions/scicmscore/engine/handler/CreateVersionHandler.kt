package ru.scisolutions.scicmscore.engine.handler

import ru.scisolutions.scicmscore.engine.model.input.CreateVersionInput
import ru.scisolutions.scicmscore.engine.model.response.Response

interface CreateVersionHandler {
    fun createVersion(itemName: String, input: CreateVersionInput, selectAttrNames: Set<String>): Response
}