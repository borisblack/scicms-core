package ru.scisolutions.scicmscore.engine.model.response

import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec

class ResponseCollection(
    val data: List<ItemRec>,
    val meta: ResponseCollectionMeta = ResponseCollectionMeta()
)