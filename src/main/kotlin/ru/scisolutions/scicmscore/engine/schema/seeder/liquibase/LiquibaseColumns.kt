package ru.scisolutions.scicmscore.engine.schema.seeder.liquibase

import liquibase.change.ColumnConfig
import liquibase.change.ConstraintsConfig
import ru.scisolutions.scicmscore.engine.schema.model.Attribute
import ru.scisolutions.scicmscore.engine.schema.model.Item

class LiquibaseColumns {
    fun list(item: Item): List<ColumnConfig> {
        val list = mutableListOf<ColumnConfig>()

        // Process attributes
        for ((_, attribute) in item.spec.attributes) {
            // Skip list relation types
            if (attribute.type == Attribute.Type.RELATION.value &&
                (attribute.relType == Attribute.RelType.ONE_TO_MANY.value || attribute.relType == Attribute.RelType.MANY_TO_MANY.value))
                break

            list.add(getColumn(item, attribute))
        }

        return list
    }

    private fun getColumn(item: Item, attribute: Attribute) =
        ColumnConfig().apply {
            this.name = attribute.columnName
            this.type = typeResolver.getType(item, attribute)

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