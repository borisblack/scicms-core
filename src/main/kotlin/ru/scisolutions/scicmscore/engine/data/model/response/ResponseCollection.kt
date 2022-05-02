package ru.scisolutions.scicmscore.engine.data.model.response

import ru.scisolutions.scicmscore.engine.data.model.ItemRec

class ResponseCollection(
    val data: List<ItemRec>,
    val meta: ResponseCollectionMeta = ResponseCollectionMeta()
)