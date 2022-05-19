package ru.scisolutions.scicmscore.engine.schema.seeder.liquibase

import liquibase.datatype.core.BigIntType
import liquibase.datatype.core.ClobType
import liquibase.datatype.core.DateTimeType
import liquibase.datatype.core.DateType
import liquibase.datatype.core.DecimalType
import liquibase.datatype.core.DoubleType
import liquibase.datatype.core.FloatType
import liquibase.datatype.core.IntType
import liquibase.datatype.core.TimeType
import liquibase.datatype.core.TimestampType
import liquibase.datatype.core.TinyIntType
import liquibase.datatype.core.VarcharType
import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.domain.model.Attribute.Type
import ru.scisolutions.scicmscore.engine.schema.model.Item

class LiquibaseTypeResolver {
    fun getType(item: Item, attrName: String, attribute: Attribute): String {
        val tableName = item.metadata.tableName
        val columnName = attribute.columnName ?: attrName.lowercase()
        itemColumnValidator.validate(item, attrName, attribute)

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
                }.toString()
            }
            Type.date -> DateType().toString()
            Type.time -> TimeType().toString()
            Type.datetime -> DateTimeType().toString()
            Type.timestamp -> TimestampType().toString()
            Type.bool -> TinyIntType().toString()
            else -> throw IllegalArgumentException("Column [${tableName}.${columnName}] has unsupported attribute type (${attribute.type})")
        }
    }

    companion object {
        private const val DEFAULT_STRING_LENGTH = 50
        private const val UUID_STRING_LENGTH = 36

        private val itemColumnValidator = ItemColumnValidator()
    }
}