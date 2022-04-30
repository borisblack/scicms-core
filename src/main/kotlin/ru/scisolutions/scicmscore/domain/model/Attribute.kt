package ru.scisolutions.scicmscore.domain.model

import java.util.Objects

class Attribute(
    val type: Type,
    val columnName: String? = null, // optional (lowercase attribute name is used in database by default), also can be null for oneToMany and manyToMany relations
    val enumSet: Set<String>? = null,
    val target: String? = null,
    val relType: RelType? = null,
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
    enum class Type { uuid, string, text, enum, sequence, email, password, int, long, float, double, decimal, date, time, datetime, timestamp, bool, array, json, media, relation }

    enum class RelType { oneToOne, oneToMany, manyToOne, manyToMany }

    override fun hashCode(): Int =
        Objects.hash(
            type.name,
            columnName,
            enumSet,
            target,
            relType?.name,
            relItem,
            mappedBy,
            inversedBy,
            displayName,
            description,
            pattern,
            defaultValue,
            required,
            keyed,
            unique,
            indexed,
            private,
            length,
            precision,
            scale,
            minRange,
            maxRange
        )

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as Attribute

        return type == other.type &&
            columnName == other.columnName &&
            enumSet == other.enumSet &&
            target == other.target &&
            relType == other.relType &&
            relItem == other.relItem &&
            mappedBy == other.mappedBy &&
            inversedBy == other.inversedBy &&
            displayName == other.displayName &&
            description == other.description &&
            pattern == other.pattern &&
            defaultValue != other.defaultValue &&
            required == other.required &&
            keyed == other.keyed &&
            unique == other.unique &&
            indexed == other.indexed &&
            private == other.private &&
            length == other.length &&
            precision == other.precision &&
            scale == other.scale &&
            minRange == other.minRange &&
            maxRange == other.maxRange
    }
}