package ru.scisolutions.scicmscore.model

class Column(
    val type: FieldType,
    val format: String? = null,
    val alias: String? = null,
    val colWidth: Int? = null
)