package ru.scisolutions.scicmscore.engine.data.model

class ResponseCollection(
    val data: List<ItemRec>,
    val meta: ResponseCollectionMeta = ResponseCollectionMeta()
)