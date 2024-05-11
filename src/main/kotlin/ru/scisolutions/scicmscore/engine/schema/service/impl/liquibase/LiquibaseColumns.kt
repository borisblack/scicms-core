package ru.scisolutions.scicmscore.engine.schema.service.impl.liquibase

import liquibase.change.AddColumnConfig
import liquibase.change.ColumnConfig
import liquibase.change.ConstraintsConfig
import liquibase.change.core.AddColumnChange
import liquibase.change.core.AddDefaultValueChange
import liquibase.change.core.AddNotNullConstraintChange
import liquibase.change.core.AddPrimaryKeyChange
import liquibase.change.core.DropColumnChange
import liquibase.change.core.DropDefaultValueChange
import liquibase.change.core.DropNotNullConstraintChange
import liquibase.change.core.DropPrimaryKeyChange
import liquibase.change.core.ModifyDataTypeChange
import liquibase.change.core.RenameColumnChange
import ru.scisolutions.scicmscore.engine.schema.model.Item

class LiquibaseColumns {
    fun list(item: Item): List<ColumnConfig> =
        item.spec.attributes.asSequence()
            .filter { (_, attribute) -> !attribute.isCollection() }
            .map { (attrName, _) -> columnConfig(item, attrName) }
            .toList()

    fun columnConfig(item: Item, attrName: String) = ColumnConfig().apply {
        applyColumnConfig(this, item, attrName)
    }

    private fun applyColumnConfig(columnConfig: ColumnConfig, item: Item, attrName: String) {
        val tableName = requireNotNull(item.metadata.tableName)
        val attribute = item.spec.getAttribute(attrName)
        val columnName = attribute.getColumnName(attrName)
        columnConfig.name = columnName
        columnConfig.type = typeResolver.getType(item, attrName, attribute)

        if (attribute.defaultValue != null)
            columnConfig.defaultValue = attribute.defaultValue

        columnConfig.constraints = ConstraintsConfig().apply {
            this.isNullable = !attribute.required

            if (attribute.keyed) {
                this.isPrimaryKey = true
                this.primaryKeyName = primaryKeyConstraintName(tableName, columnName)
            }
        }
    }

    private fun primaryKeyConstraintName(tableName: String, columnName: String) =
        "${tableName}_${columnName}_pk"

    fun addColumnChange(item: Item, attrName: String): AddColumnChange {
        val metadata = item.metadata

        return AddColumnChange().apply {
            this.tableName = metadata.tableName
            this.columns = listOf(addColumnConfig(item, attrName))
        }
    }

    fun addColumnConfig(item: Item, attrName: String) = AddColumnConfig().apply {
        applyColumnConfig(this, item, attrName)
    }

    fun dropColumnChange(tableName: String, columnName: String): DropColumnChange =
        DropColumnChange().apply {
            this.tableName = tableName
            this.columnName = columnName
        }

    fun renameColumnChange(tableName: String, existingColumnName: String, newColumnName: String): RenameColumnChange =
        RenameColumnChange().apply {
            this.tableName = tableName
            this.oldColumnName = existingColumnName
            this.newColumnName = newColumnName
        }

    fun modifyDataTypeChange(item: Item, attrName: String): ModifyDataTypeChange {
        val metadata = item.metadata
        val attribute = item.spec.getAttribute(attrName)
        return ModifyDataTypeChange().apply {
            this.tableName = requireNotNull(metadata.tableName)
            this.columnName = attribute.getColumnName(attrName)
            this.newDataType = typeResolver.getType(item, attrName, attribute)
        }
    }

    fun dropDefaultValueChange(tableName: String, columnName: String): DropDefaultValueChange =
        DropDefaultValueChange().apply {
            this.tableName = tableName
            this.columnName = columnName
        }

    fun addDefaultValueChange(tableName: String, columnName: String, defaultValue: String): AddDefaultValueChange =
        AddDefaultValueChange().apply {
            this.tableName = tableName
            this.columnName = columnName
            this.defaultValue = defaultValue
        }

    fun dropNotNullConstraintChange(tableName: String, columnName: String): DropNotNullConstraintChange =
        DropNotNullConstraintChange().apply {
            this.tableName = tableName
            this.columnName = columnName
        }

    fun addNotNullConstraintChange(tableName: String, columnName: String): AddNotNullConstraintChange =
        AddNotNullConstraintChange().apply {
            this.tableName = tableName
            this.columnName = columnName
        }

    fun dropPrimaryKeyChange(tableName: String, columnName: String): DropPrimaryKeyChange =
        DropPrimaryKeyChange().apply {
            this.tableName = tableName
            this.constraintName = primaryKeyConstraintName(tableName, columnName)
        }

    fun addPrimaryKeyChange(tableName: String, columnName: String): AddPrimaryKeyChange =
        AddPrimaryKeyChange().apply {
            this.tableName = tableName
            this.columnNames = columnName
            this.constraintName = primaryKeyConstraintName(tableName, columnName)
        }

    companion object {
        private val typeResolver = LiquibaseTypeResolver()
    }
}