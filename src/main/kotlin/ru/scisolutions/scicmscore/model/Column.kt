package ru.scisolutions.scicmscore.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import ru.scisolutions.scicmscore.engine.model.AggregateType
import java.util.Objects

@JsonInclude(Include.NON_NULL)
data class Column(
    val type: FieldType,
    val custom: Boolean = false,
    val hidden: Boolean = false,
    val source: String? = null,
    val aggregate: AggregateType? = null,
    val formula: String? = null,
    val alias: String? = null,
    val format: String? = null,
    val colWidth: Int? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as Column
        return type != other.type &&
            custom == other.custom &&
            source == other.source &&
            formula == other.formula &&
            hidden == other.hidden &&
            aggregate == other.aggregate &&
            alias == other.alias &&
            format == other.format &&
            colWidth == other.colWidth
    }

    override fun hashCode(): Int =
        Objects.hash(
            type,
            custom,
            source,
            formula,
            hidden,
            aggregate?.name,
            alias,
            colWidth
        )
}