package ru.scisolutions.scicmscore.engine.schema.seeder.liquibase

import liquibase.datatype.core.BigIntType
import liquibase.datatype.core.BooleanType
import liquibase.datatype.core.ClobType
import liquibase.datatype.core.DateTimeType
import liquibase.datatype.core.DateType
import liquibase.datatype.core.DecimalType
import liquibase.datatype.core.DoubleType
import liquibase.datatype.core.FloatType
import liquibase.datatype.core.IntType
import liquibase.datatype.core.TimeType
import liquibase.datatype.core.TimestampType
import liquibase.datatype.core.VarcharType
import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.domain.model.Attribute.RelType
import ru.scisolutions.scicmscore.domain.model.Attribute.Type
import ru.scisolutions.scicmscore.engine.schema.model.Item

class LiquibaseTypeResolver {
    fun getType(item: Item, attribute: Attribute): String {
        val tableName = item.metadata.tableName
        validateAttribute(item, attribute)

        return when (attribute.type) {
            Type.uuid, Type.media -> VarcharType().apply { addParameter(UUID_STRING_LENGTH) }.toString()
            Type.string -> VarcharType().apply { addParameter(attribute.length) }.toString()
            Type.text, Type.array, Type.json -> ClobType().toString()
            Type.enum, Type.sequence, Type.email, Type.password, Type.relation -> VarcharType().apply { addParameter(DEFAULT_STRING_LENGTH) }.toString()
            Type.int -> IntType().toString()
            Type.long -> BigIntType().toString()
            Type.float -> FloatType().toString()
            Type.double -> DoubleType().toString()
            Type.decimal -> {
                DecimalType().apply {
                    addParameter(attribute.precision)
                    addParameter(attribute.scale)
                }
                    .toString()
            }
            Type.date -> DateType().toString()
            Type.time -> TimeType().toString()
            Type.datetime -> DateTimeType().toString()
            Type.timestamp -> TimestampType().toString()
            Type.bool -> BooleanType().toString()
            else -> throw IllegalArgumentException("Column [$tableName.${attribute.columnName}] has unsupported attribute type (${attribute.type})")
        }
    }

    private fun validateAttribute(item: Item, attribute: Attribute) {
        val tableName = item.metadata.tableName

        when (attribute.type) {
            Type.string -> {
                if (attribute.length == null || attribute.length <= 0)
                    throw IllegalArgumentException("Column [$tableName.${attribute.columnName}] has invalid string length (${attribute.length})")
            }
            Type.int, Type.long, Type.float, Type.double -> {
                if (attribute.minRange != null && attribute.maxRange != null && attribute.minRange > attribute.maxRange)
                    throw IllegalArgumentException("Column [$tableName.${attribute.columnName}] has invalid range ratio (minRange=${attribute.minRange} > maxRange=${attribute.maxRange})")
            }
            Type.decimal -> {
                if (attribute.precision == null || attribute.precision <= 0 || attribute.scale == null || attribute.scale < 0)
                    throw IllegalArgumentException("Column [$tableName.${attribute.columnName}]: Invalid precision and/or scale (${attribute.precision}, ${attribute.scale})")

                if (attribute.minRange != null && attribute.maxRange != null && attribute.minRange > attribute.maxRange)
                    throw IllegalArgumentException("Column [$tableName.${attribute.columnName}]: Invalid range ratio (minRange=${attribute.minRange} > maxRange=${attribute.maxRange})")
            }
            Type.relation -> {
                if (attribute.relType == RelType.oneToMany || attribute.relType == RelType.manyToMany)
                    throw IllegalArgumentException("Column [$tableName.${attribute.columnName}] has invalid relation type (${attribute.relType})")
            }
            else -> {}
        }
    }

    companion object {
        private const val DEFAULT_STRING_LENGTH = 50
        private const val UUID_STRING_LENGTH = 36
    }
}