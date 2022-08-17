package ru.scisolutions.scicmscore.engine.handler

import ru.scisolutions.scicmscore.engine.model.input.DeleteInput
import ru.scisolutions.scicmscore.engine.model.response.ResponseCollection

interface PurgeHandler {
    fun purge(itemName: String, input: DeleteInput, selectAttrNames: Set<String>): ResponseCollection
}