package ru.scisolutions.scicmscore.engine.schema.service.impl.liquibase

import liquibase.change.AddColumnConfig
import liquibase.change.core.CreateIndexChange
import liquibase.change.core.DropIndexChange
import org.slf4j.LoggerFactory
import ru.scisolutions.scicmscore.engine.schema.model.Item
import ru.scisolutions.scicmscore.engine.persistence.entity.Item as ItemEntity

class LiquibaseIndexes {
    fun createIndexes(item: Item): List<CreateIndexChange> {
        val list = mutableListOf<CreateIndexChange>()

        // Process attribute indexes
        for ((attrName, attribute) in item.spec.attributes) {
            if (!attribute.keyed && (attribute.unique || attribute.indexed)) {
                list.addAll(createAttributeIndexes(item, attrName))
            }
        }

        // Process indexes
        for ((indexName, _) in item.spec.indexes) {
            list.add(createIndexIndexChange(item, indexName))
        }

        return list
    }

    fun createAttributeIndexes(item: Item, attrName: String): List<CreateIndexChange> {
        val metadata = item.metadata
        val attribute = item.spec.getAttribute(attrName)
        if (attribute.keyed) {
            // throw IllegalArgumentException("Keyed attribute [{}] is already indexed", attrName)
            logger.debug("Keyed attribute [{}] is already indexed.", attrName)
            return emptyList()
        }

        if (!attribute.unique && !attribute.indexed) {
            // throw IllegalArgumentException("The attribute [$attrName] has no index")
            logger.debug("The attribute [{}] has no index.", attrName)
            return emptyList()
        }

        val attributeIndexes = mutableListOf<CreateIndexChange>()
        if (attribute.unique) {
            attributeIndexes.addAll(createUniqueIndexes(item, attrName))
        }

        // Add non-unique index
        if (metadata.versioned || metadata.localized) {
            attributeIndexes.add(createNonUniqueAttributeIndexChange(item, attrName))
        }

        return attributeIndexes
    }

    fun createUniqueIndexes(item: Item, attrName: String): List<CreateIndexChange> {
        val metadata = item.metadata
        val attribute = item.spec.getAttribute(attrName)
        val columnName = attribute.getColumnName(attrName)
        if (columnName == GENERATION_COLUMN_NAME || columnName == MAJOR_REV_COLUMN_NAME || columnName == LOCALE_COLUMN_NAME) {
            throw IllegalArgumentException("The column [$columnName] cannot be unique.")
        }

        val uniqueIndexes = mutableListOf<CreateIndexChange>()
        if (metadata.versioned) {
            if (metadata.localized) {
                uniqueIndexes.addAll(createVersionedAndLocalizedUniqueIndexes(item, attrName))
            } else {
                uniqueIndexes.addAll(createVersionedUniqueIndexes(item, attrName))
            }
        } else {
            if (metadata.localized) {
                uniqueIndexes.add(createLocalizedUniqueIndex(item, attrName))
            } else {
                uniqueIndexes.add(createUniqueIndex(item, attrName))
            }
        }

        return uniqueIndexes
    }

    fun dropUniqueIndexes(itemEntity: ItemEntity, attrName: String): List<DropIndexChange> {
        val attribute = itemEntity.spec.getAttribute(attrName)
        val columnName = attribute.getColumnName(attrName)
        if (columnName == GENERATION_COLUMN_NAME || columnName == MAJOR_REV_COLUMN_NAME || columnName == LOCALE_COLUMN_NAME) {
            throw IllegalArgumentException("The column [$columnName] cannot be unique.")
        }

        val uniqueIndexes = mutableListOf<DropIndexChange>()
        if (itemEntity.versioned) {
            if (itemEntity.localized) {
                uniqueIndexes.addAll(dropVersionedAndLocalizedUniqueIndexes(itemEntity, attrName))
            } else {
                uniqueIndexes.addAll(dropVersionedUniqueIndexes(itemEntity, attrName))
            }
        } else {
            if (itemEntity.localized) {
                uniqueIndexes.add(dropLocalizedUniqueIndex(itemEntity, attrName))
            } else {
                uniqueIndexes.add(dropUniqueIndex(itemEntity, attrName))
            }
        }

        return uniqueIndexes
    }

