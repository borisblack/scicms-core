package ru.scisolutions.scicmscore.engine.model.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include

@JsonInclude(Include.NON_NULL)
class DatasetResponse(
    val data: List<Map<String, Any?>>,
    val meta: ResponseCollectionMeta = ResponseCollectionMeta()
)