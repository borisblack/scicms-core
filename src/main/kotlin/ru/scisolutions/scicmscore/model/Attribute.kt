package ru.scisolutions.scicmscore.model

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.Objects

class Attribute(
    val type: Type,
    val columnName: String? = null, // optional (lowercase attribute name is used in database by default), also can be null for oneToMany and manyToMany relations
    val displayName: String? = null,
    val description: String? = null,
    val enumSet: Set<String>? = null,
    val seqName: String? = null,
    val relType: RelType? = null,
    val target: String? = null,
    val intermediate: String? = null, // intermediate item is used for manyToMany association and includes source and target attributes
    val mappedBy: String? = null,
    val inversedBy: String? = null,
    val required: Boolean = false,
    val readOnly: Boolean = false,
    val defaultValue: String? = null,
    val keyed: Boolean = false, // primary key, used only for id attribute
    val unique: Boolean = false,
    val indexed: Boolean = false,
    val private: Boolean = false,
    val pattern: String? = null, // for string type
    val length: Int? = null, // for string type
    val precision: Int? = null, // for decimal types
    val scale: Int? = null, // for decimal types
    val minRange: Long? = null, // for int, long, float, double, decimal types
    val maxRange: Long? = null, // for int, long, float, double, decimal types
    val colHidden: Boolean? = null, // hide column in UI table
    val colWidth: Int? = null, // column width in UI table
    val fieldHidden: Boolean? = null, // hide field in UI form
    val fieldWidth: Int? = null // field width in UI form
) {
    @JsonIgnore
    fun isCollection() = (type == Type.relation && (relType == RelType.oneToMany || relType == RelType.manyToMany))

    fun validate() {
        when (type) {
            Type.string -> {
                if (length == null || length <= 0)
                    throw IllegalArgumentException("Invalid string length (${length})")
            }
            Type.enum -> {
                if (enumSet == null)
                    throw IllegalArgumentException("The enumSet is required for the enum type")
            }
            Type.sequence -> {
                if (seqName == null)
                    throw IllegalArgumentException("The seqName is required for the sequence type")
            }
            Type.int, Type.long, Type.float, Type.double -> {
                if (minRange != null && maxRange != null && minRange > maxRange)
                    throw IllegalArgumentException("Invalid range ratio (minRange=${minRange} > maxRange=${maxRange})")
            }
            Type.decimal -> {
                if ((precision != null && precision <= 0) || (scale != null && scale < 0))
                    throw IllegalArgumentException("Invalid precision and/or scale (${precision}, ${scale})")

                if (minRange != null && maxRange != null && minRange > maxRange)
                    throw IllegalArgumentException("Invalid range ratio (minRange=${minRange} > maxRange=${maxRange})")
            }
            Type.relation -> {
                if (isCollection() && required)
                    throw IllegalArgumentException("Collection relation attribute cannot be required")
            }
            else -> {}
        }
    }

    override fun hashCode(): Int =
        Objects.hash(
            type.name,
            columnName,
            enumSet,
            seqName,
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
            readOnly,
            keyed,
            unique,
            indexed,
            private,
            length,
            precision,
            scale,
            minRange,
            maxRange,
            colHidden,
            colWidth,
            fieldWidth,
            fieldHidden
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
            seqName == other.seqName &&
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
            readOnly == other.readOnly &&
            keyed == other.keyed &&
            unique == other.unique &&
            indexed == other.indexed &&
            private == other.private &&
            length == other.length &&
            precision == other.precision &&
            scale == other.scale &&
            minRange == other.minRange &&
            maxRange == other.maxRange &&
            colHidden == other.colHidden &&
            colWidth == other.colWidth &&
            fieldHidden == other.fieldHidden &&
            fieldWidth == other.fieldWidth
    }

    enum class Type {
        uuid, string, text, enum, sequence, email, password, int, long, float, double, decimal, date, time, datetime,
        timestamp, bool, array, json, media, location, relation
    }

    enum class RelType { oneToOne, oneToMany, manyToOne, manyToMany }
}