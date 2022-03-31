package ru.scisolutions.scicmscore.dbschema.model

class Property(
    val type: String,
    val columnName: String,
    val enumName: String? = null,
    val target: String? = null,
    val relType: String? = null,
    val displayName: String,
    val description: String? = null,
    val pattern: String? = null, // for string type
    val defaultValue: String? = null,
    val required: Boolean = false,
    val unique: Boolean = false,
    val indexed: Boolean = false,
    val private: Boolean = false,
    val length: Int? = null, // for string type
    val precision: Int? = null, // for float, decimal types
    val scale: Int? = null, // for float, decimal types
    val minRange: Int? = null, // for int, float, decimal types
    val maxRange: Int? = null // for int, float, decimal types
)