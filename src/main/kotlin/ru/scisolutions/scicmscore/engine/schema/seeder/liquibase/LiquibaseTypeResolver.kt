package ru.scisolutions.scicmscore.engine.schema.seeder.liquibase

import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.engine.schema.model.Item
import ru.scisolutions.scicmscore.domain.model.Attribute.RelType as AttrRelType
import ru.scisolutions.scicmscore.domain.model.Attribute.Type as AttrType

class LiquibaseTypeResolver {
    fun getType(item: Item, attribute: Attribute): String {
        val tableName = item.metadata.tableName

        return when (val attrType = AttrType.valueOf(attribute.type)) {
            AttrType.uuid, AttrType.media -> "varchar(36)"
            AttrType.string -> {
                if (attribute.length == null || attribute.length <= 0)
                    throw IllegalArgumentException("Column [$tableName.${attribute.columnName}]: Invalid string length (${attribute.length})")

                "varchar(${attribute.length})"
            }
            AttrType.text, AttrType.array, AttrType.json -> "text"
            AttrType.enum, AttrType.sequence, AttrType.email, AttrType.password -> "varchar(50)"
            AttrType.int -> {
                if (attribute.minRange != null && attribute.maxRange != null && attribute.minRange > attribute.maxRange)
                    throw IllegalArgumentException("Column [$tableName.${attribute.columnName}]: Invalid range ratio (minRange=${attribute.minRange} > maxRange=${attribute.maxRange})")

                "int"
            }
            AttrType.float -> {
                if (attribute.minRange != null && attribute.maxRange != null && attribute.minRange > attribute.maxRange)
                    throw IllegalArgumentException("Column [$tableName.${attribute.columnName}]: Invalid range ratio (minRange=${attribute.minRange} > maxRange=${attribute.maxRange})")

                "float"
            }
            AttrType.decimal -> {
                if (attribute.precision == null || attribute.precision <= 0 || attribute.scale == null || attribute.scale < 0)
                    throw IllegalArgumentException("Column [$tableName.${attribute.columnName}]: Invalid precision and/or scale (${attribute.precision}, ${attribute.scale})")

                if (attribute.minRange != null && attribute.maxRange != null && attribute.minRange > attribute.maxRange)
                    throw IllegalArgumentException("Column [$tableName.${attribute.columnName}]: Invalid range ratio (minRange=${attribute.minRange} > maxRange=${attribute.maxRange})")

                "decimal(${attribute.precision},${attribute.scale})"
            }
            AttrType.date -> "date"
            AttrType.time -> "time"
            AttrType.datetime -> "datetime"
            AttrType.timestamp -> "timestamp"
            AttrType.bool -> "boolean"
            AttrType.relation -> {
                val attrRelType = AttrRelType.nullableValueOf(attribute.relType)
                if (attrRelType == AttrRelType.oneToMany || attrRelType == AttrRelType.manyToMany)
                    throw IllegalArgumentException("Column [$tableName.${attribute.columnName}] has invalid relation type ($attrRelType)")

                "varchar(36)"
            }
            else -> throw IllegalArgumentException("Column [$tableName.${attribute.columnName}] has unsupported attribute type ($attrType)")
        }
    }
}