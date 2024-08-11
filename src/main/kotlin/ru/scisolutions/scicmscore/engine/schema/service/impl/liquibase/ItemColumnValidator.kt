package ru.scisolutions.scicmscore.engine.schema.service.impl.liquibase

import ru.scisolutions.scicmscore.engine.model.Attribute
import ru.scisolutions.scicmscore.engine.model.FieldType
import ru.scisolutions.scicmscore.engine.schema.model.Item

class ItemColumnValidator {
    fun validate(item: Item, attrName: String, attribute: Attribute) {
        val tableName = requireNotNull(item.metadata.tableName)
        val columnName = attribute.columnName ?: attrName.lowercase()

        if (attribute.type == FieldType.relation && attribute.isCollection()) {
            throw IllegalArgumentException("Column [$tableName.$columnName] has invalid relation type (${attribute.relType})")
        }
    }
}
