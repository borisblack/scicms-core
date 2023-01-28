package ru.scisolutions.scicmscore.engine.model.input

import ru.scisolutions.scicmscore.model.Attribute.Type as AttrType

class TypedPrimitiveFilterInput(
    val attrType: AttrType,
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
    val betweenFilter: BetweenPair?,
    val inFilter: List<Any?>?,
    val notInFilter: List<Any?>?,
    val nullFilter: Boolean?,
    val notNullFilter: Boolean?,
    andFilterList: List<TypedPrimitiveFilterInput>?,
    orFilterList: List<TypedPrimitiveFilterInput>?,
    notFilter: TypedPrimitiveFilterInput?
) : AbstractFilterInput<TypedPrimitiveFilterInput>(andFilterList, orFilterList, notFilter) {
    companion object {
        fun fromMap(attrType: AttrType, filters: Map<String, Any>, opPrefix: String = ""): TypedPrimitiveFilterInput = TypedPrimitiveFilterInput(
            attrType = attrType,
            containsFilter = filters["${opPrefix}$CONTAINS_KEY"]?.toString(),
            notContainsFilter = filters["${opPrefix}$NOT_CONTAINS_KEY"]?.toString(),
            containsiFilter = filters["${opPrefix}$CONTAINSI_KEY"]?.toString(),
            notContainsiFilter = filters["${opPrefix}$NOT_CONTAINSI_KEY"]?.toString(),
            startsWithFilter = filters["${opPrefix}$STARTS_WITH_KEY"]?.toString(),
            endsWithFilter = filters["${opPrefix}$ENDS_WITH_KEY"]?.toString(),
            eqFilter = filters["${opPrefix}$EQ_KEY"],
            neFilter = filters["${opPrefix}$NE_KEY"],
            gtFilter = filters["${opPrefix}$GT_KEY"],
            gteFilter = filters["${opPrefix}$GTE_KEY"],
            ltFilter = filters["${opPrefix}$LT_KEY"],
            lteFilter = filters["${opPrefix}$LTE_KEY"],

            betweenFilter = filters["${opPrefix}$BETWEEN_KEY"]?.let {
                if (it is List<*> && it.size == 2 && it[0] != null && it[1] != null)
                    BetweenPair(it[0] as Any, it[1] as Any)
                else
                    throw IllegalArgumentException("Invalid BETWEEN filter")
            },

            inFilter = filters["${opPrefix}$IN_KEY"]?.let {
                if (it is List<*>) it.filterNotNull() else throw IllegalArgumentException("Invalid IN filter")
            },

            notInFilter = filters["${opPrefix}$NOT_IN_KEY"]?.let {
                if (it is List<*>) it.filterNotNull() else throw IllegalArgumentException("Invalid NOT IN filter")
            },

            nullFilter = filters["${opPrefix}$NULL_KEY"] as Boolean?,
            notNullFilter = filters["${opPrefix}$NOT_NULL_KEY"] as Boolean?,

            andFilterList = filters[AND_KEY]?.let { list ->
                if (list is List<*>) {
                    list.filterIsInstance<Map<*, *>>()
                        .map { fromMap(attrType, it as Map<String, Any>, opPrefix) }
                } else {
                    throw IllegalArgumentException("Invalid AND clause")
                }
            },

            orFilterList = filters[OR_KEY]?.let { list ->
                if (list is List<*>) {
                    list.filterIsInstance<Map<*, *>>()
                        .map { fromMap(attrType, it as Map<String, Any>, opPrefix) }
                } else {
                    throw IllegalArgumentException("Invalid OR clause")
                }
            },

            notFilter = filters[NOT_KEY]?.let {
                if (it is Map<*, *>)
                    fromMap(attrType, it as Map<String, Any>, opPrefix)
                else
                    throw IllegalArgumentException("Invalid NOT clause")
            }
        )
    }
}