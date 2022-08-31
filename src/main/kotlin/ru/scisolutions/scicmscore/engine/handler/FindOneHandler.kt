package ru.scisolutions.scicmscore.engine.handler

import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.model.response.RelationResponse
import ru.scisolutions.scicmscore.engine.model.response.Response
import java.util.UUID

interface FindOneHandler {
    fun findOne(itemName: String, id: UUID, selectAttrNames: Set<String>): Response

    fun findOneRelated(
        parentItemName: String,
        parentItemRec: ItemRec,
        parentAttrName: String,
        itemName: String,
        selectAttrNames: Set<String>
    ): RelationResponse
}