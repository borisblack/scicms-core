package ru.scisolutions.scicmscore.engine.data.handler

import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.model.input.FindAllRelationInput
import ru.scisolutions.scicmscore.engine.data.model.input.FindAllInput
import ru.scisolutions.scicmscore.engine.data.model.response.RelationResponseCollection
import ru.scisolutions.scicmscore.engine.data.model.response.ResponseCollection

interface FindAllHandler {
    fun findAll(
        itemName: String,
        input: FindAllInput,
        selectAttrNames: Set<String>,
        selectPaginationFields: Set<String>
    ): ResponseCollection

    fun findAllRelated(
        parentItemName: String,
        parentItemRec: ItemRec,
        parentAttrName: String,
        itemName: String,
        input: FindAllRelationInput,
        selectAttrNames: Set<String>,
        selectPaginationFields: Set<String>
    ): RelationResponseCollection
}