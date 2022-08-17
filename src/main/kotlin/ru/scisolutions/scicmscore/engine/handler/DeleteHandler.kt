package ru.scisolutions.scicmscore.engine.handler

import ru.scisolutions.scicmscore.engine.model.input.DeleteInput
import ru.scisolutions.scicmscore.engine.model.response.Response

interface DeleteHandler {
    fun delete(itemName: String, input: DeleteInput, selectAttrNames: Set<String>): Response
}