    private fun createVersionedAndLocalizedUniqueIndexes(item: Item, attrName: String): List<CreateIndexChange> {
        val attribute = item.spec.getAttribute(attrName)
        val columnName = attribute.getColumnName(attrName)
        if (!attribute.unique) {
            throw IllegalArgumentException("Only the unique index needs to be versioned and localized")
        }

        val tableName = requireNotNull(item.metadata.tableName)

        return listOf(
            CreateIndexChange().apply {
                this.tableName = tableName
                this.indexName = "${tableName}_${columnName}_${GENERATION_COLUMN_NAME}_${LOCALE_COLUMN_NAME}_uk"
                this.isUnique = true
                this.columns =
                    listOf(
                        AddColumnConfig().apply { this.name = attribute.columnName },
                        AddColumnConfig().apply { this.name = GENERATION_COLUMN_NAME },
                        AddColumnConfig().apply { this.name = LOCALE_COLUMN_NAME }
                    )
            },
            CreateIndexChange().apply {
                this.tableName = tableName
                this.indexName = "${tableName}_${columnName}_${MAJOR_REV_COLUMN_NAME}_${LOCALE_COLUMN_NAME}_uk"
                this.isUnique = true
                this.columns =
                    listOf(
                        AddColumnConfig().apply { this.name = columnName },
                        AddColumnConfig().apply { this.name = MAJOR_REV_COLUMN_NAME },
                        AddColumnConfig().apply { this.name = LOCALE_COLUMN_NAME }
                    )
            }
        )
    }

    private fun dropVersionedAndLocalizedUniqueIndexes(itemEntity: ItemEntity, attrName: String): List<DropIndexChange> {
        val attribute = itemEntity.spec.getAttribute(attrName)
        val columnName = attribute.getColumnName(attrName)
        if (!attribute.unique) {
            throw IllegalArgumentException("Only the unique index needs to be versioned and localized")
        }

        val tableName = requireNotNull(itemEntity.tableName)

        return listOf(
            DropIndexChange().apply {
                this.tableName = tableName
                this.indexName = "${tableName}_${columnName}_${GENERATION_COLUMN_NAME}_${LOCALE_COLUMN_NAME}_uk"
            },
            DropIndexChange().apply {
                this.tableName = tableName
                this.indexName = "${tableName}_${columnName}_${MAJOR_REV_COLUMN_NAME}_${LOCALE_COLUMN_NAME}_uk"
            }
        )
    }

    private fun createVersionedUniqueIndexes(item: Item, attrName: String): List<CreateIndexChange> {
        val attribute = item.spec.getAttribute(attrName)
        val columnName = attribute.getColumnName(attrName)
        if (!attribute.unique) {
            throw IllegalArgumentException("Only the unique index needs to be versioned")
        }

        val tableName = requireNotNull(item.metadata.tableName)

        return listOf(
            CreateIndexChange().apply {
                this.tableName = tableName
                this.indexName = "${tableName}_${columnName}_${GENERATION_COLUMN_NAME}_uk"
                this.isUnique = true
                this.columns =
                    listOf(
                        AddColumnConfig().apply { this.name = columnName },
                        AddColumnConfig().apply { this.name = GENERATION_COLUMN_NAME }
                    )
            },
            CreateIndexChange().apply {
                this.tableName = tableName
                this.indexName = "${tableName}_${columnName}_${MAJOR_REV_COLUMN_NAME}_uk"
                this.isUnique = true
                this.columns =
                    listOf(
                        AddColumnConfig().apply { this.name = columnName },
                        AddColumnConfig().apply { this.name = MAJOR_REV_COLUMN_NAME }
                    )
            }
        )
    }

    private fun dropVersionedUniqueIndexes(itemEntity: ItemEntity, attrName: String): List<DropIndexChange> {
        val attribute = itemEntity.spec.getAttribute(attrName)
        val columnName = attribute.getColumnName(attrName)
        if (!attribute.unique) {
            throw IllegalArgumentException("Only the unique index needs to be versioned")
        }

        val tableName = requireNotNull(itemEntity.tableName)

        return listOf(
            DropIndexChange().apply {
                this.tableName = tableName
                this.indexName = "${tableName}_${columnName}_${GENERATION_COLUMN_NAME}_uk"
            },
            DropIndexChange().apply {
                this.tableName = tableName
                this.indexName = "${tableName}_${columnName}_${MAJOR_REV_COLUMN_NAME}_uk"
            }
        )
    }

