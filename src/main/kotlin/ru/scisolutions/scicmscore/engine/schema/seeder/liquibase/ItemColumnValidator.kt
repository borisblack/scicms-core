package ru.scisolutions.scicmscore.engine.schema.seeder.liquibase

import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.domain.model.Attribute.Type
import ru.scisolutions.scicmscore.engine.schema.model.Item

class ItemColumnValidator {
    fun validate(item: Item, attrName: String, attribute: Attribute) {
        val tableName = item.metadata.tableName
        val columnName = attribute.columnName ?: attrName.lowercase()

        if (attribute.type == Type.relation && attribute.isCollection())
            throw IllegalArgumentException("Column [${tableName}.${columnName}] has invalid relation type (${attribute.relType})")
    }
}