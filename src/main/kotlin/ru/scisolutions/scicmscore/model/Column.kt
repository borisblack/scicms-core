package ru.scisolutions.scicmscore.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include

@JsonInclude(Include.NON_NULL)
class Column(
    val type: FieldType,
    val format: String? = null,
    val alias: String? = null,
    val colWidth: Int? = null
)