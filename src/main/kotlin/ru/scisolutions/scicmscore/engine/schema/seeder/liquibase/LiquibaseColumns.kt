package ru.scisolutions.scicmscore.engine.schema.seeder.liquibase

import liquibase.change.ColumnConfig
import liquibase.change.ConstraintsConfig
import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.domain.model.Attribute.RelType
import ru.scisolutions.scicmscore.domain.model.Attribute.Type
import ru.scisolutions.scicmscore.engine.schema.model.Item

class LiquibaseColumns {
    fun list(item: Item): List<ColumnConfig> {
        val list = mutableListOf<ColumnConfig>()

        // Process attributes
        item.spec.attributes.asSequence()
            .filter { (_, attribute) ->
                attribute.type != Type.relation || (attribute.relType != RelType.oneToMany && attribute.relType != RelType.manyToMany)
            }
            .forEach { (attrName, attribute) ->
                list.add(getColumn(item, attrName, attribute))
            }

        return list
    }

    private fun getColumn(item: Item, attrName: String, attribute: Attribute) =
        ColumnConfig().apply {
            this.name = attribute.columnName ?: attrName.lowercase()
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