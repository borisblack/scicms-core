package ru.scisolutions.scicmscore.schema.service.impl.liquibase

import ru.scisolutions.scicmscore.model.Attribute
import ru.scisolutions.scicmscore.model.Attribute.Type
import ru.scisolutions.scicmscore.schema.model.Item

class ItemColumnValidator {
    fun validate(item: Item, attrName: String, attribute: Attribute) {
        val tableName = item.metadata.tableName
        val columnName = attribute.columnName ?: attrName.lowercase()

        if (attribute.type == Type.relation && attribute.isCollection())
            throw IllegalArgumentException("Column [${tableName}.${columnName}] has invalid relation type (${attribute.relType})")
    }
}