package ru.scisolutions.scicmscore.engine.data.handler

import ru.scisolutions.scicmscore.engine.data.model.input.DeleteInput
import ru.scisolutions.scicmscore.engine.data.model.response.ResponseCollection

interface PurgeHandler {
    fun purge(itemName: String, input: DeleteInput, selectAttrNames: Set<String>): ResponseCollection
}