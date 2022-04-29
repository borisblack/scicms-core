package ru.scisolutions.scicmscore.engine.schema.seeder.liquibase

import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.domain.model.Attribute.Type
import ru.scisolutions.scicmscore.engine.schema.model.Item

class LiquibaseTypeResolver {
    fun getType(item: Item, attribute: Attribute): String {
        val tableName = item.metadata.tableName

        return when (attribute.type) {
            Type.UUID.value, Type.MEDIA.value -> "varchar(36)"
            Type.STRING.value -> {
                if (attribute.length == null || attribute.length <= 0)
                    throw IllegalArgumentException("Column [$tableName.${attribute.columnName}]: Invalid string length (${attribute.length})")

                "varchar(${attribute.length})"
            }
            Type.TEXT.value, Type.ARRAY.value, Type.JSON.value -> "text"
            Type.ENUM.value, Type.SEQUENCE.value, Type.EMAIL.value, Type.PASSWORD.value -> "varchar(50)"
            Type.INT.value -> {
                if (attribute.minRange != null && attribute.maxRange != null && attribute.minRange > attribute.maxRange)
                    throw IllegalArgumentException("Column [$tableName.${attribute.columnName}]: Invalid range ratio (minRange=${attribute.minRange} > maxRange=${attribute.maxRange})")

                "int"
            }
            Type.FLOAT.value -> {
                if (attribute.minRange != null && attribute.maxRange != null && attribute.minRange > attribute.maxRange)
                    throw IllegalArgumentException("Column [$tableName.${attribute.columnName}]: Invalid range ratio (minRange=${attribute.minRange} > maxRange=${attribute.maxRange})")

                "float"
            }
            Type.DECIMAL.value -> {
                if (attribute.precision == null || attribute.precision <= 0 || attribute.scale == null || attribute.scale < 0)
                    throw IllegalArgumentException("Column [$tableName.${attribute.columnName}]: Invalid precision and/or scale (${attribute.precision}, ${attribute.scale})")

                if (attribute.minRange != null && attribute.maxRange != null && attribute.minRange > attribute.maxRange)
                    throw IllegalArgumentException("Column [$tableName.${attribute.columnName}]: Invalid range ratio (minRange=${attribute.minRange} > maxRange=${attribute.maxRange})")

                "decimal(${attribute.precision},${attribute.scale})"
            }
            Type.DATE.value -> "date"
            Type.TIME.value -> "time"
            Type.DATETIME.value -> "datetime"
            Type.TIMESTAMP.value -> "timestamp"
            Type.BOOL.value -> "boolean"
            Type.RELATION.value -> {
                if (attribute.relType == Attribute.RelType.ONE_TO_MANY.value || attribute.relType == Attribute.RelType.MANY_TO_MANY.value)
                    throw IllegalArgumentException("Column [$tableName.${attribute.columnName}]: Invalid relation type (${attribute.relType})")

                "varchar(36)"
            }
            else -> throw IllegalArgumentException("Column [$tableName.${attribute.columnName}]: Invalid attribute type (${attribute.type})")
        }
    }
}