package ru.scisolutions.scicmscore.engine.schema.service.impl.liquibase

import liquibase.Liquibase
import liquibase.change.Change
import liquibase.change.core.CreateTableChange
import liquibase.change.core.DropTableChange
import liquibase.change.core.RenameTableChange
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.SchemaProps
import ru.scisolutions.scicmscore.engine.service.DatasourceManager
import ru.scisolutions.scicmscore.engine.model.Attribute
import ru.scisolutions.scicmscore.engine.model.Index
import ru.scisolutions.scicmscore.engine.schema.model.Item
import ru.scisolutions.scicmscore.engine.schema.service.TableSeeder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import ru.scisolutions.scicmscore.engine.persistence.entity.Item as ItemEntity

@Service
class LiquibaseTableSeeder(
    private val schemaProps: SchemaProps,
    private val dsManager: DatasourceManager
) : TableSeeder {
    private val liquibaseIndexes = LiquibaseIndexes()

    override fun create(item: Item) {
        val metadata = item.metadata
        if (!metadata.performDdl) {
            logger.info("DDL performing flag is disabled for item [{}]. Creating skipped.", metadata.name)
            return
        }

        logger.info("Creating the table [{}]", metadata.tableName)
        createTable(item)
    }

    override fun update(item: Item, existingItemEntity: ItemEntity) {
        val metadata = item.metadata
        if (!metadata.performDdl) {
            logger.info("DDL performing flag is disabled for item [{}]. Updating skipped.", metadata.name)
            return
        }

        if (metadata.readOnly) {
            logger.info("Item [{}] is read only. Updating skipped.", metadata.name)
            return
        }

        if (isTableChanged(item, existingItemEntity)) {
            logger.info("Updating table [{}]", metadata.tableName)
            updateTable(item, existingItemEntity)
        } else {
            logger.info("Table [{}] is unchanged. Nothing to update.", item.metadata.tableName)
        }
    }

    override fun delete(existingItemEntity: ItemEntity) {
        if (!existingItemEntity.performDdl) {
            logger.info("DDL performing flag is disabled for item [{}]. Deleting skipped.", existingItemEntity.name)
            return
        }

        val tableName = requireNotNull(existingItemEntity.tableName)
        logger.info("Deleting the table [{}]", tableName)
        dropTable(existingItemEntity.ds, tableName)
    }

    private fun createTable(item: Item) {
        val metadata = item.metadata
        val databaseChangeLog = DatabaseChangeLog("")
        val changeSet = addChangeSet(databaseChangeLog, "create-${metadata.tableName}-table")

        addCreateTableChange(changeSet, item) // create table

        // Run changelog
        val liquibase = newLiquibase(item.metadata.dataSource, databaseChangeLog)
        liquibase.update("")
        liquibase.close()
    }

    private fun addChangeSet(databaseChangeLog: DatabaseChangeLog, id: String): ChangeSet {
        val fullId = "${LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}-$id"
        val changeSet = ChangeSet(
            fullId,
            SecurityContextHolder.getContext().authentication?.name ?: "unknown",
            false,
            false,
            fullId,
            null,
            null,
            true,
            null,
            databaseChangeLog
        )
        databaseChangeLog.addChangeSet(changeSet)

        return changeSet
    }

    private fun addCreateTableChange(changeSet: ChangeSet, item: Item) {
        val metadata = item.metadata
        val createTableChange = CreateTableChange().apply {
            this.tableName = metadata.tableName
        }
        changeSet.addChange(createTableChange)

        // Add columns
        val columns = liquibaseColumns.list(item)
        for (column in columns) {
            createTableChange.addColumn(column)
        }

        // Add indexes
        val indexes = liquibaseIndexes.createIndexes(item)
        for (index in indexes) {
            changeSet.addChange(index)
        }
    }

    private fun isTableChanged(item: Item, existingItemEntity: ItemEntity): Boolean =
        (item.checksum == null || item.checksum != existingItemEntity.checksum) && item.hashCode().toString() != existingItemEntity.hash && (
            item.metadata.tableName != existingItemEntity.tableName
                || item.metadata.dataSource != existingItemEntity.ds
                || item.metadata.versioned != existingItemEntity.versioned
                || item.metadata.localized != existingItemEntity.localized
                || item.spec.hashCode() != existingItemEntity.spec.hashCode())

    private fun updateTable(item: Item, existingItemEntity: ItemEntity) {
        val metadata = item.metadata
        val existingTableName = requireNotNull(existingItemEntity.tableName)
        val newTableName = requireNotNull(metadata.tableName)
        if (isTableRenamedOnly(item, existingItemEntity)) {
            logger.warn("Table {} will only be RENAMED to {}", existingTableName, newTableName)
            renameTable(existingItemEntity, item)
        } else {
            val databaseChangeLog = DatabaseChangeLog("")
            val changeSet = addChangeSet(databaseChangeLog, "update-$newTableName-table")

            if (isTableRenamed(item, existingItemEntity)) {
                logger.warn("Table {} will be RENAMED to {}", existingTableName, newTableName)
                changeSet.addChange(renameTableChange(existingTableName, newTableName))
            }

            // ------------------
            // Process attributes
            // ------------------
            val existingAttributes: Map<String, Attribute> = existingItemEntity.spec.attributes
                .filter { (_, attribute) -> !attribute.isRelation() || !attribute.isCollection() }
                .toMap()

            val newAttributes: Map<String, Attribute> = item.spec.attributes
                .filter { (_, attribute) -> !attribute.isRelation() || !attribute.isCollection() }
                .toMap()

            val attributesToAdd: Map<String, Attribute> = newAttributes - existingAttributes.keys
            val attributesToRemove: Map<String, Attribute> = existingAttributes - newAttributes.keys

            val attributesToUpdate: Map<String, Attribute> = (newAttributes - attributesToAdd.keys)
                .filter { (attrName, attribute) ->
                    existingAttributes[attrName]?.hashCode()?.let { it != attribute.hashCode() } ?: false
                }
                .toMap()

            // Validate attributes before updating
            for ((attrName, attribute) in attributesToUpdate) {
                validateAttributeChanging(attrName, requireNotNull(existingAttributes[attrName]), attribute)
            }

            // Update columns
            for ((attrName, attribute) in attributesToUpdate) {
                val existingAttribute = requireNotNull(existingAttributes[attrName])
                logger.warn(
                    "Column {}.{}/{}.{} will be UPDATED",
                    existingItemEntity.tableName, existingAttribute.getColumnName(attrName), newTableName, attribute.getColumnName(attrName)
                )
                val modifyChanges = modifyColumnChangeList(existingItemEntity, item, attrName)
                for (modifyChange in modifyChanges) {
                    changeSet.addChange(modifyChange)
                }
            }

            // Remove columns
            for ((attrName, attribute) in attributesToRemove) {
                val columnName = attribute.getColumnName(attrName)
                logger.warn("Column {}.{} will be DROPPED", existingItemEntity.tableName, columnName)
                changeSet.addChange(liquibaseColumns.dropColumnChange(newTableName, columnName))
            }

            // Add columns
            for ((attrName, attribute) in attributesToAdd) {
                logger.warn("Column {}.{} will be CREATED", newTableName, attribute.getColumnName(attrName))
                val addChanges = addColumnChangeList(item, attrName)
                for (addChange in addChanges) {
                    changeSet.addChange(addChange)
                }
            }

            // ------------------------------------------------------------------------
            // Process non-unique indexes. Don't touch unique indexes due to complexity
            // ------------------------------------------------------------------------
            val existingIndexes: Map<String, Index> = existingItemEntity.spec.indexes
                .filter { (_, index) -> !index.unique }
                .toMap()

            val newIndexes: Map<String, Index> = item.spec.indexes
                .filter { (_, index) -> !index.unique }
                .toMap()

            val indexesToAdd: Map<String, Index> = newIndexes - existingIndexes.keys

            val actualAttrNames = existingAttributes.keys - attributesToRemove.keys
            val indexesToRemove: Map<String, Index> = (existingIndexes - newIndexes.keys)
                .filter { (idxName, _) -> idxName in actualAttrNames } // if attribute not exists, index is already dropped
                .toMap()

            val indexesToUpdate: Map<String, Index> = (newIndexes - indexesToAdd.keys)
                .filter { (idxName, index) ->
                    existingAttributes[idxName]?.hashCode()?.let { it != index.hashCode() } ?: false
                }
                .toMap()

            // Update indexes
            for ((indexName, index) in indexesToUpdate) {
                logger.warn("Non-unique index [{}] for columns {} will be UPDATED", indexName, index.columns)
                val modifyChanges = modifyIndexChangeList(item, indexName)
                for (modifyChange in modifyChanges) {
                    changeSet.addChange(modifyChange)
                }
            }

            // Remove indexes
            for ((indexName, index) in indexesToRemove) {
                logger.warn("Non-unique index [{}] for columns {} will be DROPPED", indexName, index.columns)
                changeSet.addChange(liquibaseIndexes.dropIndexIndexChange(item, indexName))
            }

            // Add indexes
            for ((indexName, index) in indexesToAdd) {
                logger.warn("Non-unique index [{}] for columns {} will be CREATED", indexName, index.columns)
                changeSet.addChange(liquibaseIndexes.createIndexIndexChange(item, indexName))
            }

            // Run changelog
            logger.debug("Running changelog with changeset [{}]", changeSet.id)
            val liquibase = newLiquibase(metadata.dataSource, databaseChangeLog)
            liquibase.update("")
            liquibase.close()
            logger.debug("Changelog with changeset [{}] finished successfully.", changeSet.id)
        }
    }

    private fun isTableRenamedOnly(item: Item, itemEntity: ItemEntity) =
        isTableRenamed(item, itemEntity) &&
            item.metadata.versioned == itemEntity.versioned &&
            item.metadata.localized == itemEntity.localized &&
            item.spec.hashCode() == itemEntity.spec.hashCode()

    private fun isTableRenamed(item: Item, itemEntity: ItemEntity) =
        item.metadata.tableName != itemEntity.tableName

    private fun renameTable(existingItemEntity: ItemEntity, item: Item) {
        val metadata = item.metadata
        val databaseChangeLog = DatabaseChangeLog("")
        val changeSet = addChangeSet(databaseChangeLog, "rename-${metadata.tableName}-table")

        // Rename table
        changeSet.addChange(
            renameTableChange(
                requireNotNull(existingItemEntity.tableName),
                requireNotNull(metadata.tableName)
            )
        )

        val liquibase = newLiquibase(item.metadata.dataSource, databaseChangeLog)
        liquibase.update("")
        liquibase.close()
    }

    private fun renameTableChange(oldTableName: String, newTableName: String): RenameTableChange =
        RenameTableChange().apply {
            this.oldTableName = oldTableName
            this.newTableName = newTableName
        }

    override fun dropTable(dataSource: String, tableName: String) {
        val databaseChangeLog = DatabaseChangeLog("")
        val changeSet = addChangeSet(databaseChangeLog, "drop-$tableName-table")

        addDropTableChange(changeSet, tableName, false) // drop table

        // Run changelog
        val liquibase = newLiquibase(dataSource, databaseChangeLog)
        liquibase.update("")
        liquibase.close()
    }

    private fun addDropTableChange(changeSet: ChangeSet, tableName: String, cascade: Boolean) {
        val dropTableChange = DropTableChange().apply {
            this.tableName = tableName
            this.isCascadeConstraints = cascade
        }
        changeSet.addChange(dropTableChange)
    }

    private fun validateAttributeChanging(attrName: String, existingAttribute: Attribute, newAttribute: Attribute) {
        if (newAttribute.isRelation() && !existingAttribute.isRelation())
            throw IllegalArgumentException("Cannot convert non-relation attribute [$attrName] into relation.")

        if (newAttribute.isRelation() && existingAttribute.isRelation() && newAttribute.target != existingAttribute.target)
            throw IllegalArgumentException("Cannot change target relation for attribute [$attrName].")
    }

    private fun addColumnChangeList(item: Item, attrName: String): List<Change> {
        val changeList = mutableListOf<Change>()
        // Add column
        changeList.add(liquibaseColumns.addColumnChange(item, attrName))

        // Add attribute indexes
        liquibaseIndexes.createAttributeIndexes(item, attrName).forEach { changeList.add(it) }

        // Add indexes
        val attribute = item.spec.getAttribute(attrName)
        item.spec.indexes
            .filter { (_, index: Index) -> (attribute.getColumnName(attrName)) in index.columns }
            .forEach { (indexName: String, _) ->
                changeList.add(liquibaseIndexes.createIndexIndexChange(item, indexName))
            }

        return changeList
    }

    private fun modifyColumnChangeList(existingItemEntity: ItemEntity, item: Item, attrName: String): List<Change> {
        val changeList = mutableListOf<Change>()
        val metadata = item.metadata
        val newAttribute = item.spec.getAttribute(attrName)
        val existingAttribute = existingItemEntity.spec.getAttribute(attrName)
        val existingColumnName = existingAttribute.getColumnName(attrName)
        val newColumnName = newAttribute.getColumnName(attrName)
        val tableName = requireNotNull(metadata.tableName)

        // Rename column
        if (newColumnName != existingColumnName) {
            logger.debug("Column {}.{} will be RENAMED to {}.{}", tableName, existingColumnName, tableName, newColumnName)
            changeList.add(liquibaseColumns.renameColumnChange(tableName, existingColumnName, newColumnName))
        }

        // Change data type
        if (newAttribute.type != existingAttribute.type ||
            newAttribute.length != existingAttribute.length ||
            newAttribute.precision != existingAttribute.precision ||
            newAttribute.scale != existingAttribute.scale) {
            logger.debug("Data type of column {}.{} will be MODIFIED", tableName, newColumnName)
            changeList.add(liquibaseColumns.modifyDataTypeChange(item, attrName))
        }

        // Drop not null
        if (existingAttribute.required && !newAttribute.required) {
            logger.debug("Not null constraint for column {}.{} will be DROPPED", tableName, newColumnName)
            changeList.add(liquibaseColumns.dropNotNullConstraintChange(tableName, newColumnName))
        }

        // Change default value
        if (newAttribute.defaultValue != existingAttribute.defaultValue) {
            existingAttribute.defaultValue?.let {
                logger.debug("Default value for column {}.{} will be DROPPED", tableName, newColumnName)
                changeList.add(liquibaseColumns.dropDefaultValueChange(tableName, newColumnName))
            }

            newAttribute.defaultValue?.let {
                logger.debug("Default value [{}] for column {}.{} will be ADDED", it, tableName, newColumnName)
                changeList.add(liquibaseColumns.addDefaultValueChange(tableName, newColumnName, it))
            }
        }

        // Add not null
        if (newAttribute.required && !existingAttribute.required) {
            logger.debug("Not null constraint for column {}.{} will be ADDED", tableName, newColumnName)
            changeList.add(liquibaseColumns.addNotNullConstraintChange(tableName, newColumnName))
        }

        // Drop primary key
        if (existingAttribute.keyed && !newAttribute.keyed) {
            logger.debug("Primary key constraint for column {}.{} will be DROPPED", tableName, newColumnName)
            changeList.add(liquibaseColumns.dropPrimaryKeyChange(tableName, newColumnName))
        }

        // Add primary key
        if (newAttribute.keyed && !existingAttribute.keyed) {
            logger.debug("Primary key constraint for column {}.{} will be ADDED", tableName, newColumnName)
            changeList.add(liquibaseColumns.addPrimaryKeyChange(tableName, newColumnName))
        }

        // Drop index
        if (existingAttribute.indexed && !newAttribute.indexed) {
            logger.debug("Index for column {}.{} will be dropped", tableName, newColumnName)
            changeList.add(liquibaseIndexes.dropIndexChange(tableName, existingColumnName))
        }

        // Drop unique index
        if (existingAttribute.unique && !newAttribute.unique) {
            logger.debug("Unique index(es) for column {}.{} will be DROPPED", tableName, newColumnName)
            changeList.addAll(liquibaseIndexes.dropUniqueIndexes(existingItemEntity, attrName))
        }

        // Add unique index
        if (newAttribute.unique && !existingAttribute.unique) {
            logger.debug("Unique index(es) for column {}.{} will be CREATED", tableName, newColumnName)
            changeList.addAll(liquibaseIndexes.createUniqueIndexes(item, attrName))
        }

        // Add index
        if (newAttribute.indexed && !existingAttribute.indexed) {
            logger.debug("Index for column {}.{} will be CREATED", tableName, newColumnName)
            changeList.add(liquibaseIndexes.createNonUniqueAttributeIndexChange(item, attrName))
        }

        if (schemaProps.rebuildUniqueAttributeIndexes &&
            (metadata.versioned != existingItemEntity.versioned || metadata.localized != existingItemEntity.localized)
            && newAttribute.unique && existingAttribute.unique) {
            logger.debug(
                "Versioned/localized flags for item [{}] has changed. Unique index(es) for column {}.{} will be RECREATED",
                metadata.name, tableName, newColumnName
            )
            changeList.addAll(liquibaseIndexes.dropUniqueIndexes(existingItemEntity, attrName))
            changeList.addAll(liquibaseIndexes.createUniqueIndexes(item, attrName))
        }

        return changeList
    }

    private fun modifyIndexChangeList(item: Item, indexName: String): List<Change> =
        listOf(
            liquibaseIndexes.dropIndexIndexChange(item, indexName),
            liquibaseIndexes.createIndexIndexChange(item, indexName)
        )

    private fun newLiquibase(dataSourceName: String, databaseChangeLog: DatabaseChangeLog): Liquibase {
        val dataSource = dsManager.dataSource(dataSourceName)

        return Liquibase(
            databaseChangeLog,
            null,
            DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
                JdbcConnection(dataSource.connection)
            )
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LiquibaseTableSeeder::class.java)
        private val liquibaseColumns = LiquibaseColumns()
    }
}