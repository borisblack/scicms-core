package ru.scisolutions.scicmscore.engine.schema.seeder.liquibase

import liquibase.change.AddColumnConfig
import liquibase.change.core.CreateIndexChange
import ru.scisolutions.scicmscore.domain.model.Attribute
import ru.scisolutions.scicmscore.domain.model.Index
import ru.scisolutions.scicmscore.engine.schema.model.Item

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

        val attributeIndexes = mutableListOf<CreateIndexChange>()
        if (attribute.unique) {
            val columnName = attribute.columnName
            if (columnName == GENERATION_COLUMN_NAME || columnName == MAJOR_REV_COLUMN_NAME || columnName == LOCALE_COLUMN_NAME)
                throw IllegalArgumentException("The column [$columnName] cannot be unique")

            val isVersioned = item.metadata.versioned && versioningIncludeInUniqueIndex
            val isLocalized = item.metadata.localized && i18nIncludeInUniqueIndex
            if (isVersioned) {
                if (isLocalized) {
                    attributeIndexes.addAll(
                        listVersionedAndLocalizedUniqueIndexes(item, attribute)
                    )
                } else {
                    attributeIndexes.addAll(
                        listVersionedUniqueIndexes(item, attribute)
                    )
                }
            } else {
                if (isLocalized) {
                    attributeIndexes.add(localizedUniqueIndex(item, attribute))
                } else {
                    return listOf(attributeUniqueIndex(item, attribute))
                }
            }
        }

        // Add non-unique indexes
        attributeIndexes.add(attributeIndex(item, attribute))

        return attributeIndexes
    }

    private fun listVersionedAndLocalizedUniqueIndexes(item: Item, attribute: Attribute): List<CreateIndexChange> {
        if (!attribute.unique)
            throw IllegalArgumentException("Only the unique index needs to be versioned and localized")

        val tableName = item.metadata.tableName

        return listOf(
            CreateIndexChange().apply {
                this.tableName = tableName
                this.indexName = "${tableName}_${attribute.columnName}_${GENERATION_COLUMN_NAME}_${LOCALE_COLUMN_NAME}_uk"
                this.isUnique = true
                this.columns = listOf(
                    AddColumnConfig().apply { this.name = attribute.columnName },
                    AddColumnConfig().apply { this.name = GENERATION_COLUMN_NAME },
                    AddColumnConfig().apply { this.name = LOCALE_COLUMN_NAME }
                )
            },
            CreateIndexChange().apply {
                this.tableName = tableName
                this.indexName = "${tableName}_${attribute.columnName}_${MAJOR_REV_COLUMN_NAME}_${LOCALE_COLUMN_NAME}_uk"
                this.isUnique = true
                this.columns = listOf(
                    AddColumnConfig().apply { this.name = attribute.columnName },
                    AddColumnConfig().apply { this.name = MAJOR_REV_COLUMN_NAME },
                    AddColumnConfig().apply { this.name = LOCALE_COLUMN_NAME }
                )
            }
        )
    }

    private fun listVersionedUniqueIndexes(item: Item, attribute: Attribute): List<CreateIndexChange> {
        if (!attribute.unique)
            throw IllegalArgumentException("Only the unique index needs to be versioned")

        val tableName = item.metadata.tableName

        return listOf(
            CreateIndexChange().apply {
                this.tableName = tableName
                this.indexName = "${tableName}_${attribute.columnName}_${GENERATION_COLUMN_NAME}_uk"
                this.isUnique = true
                this.columns = listOf(
                    AddColumnConfig().apply { this.name = attribute.columnName },
                    AddColumnConfig().apply { this.name = GENERATION_COLUMN_NAME }
                )
            },
            CreateIndexChange().apply {
                this.tableName = tableName
                this.indexName = "${tableName}_${attribute.columnName}_${MAJOR_REV_COLUMN_NAME}_uk"
                this.isUnique = true
                this.columns = listOf(
                    AddColumnConfig().apply { this.name = attribute.columnName },
                    AddColumnConfig().apply { this.name = MAJOR_REV_COLUMN_NAME }
                )
            }
        )
    }

    private fun localizedUniqueIndex(item: Item, attribute: Attribute): CreateIndexChange {
        if (!attribute.unique)
            throw IllegalArgumentException("Only the unique index needs to be localized")

        val tableName = item.metadata.tableName

        return CreateIndexChange().apply {
            this.tableName = tableName
            this.indexName = "${tableName}_${attribute.columnName}_${LOCALE_COLUMN_NAME}_uk"
            this.isUnique = true
            this.columns = mutableListOf(
                AddColumnConfig().apply { this.name = attribute.columnName },
                AddColumnConfig().apply { this.name = LOCALE_COLUMN_NAME }
            )
        }
    }

    private fun attributeUniqueIndex(item: Item, attribute: Attribute): CreateIndexChange {
        val tableName = item.metadata.tableName

        return CreateIndexChange().apply {
            this.tableName = tableName
            this.indexName = "${tableName}_${attribute.columnName}_uk"
            this.isUnique = true
            this.columns = mutableListOf(
                AddColumnConfig().apply { this.name = attribute.columnName }
            )
        }
    }

    private fun attributeIndex(item: Item, attribute: Attribute): CreateIndexChange {
        val tableName = item.metadata.tableName

        return CreateIndexChange().apply {
            this.tableName = tableName
            this.indexName = "${tableName}_${attribute.columnName}_idx"
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