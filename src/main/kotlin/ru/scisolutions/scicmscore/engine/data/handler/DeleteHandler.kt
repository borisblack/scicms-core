package ru.scisolutions.scicmscore.engine.data.handler

import ru.scisolutions.scicmscore.engine.data.model.input.DeleteInput
import ru.scisolutions.scicmscore.engine.data.model.response.Response

interface DeleteHandler {
    fun delete(itemName: String, input: DeleteInput, selectAttrNames: Set<String>): Response
}