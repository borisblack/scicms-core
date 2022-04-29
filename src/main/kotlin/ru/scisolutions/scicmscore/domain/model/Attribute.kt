package ru.scisolutions.scicmscore.domain.model

data class Attribute(
    val type: String,
    val columnName: String? = null, // optional (lowercase attribute name is used in database by default), also can be null for oneToMany and manyToMany relations
    val enumSet: Set<String>? = null,
    val target: String? = null,
    val relType: String? = null,
    val relItem: String? = null,
    val mappedBy: String? = null,
    val inversedBy: String? = null,
    val displayName: String,
    val description: String? = null,
    val pattern: String? = null, // for string type
    val defaultValue: String? = null,
    val required: Boolean = false,
    val keyed: Boolean = false, // primary key, only for internal use!
    val unique: Boolean = false,
    val indexed: Boolean = false,
    val private: Boolean = false,
    val length: Int? = null, // for string type
    val precision: Int? = null, // for float, decimal types
    val scale: Int? = null, // for float, decimal types
    val minRange: Int? = null, // for int, float, decimal types
    val maxRange: Int? = null // for int, float, decimal types
) {
    enum class Type { uuid, string, text, enum, sequence, email, password, int, float, decimal, date, time, datetime, timestamp, bool, array, json, media, relation }

    enum class RelType {
        oneToOne, oneToMany, manyToOne, manyToMany;

        companion object {
            fun nullableValueOf(value: String?) = if (value == null) null else valueOf(value)
        }
    }
}