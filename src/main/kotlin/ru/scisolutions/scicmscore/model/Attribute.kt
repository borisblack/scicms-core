package ru.scisolutions.scicmscore.model

import com.fasterxml.jackson.annotation.JsonIgnore
import ru.scisolutions.scicmscore.util.Json
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.util.Objects

class Attribute(
    val type: FieldType,
    val columnName: String? = null, // optional (lowercase attribute name is used in database by default), also can be null for oneToMany and manyToMany relations
    val displayName: String,
    val description: String? = null,
    val enumSet: Set<String>? = null,
    val seqName: String? = null,
    val confirm: Boolean? = null,
    val encode: Boolean? = null,
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
    fun isRelation() = type == FieldType.relation

    @JsonIgnore
    fun isCollection() = isRelation() && (relType == RelType.oneToMany || relType == RelType.manyToMany)

    fun validate() {
        when (type) {
            FieldType.string -> {
                if (length == null || length <= 0)
                    throw IllegalArgumentException("Invalid string length (${length})")
            }
            FieldType.enum -> {
                if (enumSet == null)
                    throw IllegalArgumentException("The enumSet is required for the enum type")
            }
            FieldType.sequence -> {
                if (seqName == null)
                    throw IllegalArgumentException("The seqName is required for the sequence type")
            }
            FieldType.int, FieldType.long, FieldType.float, FieldType.double -> {
                if (minRange != null && maxRange != null && minRange > maxRange)
                    throw IllegalArgumentException("Invalid range ratio (minRange=${minRange} > maxRange=${maxRange})")
            }
            FieldType.decimal -> {
                if ((precision != null && precision <= 0) || (scale != null && scale < 0))
                    throw IllegalArgumentException("Invalid precision and/or scale (${precision}, ${scale})")

                if (minRange != null && maxRange != null && minRange > maxRange)
                    throw IllegalArgumentException("Invalid range ratio (minRange=${minRange} > maxRange=${maxRange})")
            }
            FieldType.relation -> {
                if (isCollection() && required)
                    throw IllegalArgumentException("Collection relation attribute cannot be required")
            }
            else -> {}
        }
    }

    fun parseDefaultValue(): Any? =
        if (defaultValue == null) null
        else when (type) {
            FieldType.uuid, FieldType.string, FieldType.text, FieldType.enum, FieldType.email, FieldType.sequence, FieldType.password -> defaultValue
            FieldType.int -> defaultValue.toInt()
            FieldType.long -> defaultValue.toLong()
            FieldType.float -> defaultValue.toFloat()
            FieldType.double -> defaultValue.toDouble()
            FieldType.decimal -> defaultValue.toBigDecimal()
            FieldType.date -> LocalDate.parse(defaultValue)
            FieldType.time -> OffsetTime.parse(defaultValue)
            FieldType.datetime, FieldType.timestamp -> OffsetDateTime.parse(defaultValue)
            FieldType.bool -> defaultValue == "1" || defaultValue == "true"
            FieldType.array -> Json.objectMapper.readValue(defaultValue, List::class.java)
            FieldType.json -> Json.objectMapper.readValue(defaultValue, Map::class.java)
            FieldType.media -> defaultValue
            FieldType.relation -> if (isCollection()) Json.objectMapper.readValue(defaultValue, List::class.java).toSet() else defaultValue
        }

    fun getColumnName(fallbackColumnName: String): String =
        columnName ?: fallbackColumnName.lowercase()

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
            confirm == other.confirm &&
            encode == other.encode &&
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

    override fun hashCode(): Int =
        Objects.hash(
            type.name,
            columnName,
            enumSet,
            seqName,
            confirm,
            encode,
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

    enum class RelType { oneToOne, oneToMany, manyToOne, manyToMany }
}