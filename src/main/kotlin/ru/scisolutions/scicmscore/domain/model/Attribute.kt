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
    enum class Type(val value: String) {
        UUID("uuid"),
        STRING("string"),
        TEXT("text"),
        ENUM("enum"),
        SEQUENCE("sequence"),
        EMAIL("email"),
        PASSWORD("password"),
        INT("int"),
        FLOAT("float"),
        DECIMAL("decimal"),
        DATE("date"),
        TIME("time"),
        DATETIME("datetime"),
        TIMESTAMP("timestamp"),
        BOOL("bool"),
        ARRAY("array"),
        JSON("json"),
        MEDIA("media"),
        RELATION("relation")
    }

    enum class RelType(val value: String) {
        ONE_TO_ONE("oneToOne"),
        ONE_TO_MANY("oneToMany"),
        MANY_TO_ONE("manyToOne"),
        MANY_TO_MANY("manyToMany")
    }
}