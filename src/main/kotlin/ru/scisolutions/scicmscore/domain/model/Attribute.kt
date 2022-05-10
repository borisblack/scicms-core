package ru.scisolutions.scicmscore.domain.model

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.Objects

class Attribute(
    val type: Type,
    val relType: RelType? = null,
    val target: String? = null, // by default, id is used as the key attribute for target. But for unidirectional oneToOne and manyToOne relations another key attribute can be specified in brackets, for example, product(code)
    val intermediate: String? = null, // intermediate item is used for manyToMany association and includes source and target attributes
    val mappedBy: String? = null,
    val inversedBy: String? = null,
    val enumSet: Set<String>? = null,
    val columnName: String? = null, // optional (lowercase attribute name is used in database by default), also can be null for oneToMany and manyToMany relations
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
    @JsonIgnore
    fun isCollection() = (type == Type.relation && (relType == RelType.oneToMany || relType == RelType.manyToMany))

    override fun hashCode(): Int =
        Objects.hash(
            type.name,
            columnName,
            enumSet,
            target,
            relType?.name,
            intermediate,
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
            intermediate == other.intermediate &&
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

    enum class Type { uuid, string, text, enum, sequence, email, password, int, long, float, double, decimal, date, time, datetime, timestamp, bool, array, json, media, relation }

    enum class RelType { oneToOne, oneToMany, manyToOne, manyToMany }
}