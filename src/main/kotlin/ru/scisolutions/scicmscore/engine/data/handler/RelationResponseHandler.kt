package ru.scisolutions.scicmscore.engine.data.handler

import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.RelationResponse

interface RelationResponseHandler {
    fun getRelationResponse(sourceItemRec: ItemRec, itemName: String, fields: Set<String>): RelationResponse
}