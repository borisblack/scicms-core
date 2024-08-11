package ru.scisolutions.scicmscore.engine.model.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import ru.scisolutions.scicmscore.engine.model.DatasetRec

@JsonInclude(Include.NON_NULL)
class DatasetResponse(
    val data: List<DatasetRec> = emptyList(),
    val query: String? = null,
    val params: Map<String, Any?>? = null,
    val timeMs: Long? = null,
    val cacheHit: Boolean? = null,
    val meta: ResponseCollectionMeta = ResponseCollectionMeta()
)
