package ru.scisolutions.scicmscore.engine.data.handler

import ru.scisolutions.scicmscore.engine.data.model.input.ResponseCollectionInput
import ru.scisolutions.scicmscore.engine.data.model.response.ResponseCollection

interface ResponseCollectionHandler {
    fun getResponseCollection(itemName: String, input: ResponseCollectionInput, selectAttrNames: Set<String>): ResponseCollection
}