package ru.scisolutions.scicmscore.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import ru.scisolutions.scicmscore.engine.model.AggregateType
import java.util.Objects

@JsonInclude(Include.NON_NULL)
data class Column(
    val type: FieldType,
    val source: String? = null,
    val isVisible: Boolean = true,
    val aggregate: AggregateType? = null,
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
            source == other.source &&
            isVisible == other.isVisible &&
            aggregate == other.aggregate &&
            alias == other.alias &&
            format == other.format &&
            colWidth == other.colWidth
    }

    override fun hashCode(): Int =
        Objects.hash(
            type,
            source,
            isVisible,
            aggregate?.name,
            alias,
            colWidth
        )
}