    private fun createLocalizedUniqueIndex(item: Item, attrName: String): CreateIndexChange {
        val attribute = item.spec.getAttribute(attrName)
        val columnName = attribute.getColumnName(attrName)
        if (!attribute.unique) {
            throw IllegalArgumentException("Only the unique index needs to be localized")
        }

        val tableName = requireNotNull(item.metadata.tableName)

        return CreateIndexChange().apply {
            this.tableName = tableName
            this.indexName = "${tableName}_${columnName}_${LOCALE_COLUMN_NAME}_uk"
            this.isUnique = true
            this.columns =
                mutableListOf(
                    AddColumnConfig().apply { this.name = columnName },
                    AddColumnConfig().apply { this.name = LOCALE_COLUMN_NAME }
                )
        }
    }

    private fun dropLocalizedUniqueIndex(itemEntity: ItemEntity, attrName: String): DropIndexChange {
        val attribute = itemEntity.spec.getAttribute(attrName)
        val columnName = attribute.getColumnName(attrName)
        if (!attribute.unique) {
            throw IllegalArgumentException("Only the unique index needs to be localized")
        }

        val tableName = requireNotNull(itemEntity.tableName)

        return DropIndexChange().apply {
            this.tableName = tableName
            this.indexName = "${tableName}_${columnName}_${LOCALE_COLUMN_NAME}_uk"
        }
    }

    private fun createUniqueIndex(item: Item, attrName: String): CreateIndexChange {
        val tableName = requireNotNull(item.metadata.tableName)
        val attribute = item.spec.getAttribute(attrName)
        val columnName = attribute.getColumnName(attrName)

        return CreateIndexChange().apply {
            this.tableName = tableName
            this.indexName = "${tableName}_${columnName}_uk"
            this.isUnique = true
            this.columns =
                mutableListOf(
                    AddColumnConfig().apply { this.name = columnName }
                )
        }
    }

    private fun dropUniqueIndex(itemEntity: ItemEntity, attrName: String): DropIndexChange {
        val tableName = requireNotNull(itemEntity.tableName)
        val attribute = itemEntity.spec.getAttribute(attrName)
        val columnName = attribute.getColumnName(attrName)

        return DropIndexChange().apply {
            this.tableName = tableName
            this.indexName = "${tableName}_${columnName}_uk"
        }
    }

    fun createNonUniqueAttributeIndexChange(item: Item, attrName: String): CreateIndexChange {
        val tableName = requireNotNull(item.metadata.tableName)
        val attribute = item.spec.getAttribute(attrName)
        val columnName = attribute.getColumnName(attrName)

        return CreateIndexChange().apply {
            this.tableName = tableName
            this.indexName = buildIndexName(tableName, columnName)
            this.columns =
                mutableListOf(
                    AddColumnConfig().apply { this.name = columnName }
                )
        }
    }

    private fun buildIndexName(tableName: String, columnName: String) = "${tableName}_${columnName}_idx"

    fun dropIndexChange(tableName: String, columnName: String): DropIndexChange = DropIndexChange().apply {
        this.tableName = tableName
        this.indexName = buildIndexName(tableName, columnName)
    }

    fun createIndexIndexChange(item: Item, indexName: String): CreateIndexChange {
        val tableName = requireNotNull(item.metadata.tableName)
        val index = item.spec.getIndex(indexName)

        return CreateIndexChange().apply {
            this.tableName = tableName
            this.indexName = buildIndexIndexName(tableName, indexName)
            this.isUnique = index.unique
            this.columns =
                index.columns.map {
                    AddColumnConfig().apply { this.name = it }
                }
        }
    }

    private fun buildIndexIndexName(tableName: String, indexName: String) = if (indexName.startsWith("_")) "${tableName}$indexName" else indexName

    fun dropIndexIndexChange(item: Item, indexName: String): DropIndexChange {
        val tableName = requireNotNull(item.metadata.tableName)

        return DropIndexChange().apply {
            this.tableName = tableName
            this.indexName = buildIndexIndexName(tableName, indexName)
        }
    }

    companion object {
        private const val GENERATION_COLUMN_NAME = "generation"
        private const val MAJOR_REV_COLUMN_NAME = "major_rev"
        private const val LOCALE_COLUMN_NAME = "locale"

        private val logger = LoggerFactory.getLogger(LiquibaseIndexes::class.java)
    }
}
