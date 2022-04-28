package ru.scisolutions.scicmscore.engine.data.handler

import ru.scisolutions.scicmscore.engine.data.model.Response

interface ResponseHandler {
    fun getResponse(itemName: String, id: String, fields: Set<String>): Response
}