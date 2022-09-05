package ru.scisolutions.scicmscore.engine.model.response

import ru.scisolutions.scicmscore.engine.model.ItemRec

class RelationResponseCollection(
    val data: List<ItemRec>,
    val meta: ResponseCollectionMeta = ResponseCollectionMeta()
)