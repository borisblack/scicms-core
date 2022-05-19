package ru.scisolutions.scicmscore.engine.schema.seeder.liquibase

import liquibase.change.ColumnConfig
import liquibase.change.ConstraintsConfig
import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.engine.schema.model.Item

class LiquibaseColumns {
    fun list(item: Item): List<ColumnConfig> =
        item.spec.attributes.asSequence()
            .filter { (_, attribute) -> !attribute.isCollection() }
            .map { (attrName, attribute) -> getColumn(item, attrName, attribute) }
            .toList()

    private fun getColumn(item: Item, attrName: String, attribute: Attribute) = ColumnConfig().apply {
        this.name = attribute.columnName ?: attrName.lowercase()
        this.type = typeResolver.getType(item, attrName, attribute)

        if (attribute.defaultValue != null)
            this.defaultValue = attribute.defaultValue

        this.constraints = ConstraintsConfig().apply {
            this.isNullable = !attribute.required

            if (attribute.keyed) {
                this.isPrimaryKey = true
                this.primaryKeyName = "${item.metadata.tableName}_${attribute.columnName}_pk"
            }
        }
    }

    companion object {
        private val typeResolver = LiquibaseTypeResolver()
    }
}