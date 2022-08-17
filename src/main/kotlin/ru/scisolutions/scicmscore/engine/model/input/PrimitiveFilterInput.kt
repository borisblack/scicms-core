package ru.scisolutions.scicmscore.engine.model.input

class PrimitiveFilterInput(
    val containsFilter: String?,
    val notContainsFilter: String?,
    val containsiFilter: String?,
    val notContainsiFilter: String?,
    val startsWithFilter: String?,
    val endsWithFilter: String?,
    val eqFilter: Any?,
    val neFilter: Any?,
    val gtFilter: Any?,
    val gteFilter: Any?,
    val ltFilter: Any?,
    val lteFilter: Any?,
    val betweenFilter: Pair?,
    val inFilter: List<Any?>?,
    val notInFilter: List<Any?>?,
    val nullFilter: Boolean?,
    val notNullFilter: Boolean?,
    andFilterList: List<PrimitiveFilterInput>?,
    orFilterList: List<PrimitiveFilterInput>?,
    notFilter: PrimitiveFilterInput?
) : AbstractFilterInput<PrimitiveFilterInput>(andFilterList, orFilterList, notFilter) {
    class Pair(
        val left: Any,
        val right: Any
    )

    companion object {
        private const val CONTAINS_KEY = "contains"
        private const val CONTAINSI_KEY = "containsi"
        private const val NOT_CONTAINS_KEY = "notContains"
        private const val NOT_CONTAINSI_KEY = "notContainsi"
        private const val STARTS_WITH_KEY = "startsWith"
        private const val ENDS_WITH_KEY = "endsWith"
        private const val EQ_KEY = "eq"
        private const val NE_KEY = "ne"
        private const val GT_KEY = "gt"
        private const val GTE_KEY = "gte"
        private const val LT_KEY = "lt"
        private const val LTE_KEY = "lte"
        private const val BETWEEN_KEY = "between"
        private const val IN_KEY = "in"
        private const val NOT_IN_KEY = "notIn"
        private const val NULL_KEY = "null"
        private const val NOT_NULL_KEY = "notNull"

        fun fromMap(filters: Map<String, Any>): PrimitiveFilterInput = PrimitiveFilterInput(
            containsFilter = filters[CONTAINS_KEY]?.toString(),
            notContainsFilter = filters[NOT_CONTAINS_KEY]?.toString(),
            containsiFilter = filters[CONTAINSI_KEY]?.toString(),
            notContainsiFilter = filters[NOT_CONTAINSI_KEY]?.toString(),
            startsWithFilter = filters[STARTS_WITH_KEY]?.toString(),
            endsWithFilter = filters[ENDS_WITH_KEY]?.toString(),
            eqFilter = filters[EQ_KEY],
            neFilter = filters[NE_KEY],
            gtFilter = filters[GT_KEY],
            gteFilter = filters[GTE_KEY],
            ltFilter = filters[LT_KEY],
            lteFilter = filters[LTE_KEY],

            betweenFilter = filters[BETWEEN_KEY]?.let {
                if (it is List<*> && it.size == 2 && it[0] != null && it[1] != null) Pair(it[0] as Any, it[1] as Any) else null
            },

            inFilter = filters[IN_KEY]?.let {
                if (it is List<*>) it.filterNotNull() else null
            },

            notInFilter = filters[NOT_IN_KEY]?.let {
                if (it is List<*>) it.filterNotNull() else null
            },

            nullFilter = filters[NULL_KEY] as Boolean?,
            notNullFilter = filters[NOT_NULL_KEY] as Boolean?,

            andFilterList = filters[AND_KEY]?.let { list ->
                if (list is List<*>) {
                    list.filterIsInstance<Map<*, *>>()
                        .map { fromMap(it as Map<String, Any>) }
                } else
                    null
            },

            orFilterList = filters[OR_KEY]?.let { list ->
                if (list is List<*>) {
                    list.filterIsInstance<Map<*, *>>()
                        .map { fromMap(it as Map<String, Any>) }
                } else
                    null
            },

            notFilter = filters[NOT_KEY]?.let {
                if (it is Map<*, *>) fromMap(it as Map<String, Any>) else null
            }
        )
    }
}