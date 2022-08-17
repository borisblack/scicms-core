package ru.scisolutions.scicmscore.engine.handler

import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.engine.model.input.FindAllRelationInput
import ru.scisolutions.scicmscore.engine.model.input.FindAllInput
import ru.scisolutions.scicmscore.engine.model.response.RelationResponseCollection
import ru.scisolutions.scicmscore.engine.model.response.ResponseCollection

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