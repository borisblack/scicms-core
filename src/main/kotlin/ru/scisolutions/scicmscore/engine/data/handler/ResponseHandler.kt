package ru.scisolutions.scicmscore.engine.data.handler

import ru.scisolutions.scicmscore.engine.data.model.Response

interface ResponseHandler {
    fun getResponse(itemName: String, fields: Set<String>, id: String): Response
}