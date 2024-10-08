package ru.scisolutions.scicmscore.engine.model.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include

@JsonInclude(Include.NON_NULL)
open class Pagination(
    val page: Int? = null,
    val pageSize: Int? = null,
    val start: Int? = null,
    val limit: Int? = null,
    val total: Int?,
    val pageCount: Int?,
    var timeMs: Long? = null,
    var cacheHit: Boolean? = null
)
