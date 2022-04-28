package ru.scisolutions.scicmscore.engine.data.handler

import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.RelationResponse

interface RelationResponseHandler {
    fun getRelationResponse(itemName: String, fields: Set<String>, sourceItemRec: ItemRec, fieldName: String): RelationResponse
}