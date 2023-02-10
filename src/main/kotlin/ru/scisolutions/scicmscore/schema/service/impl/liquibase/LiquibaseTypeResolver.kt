package ru.scisolutions.scicmscore.schema.service.impl.liquibase

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
import ru.scisolutions.scicmscore.model.Attribute
import ru.scisolutions.scicmscore.model.FieldType
import ru.scisolutions.scicmscore.schema.model.Item

class LiquibaseTypeResolver {
    fun getType(item: Item, attrName: String, attribute: Attribute): String {
        itemColumnValidator.validate(item, attrName, attribute)

        return when (attribute.type) {
            FieldType.uuid, FieldType.media, FieldType.relation -> VarcharType().apply { addParameter(UUID_STRING_LENGTH) }.toString()
            FieldType.string -> VarcharType().apply { addParameter(attribute.length) }.toString()
            FieldType.text, FieldType.array, FieldType.json -> ClobType().toString()
            FieldType.enum, FieldType.sequence, FieldType.email, FieldType.password -> VarcharType().apply { addParameter(DEFAULT_STRING_LENGTH) }.toString()
            FieldType.int -> IntType().toString()
            FieldType.long -> BigIntType().toString()
            FieldType.float -> FloatType().toString()
            FieldType.double -> DoubleType().toString()
            FieldType.decimal -> {
                DecimalType().apply {
                    addParameter(attribute.precision)
                    addParameter(attribute.scale)
                }.toString()
            }
            FieldType.date -> DateType().toString()
            FieldType.time -> TimeType().toString()
            FieldType.datetime -> DateTimeType().toString()
            FieldType.timestamp -> TimestampType().toString()
            FieldType.bool -> TinyIntType().toString()
        }
    }

    companion object {
        private const val DEFAULT_STRING_LENGTH = 50
        private const val UUID_STRING_LENGTH = 36

        private val itemColumnValidator = ItemColumnValidator()
    }
}