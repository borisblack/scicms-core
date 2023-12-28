package ru.scisolutions.scicmscore.model

import java.util.Objects

data class JoinedTable(
    val name: String,
    val columns: Map<String, Column>,
    val joinType: JoinType? = null,
    val joins: List<Join>
) {
    enum class JoinType {
        inner, left, right, full
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as JoinedTable

        return name == other.name &&
            columns == other.columns &&
            joinType == other.joinType &&
            joins == other.joins
    }

    override fun hashCode(): Int =
        Objects.hash(
            name,
            columns,
            joinType?.name,
            joins
        )
}