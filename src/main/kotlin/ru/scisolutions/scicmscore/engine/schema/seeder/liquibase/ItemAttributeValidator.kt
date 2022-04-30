package ru.scisolutions.scicmscore.engine.schema.seeder.liquibase

import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.engine.schema.model.Item

class ItemAttributeValidator {
    fun validate(item: Item, attribute: Attribute) {
        val tableName = item.metadata.tableName

        when (attribute.type) {
            Attribute.Type.string -> {
                if (attribute.length == null || attribute.length <= 0)
                    throw IllegalArgumentException("Column [$tableName.${attribute.columnName}] has invalid string length (${attribute.length})")
            }
            Attribute.Type.int, Attribute.Type.long, Attribute.Type.float, Attribute.Type.double -> {
                if (attribute.minRange != null && attribute.maxRange != null && attribute.minRange > attribute.maxRange)
                    throw IllegalArgumentException("Column [$tableName.${attribute.columnName}] has invalid range ratio (minRange=${attribute.minRange} > maxRange=${attribute.maxRange})")
            }
            Attribute.Type.decimal -> {
                if (attribute.precision == null || attribute.precision <= 0 || attribute.scale == null || attribute.scale < 0)
                    throw IllegalArgumentException("Column [$tableName.${attribute.columnName}]: Invalid precision and/or scale (${attribute.precision}, ${attribute.scale})")

                if (attribute.minRange != null && attribute.maxRange != null && attribute.minRange > attribute.maxRange)
                    throw IllegalArgumentException("Column [$tableName.${attribute.columnName}]: Invalid range ratio (minRange=${attribute.minRange} > maxRange=${attribute.maxRange})")
            }
            Attribute.Type.relation -> {
                if (attribute.relType == Attribute.RelType.oneToMany || attribute.relType == Attribute.RelType.manyToMany)
                    throw IllegalArgumentException("Column [$tableName.${attribute.columnName}] has invalid relation type (${attribute.relType})")
            }
            else -> {}
        }
    }
}