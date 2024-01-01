package ru.scisolutions.scicmscore.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import ru.scisolutions.scicmscore.engine.model.AggregateType

@JsonInclude(Include.NON_NULL)
class Column(
    val type: FieldType,
    val isVisible: Boolean = true,
    val asAlias: String? = null,
    val aggregate: AggregateType? = null,
    val alias: String? = null,
    val format: String? = null,
    val colWidth: Int? = null
)