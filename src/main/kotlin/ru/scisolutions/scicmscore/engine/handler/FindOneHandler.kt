package ru.scisolutions.scicmscore.engine.handler

import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.model.response.RelationResponse
import ru.scisolutions.scicmscore.engine.model.response.Response

interface FindOneHandler {
    fun findOne(itemName: String, id: String, selectAttrNames: Set<String>): Response

    fun findOneRelated(
        parentItemRec: ItemRec,
        parentAttrName: String,
        itemName: String,
        selectAttrNames: Set<String>
    ): RelationResponse
}