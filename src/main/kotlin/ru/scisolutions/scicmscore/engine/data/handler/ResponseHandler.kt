package ru.scisolutions.scicmscore.engine.data.handler

import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.response.RelationResponse
import ru.scisolutions.scicmscore.engine.data.model.response.Response

interface ResponseHandler {
    fun getResponse(itemName: String, selectAttrNames: Set<String>, id: String): Response

    fun getRelationResponse(itemName: String, selectAttrNames: Set<String>, sourceItemRec: ItemRec, attrName: String): RelationResponse
}