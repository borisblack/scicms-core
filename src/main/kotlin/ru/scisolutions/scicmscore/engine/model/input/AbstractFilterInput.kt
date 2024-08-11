package ru.scisolutions.scicmscore.engine.model.input

abstract class AbstractFilterInput<T : AbstractFilterInput<T>>(
    val andFilterList: List<T>?,
    val orFilterList: List<T>?,
    val notFilter: T?
) {
    class BetweenPair(
        val left: Any,
        val right: Any
    )

    companion object {
        const val CONTAINS_KEY = "contains"
        const val CONTAINSI_KEY = "containsi"
        const val NOT_CONTAINS_KEY = "notContains"
        const val NOT_CONTAINSI_KEY = "notContainsi"
        const val STARTS_WITH_KEY = "startsWith"
        const val ENDS_WITH_KEY = "endsWith"
        const val EQ_KEY = "eq"
        const val NE_KEY = "ne"
        const val GT_KEY = "gt"
        const val GTE_KEY = "gte"
        const val LT_KEY = "lt"
        const val LTE_KEY = "lte"
        const val BETWEEN_KEY = "between"
        const val IN_KEY = "in"
        const val NOT_IN_KEY = "notIn"
        const val NULL_KEY = "null"
        const val NOT_NULL_KEY = "notNull"

        const val AND_KEY = "and"
        const val OR_KEY = "or"
        const val NOT_KEY = "not"
    }
}
