package ru.scisolutions.scicmscore.dbschema.seeder.impl.liquibase

import liquibase.change.AddColumnConfig
import liquibase.change.core.CreateIndexChange
import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.domain.model.Index
import ru.scisolutions.scicmscore.domain.model.Item

class LiquibaseIndexes(
    private val versioningIncludeInUniqueIndex: Boolean = true,
    private val i18nIncludeInUniqueIndex: Boolean = true,
) {
    fun list(item: Item): List<CreateIndexChange> {
        val list = mutableListOf<CreateIndexChange>()

        // Process attributes
        for ((attrName, attribute) in item.spec.attributes) {
            if (!attribute.keyed && (attribute.unique || attribute.indexed)) {
                list.addAll(listAttributeIndexes(item, attrName, attribute))
            }
        }

        // Process indexes
        for ((indexName, index) in item.spec.indexes) {
            list.add(index(item, indexName, index))
        }

        return list
    }

    private fun listAttributeIndexes(item: Item, attrName: String, attribute: Attribute): List<CreateIndexChange> {
        if (attribute.keyed)
            throw IllegalArgumentException("Keyed attribute [$attrName] is already indexed")

        if (!attribute.unique && !attribute.indexed)
            throw IllegalArgumentException("The attribute [$attrName] has no index")

        return if (attribute.unique) {
            val columnName = attribute.columnName
            if (columnName == GENERATION_COLUMN_NAME || columnName == MAJOR_REV_COLUMN_NAME || columnName == LOCALE_COLUMN_NAME)
                throw IllegalArgumentException("The column [$columnName] cannot be unique")

            val isVersioned = item.metadata.versioned && versioningIncludeInUniqueIndex
            val isLocalized = item.metadata.localized && i18nIncludeInUniqueIndex
            if (isVersioned) {
                if (isLocalized) {
                    listVersionedAndLocalizedAttributeIndexes(item, attribute)
                } else {
                    listVersionedAttributeIndexes(item, attribute)
                }
            } else {
                if (isLocalized) {
                    listOf(localizedIndex(item, attribute))
                } else {
                    listOf(attributeIndex(item, attribute))
                }
            }
        } else {
            listOf(attributeIndex(item, attribute))
        }
    }

    private fun listVersionedAndLocalizedAttributeIndexes(item: Item, attribute: Attribute): List<CreateIndexChange> {
        val tableName = item.metadata.tableName
        val suffix = if (attribute.unique) "uk" else "idx"

        return listOf(
            CreateIndexChange().apply {
                this.tableName = tableName
                this.indexName = "${tableName}_${attribute.columnName}_${GENERATION_COLUMN_NAME}_${LOCALE_COLUMN_NAME}_${suffix}"
                this.isUnique = attribute.unique
                this.columns = listOf(
                    AddColumnConfig().apply { this.name = attribute.columnName },
                    AddColumnConfig().apply { this.name = GENERATION_COLUMN_NAME },
                    AddColumnConfig().apply { this.name = LOCALE_COLUMN_NAME }
                )
            },
            CreateIndexChange().apply {
                this.tableName = tableName
                this.indexName = "${tableName}_${attribute.columnName}_${MAJOR_REV_COLUMN_NAME}_${LOCALE_COLUMN_NAME}_${suffix}"
                this.isUnique = attribute.unique
                this.columns = listOf(
                    AddColumnConfig().apply { this.name = attribute.columnName },
                    AddColumnConfig().apply { this.name = MAJOR_REV_COLUMN_NAME },
                    AddColumnConfig().apply { this.name = LOCALE_COLUMN_NAME }
                )
            }
        )
    }

    private fun listVersionedAttributeIndexes(item: Item, attribute: Attribute): List<CreateIndexChange> {
        val tableName = item.metadata.tableName
        val suffix = if (attribute.unique) "uk" else "idx"

        return listOf(
            CreateIndexChange().apply {
                this.tableName = tableName
                this.indexName = "${tableName}_${attribute.columnName}_${GENERATION_COLUMN_NAME}_${suffix}"
                this.isUnique = attribute.unique
                this.columns = listOf(
                    AddColumnConfig().apply { this.name = attribute.columnName },
                    AddColumnConfig().apply { this.name = GENERATION_COLUMN_NAME }
                )
            },
            CreateIndexChange().apply {
                this.tableName = tableName
                this.indexName = "${tableName}_${attribute.columnName}_${MAJOR_REV_COLUMN_NAME}_${suffix}"
                this.isUnique = attribute.unique
                this.columns = listOf(
                    AddColumnConfig().apply { this.name = attribute.columnName },
                    AddColumnConfig().apply { this.name = MAJOR_REV_COLUMN_NAME }
                )
            }
        )
    }

    private fun localizedIndex(item: Item, attribute: Attribute): CreateIndexChange {
        val tableName = item.metadata.tableName
        val suffix = if (attribute.unique) "uk" else "idx"

        return CreateIndexChange().apply {
            this.tableName = tableName
            this.indexName = "${tableName}_${attribute.columnName}_${LOCALE_COLUMN_NAME}_${suffix}"
            this.isUnique = true
            this.columns = mutableListOf(
                AddColumnConfig().apply { this.name = attribute.columnName },
                AddColumnConfig().apply { this.name = LOCALE_COLUMN_NAME }
            )
        }
    }

    private fun attributeIndex(item: Item, attribute: Attribute): CreateIndexChange {
        val tableName = item.metadata.tableName
        val suffix = if (attribute.unique) "uk" else "idx"

        return CreateIndexChange().apply {
            this.tableName = tableName
            this.indexName = "${tableName}_${attribute.columnName}_${suffix}"
            this.isUnique = true
            this.columns = mutableListOf(
                AddColumnConfig().apply { this.name = attribute.columnName }
            )
        }
    }

    private fun index(item: Item, indexName: String, index: Index): CreateIndexChange {
        val tableName = item.metadata.tableName

        return CreateIndexChange().apply {
            this.tableName = tableName
            this.indexName = if (indexName.startsWith("_")) "${tableName}${indexName}" else indexName
            this.isUnique = index.unique
            this.columns = index.columns.map {
                AddColumnConfig().apply { this.name = it }
            }
        }
    }

    companion object {
        private const val GENERATION_COLUMN_NAME = "generation"
        private const val MAJOR_REV_COLUMN_NAME = "major_rev"
        private const val LOCALE_COLUMN_NAME = "locale"
    }
}