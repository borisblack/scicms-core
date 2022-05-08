package ru.scisolutions.scicmscore.engine.data.handler

import ru.scisolutions.scicmscore.engine.data.model.response.Response

interface CreateHandler {
    fun create(itemName: String, data: Map<String, Any?>, selectAttrNames: Set<String>): Response
}