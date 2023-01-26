package ru.scisolutions.scicmscore.engine.model.response

class DatasetResponse(
    val data: List<Map<String, Any?>>,
    val meta: ResponseCollectionMeta = ResponseCollectionMeta()
)