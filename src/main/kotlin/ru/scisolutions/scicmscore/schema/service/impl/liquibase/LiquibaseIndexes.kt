package ru.scisolutions.scicmscore.schema.service.impl.liquibase

import liquibase.change.AddColumnConfig
import liquibase.change.core.CreateIndexChange
import org.slf4j.LoggerFactory
import ru.scisolutions.scicmscore.model.Index
import ru.scisolutions.scicmscore.schema.model.Item

class LiquibaseIndexes(
    private val versioningIncludeInUniqueIndex: Boolean = true,
    private val i18nIncludeInUniqueIndex: Boolean = true,
) {
    fun createIndexes(item: Item): List<CreateIndexChange> {
        val list = mutableListOf<CreateIndexChange>()

        // Process attributes
        for ((attrName, attribute) in item.spec.attributes) {
            if (!attribute.keyed && (attribute.unique || attribute.indexed)) {
                list.addAll(createAttributeIndexes(item, attrName))
            }
        }

        // Process indexes
        for ((indexName, index) in item.spec.indexes) {
            list.add(indexFromIndex(item, indexName, index))
        }

        return list
    }

    fun createAttributeIndexes(item: Item, attrName: String): List<CreateIndexChange> {
        val attribute = item.spec.getAttribute(attrName)
        if (attribute.keyed) {
            // throw IllegalArgumentException("Keyed attribute [$attrName] is already indexed")
            logger.debug("Keyed attribute [{}] is already indexed", attrName)
            return emptyList()
        }

        if (!attribute.unique && !attribute.indexed) {
            // throw IllegalArgumentException("The attribute [$attrName] has no index")
            logger.debug("The attribute [{}] has no index", attrName)
            return emptyList()
        }

        val attributeIndexes = mutableListOf<CreateIndexChange>()
        if (attribute.unique) {
            val columnName = attribute.columnName
            if (columnName == GENERATION_COLUMN_NAME || columnName == MAJOR_REV_COLUMN_NAME || columnName == LOCALE_COLUMN_NAME)
                throw IllegalArgumentException("The column [$columnName] cannot be unique.")

            val isVersioned = item.metadata.versioned && versioningIncludeInUniqueIndex
            val isLocalized = item.metadata.localized && i18nIncludeInUniqueIndex
            if (isVersioned) {
                if (isLocalized) {
                    attributeIndexes.addAll(
                        createVersionedAndLocalizedUniqueIndexes(item, attrName)
                    )
                } else {
                    attributeIndexes.addAll(
                        createVersionedUniqueIndexes(item, attrName)
                    )
                }
            } else {
                if (isLocalized) {
                    attributeIndexes.add(localizedUniqueIndexFromAttribute(item, attrName))
                } else {
                    return listOf(uniqueIndexFromAttribute(item, attrName))
                }
            }
        }

        // Add non-unique index
        attributeIndexes.add(indexFromAttribute(item, attrName))

        return attributeIndexes
    }

    private fun createVersionedAndLocalizedUniqueIndexes(item: Item, attrName: String): List<CreateIndexChange> {
        val attribute = item.spec.getAttribute(attrName)
        val columnName = attribute.getColumnName(attrName)
        if (!attribute.unique)
            throw IllegalArgumentException("Only the unique index needs to be versioned and localized")

        val tableName = item.metadata.tableName

        return listOf(
            CreateIndexChange().apply {
                this.tableName = tableName
                this.indexName = "${tableName}_${columnName}_${GENERATION_COLUMN_NAME}_${LOCALE_COLUMN_NAME}_uk"
                this.isUnique = true
                this.columns = listOf(
                    AddColumnConfig().apply { this.name = attribute.columnName },
                    AddColumnConfig().apply { this.name = GENERATION_COLUMN_NAME },
                    AddColumnConfig().apply { this.name = LOCALE_COLUMN_NAME }
                )
            },
            CreateIndexChange().apply {
                this.tableName = tableName
                this.indexName = "${tableName}_${columnName}_${MAJOR_REV_COLUMN_NAME}_${LOCALE_COLUMN_NAME}_uk"
                this.isUnique = true
                this.columns = listOf(
                    AddColumnConfig().apply { this.name = columnName },
                    AddColumnConfig().apply { this.name = MAJOR_REV_COLUMN_NAME },
                    AddColumnConfig().apply { this.name = LOCALE_COLUMN_NAME }
                )
            }
        )
    }

    private fun createVersionedUniqueIndexes(item: Item, attrName: String): List<CreateIndexChange> {
        val attribute = item.spec.getAttribute(attrName)
        val columnName = attribute.getColumnName(attrName)
        if (!attribute.unique)
            throw IllegalArgumentException("Only the unique index needs to be versioned")

        val tableName = item.metadata.tableName

        return listOf(
            CreateIndexChange().apply {
                this.tableName = tableName
                this.indexName = "${tableName}_${columnName}_${GENERATION_COLUMN_NAME}_uk"
                this.isUnique = true
                this.columns = listOf(
                    AddColumnConfig().apply { this.name = columnName },
                    AddColumnConfig().apply { this.name = GENERATION_COLUMN_NAME }
                )
            },
            CreateIndexChange().apply {
                this.tableName = tableName
                this.indexName = "${tableName}_${columnName}_${MAJOR_REV_COLUMN_NAME}_uk"
                this.isUnique = true
                this.columns = listOf(
                    AddColumnConfig().apply { this.name = columnName },
                    AddColumnConfig().apply { this.name = MAJOR_REV_COLUMN_NAME }
                )
            }
        )
    }

    private fun localizedUniqueIndexFromAttribute(item: Item, attrName: String): CreateIndexChange {
        val attribute = item.spec.getAttribute(attrName)
        val columnName = attribute.getColumnName(attrName)
        if (!attribute.unique)
            throw IllegalArgumentException("Only the unique index needs to be localized")

        val tableName = item.metadata.tableName

        return CreateIndexChange().apply {
            this.tableName = tableName
            this.indexName = "${tableName}_${columnName}_${LOCALE_COLUMN_NAME}_uk"
            this.isUnique = true
            this.columns = mutableListOf(
                AddColumnConfig().apply { this.name = columnName },
                AddColumnConfig().apply { this.name = LOCALE_COLUMN_NAME }
            )
        }
    }

    private fun uniqueIndexFromAttribute(item: Item, attrName: String): CreateIndexChange {
        val tableName = item.metadata.tableName
        val attribute = item.spec.getAttribute(attrName)
        val columnName = attribute.getColumnName(attrName)

        return CreateIndexChange().apply {
            this.tableName = tableName
            this.indexName = "${tableName}_${columnName}_uk"
            this.isUnique = true
            this.columns = mutableListOf(
                AddColumnConfig().apply { this.name = columnName }
            )
        }
    }

    private fun indexFromAttribute(item: Item, attrName: String): CreateIndexChange {
        val tableName = item.metadata.tableName
        val attribute = item.spec.getAttribute(attrName)
        val columnName = attribute.getColumnName(attrName)

        return CreateIndexChange().apply {
            this.tableName = tableName
            this.indexName = "${tableName}_${columnName}_idx"
            this.columns = mutableListOf(
                AddColumnConfig().apply { this.name = columnName }
            )
        }
    }

    fun indexFromIndex(item: Item, indexName: String, index: Index): CreateIndexChange {
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

        private val logger = LoggerFactory.getLogger(LiquibaseIndexes::class.java)
    }
}