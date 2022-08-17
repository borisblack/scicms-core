package ru.scisolutions.scicmscore.schema.seeder.liquibase

import liquibase.change.AddColumnConfig
import liquibase.change.ColumnConfig
import liquibase.change.ConstraintsConfig
import ru.scisolutions.scicmscore.model.Attribute
import ru.scisolutions.scicmscore.schema.model.Item

class LiquibaseColumns {
    fun list(item: Item): List<ColumnConfig> =
        item.spec.attributes.asSequence()
            .filter { (_, attribute) -> !attribute.isCollection() }
            .map { (attrName, attribute) -> getColumn(item, attrName, attribute) }
            .toList()

    fun getColumn(item: Item, attrName: String, attribute: Attribute) = ColumnConfig().apply {
        applyColumnConfig(this, item, attrName, attribute)
    }

    fun getAddColumn(item: Item, attrName: String, attribute: Attribute) = AddColumnConfig().apply {
        applyColumnConfig(this, item, attrName, attribute)
    }

    private fun applyColumnConfig(columnConfig: ColumnConfig, item: Item, attrName: String, attribute: Attribute) {
        columnConfig.name = attribute.columnName ?: attrName.lowercase()
        columnConfig.type = typeResolver.getType(item, attrName, attribute)

        if (attribute.defaultValue != null)
            columnConfig.defaultValue = attribute.defaultValue

        columnConfig.constraints = ConstraintsConfig().apply {
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