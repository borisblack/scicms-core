package ru.scisolutions.scicmscore.schema.service.impl.liquibase

import liquibase.Liquibase
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
import ru.scisolutions.scicmscore.config.props.I18nProps
import ru.scisolutions.scicmscore.config.props.VersioningProps
import ru.scisolutions.scicmscore.engine.service.DatasourceManager
import ru.scisolutions.scicmscore.model.Attribute
import ru.scisolutions.scicmscore.model.Index
import ru.scisolutions.scicmscore.schema.model.Item
import ru.scisolutions.scicmscore.schema.service.TableSeeder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import ru.scisolutions.scicmscore.persistence.entity.Item as ItemEntity

@Service
class LiquibaseTableSeeder(
    versioningProps: VersioningProps,
    i18nProps: I18nProps,
    private val dsManager: DatasourceManager
) : TableSeeder {
    private val liquibaseIndexes = LiquibaseIndexes(
        versioningProps.includeInUniqueIndex,
        i18nProps.includeInUniqueIndex
    )

    override fun create(item: Item) {
        val metadata = item.metadata
        if (!metadata.performDdl) {
            logger.info("DDL performing flag is disabled for item [{}]. Creating skipped", metadata.name)
            return
        }

        logger.info("Creating the table [{}]", metadata.tableName)
        createTable(item)
    }

    override fun update(item: Item, existingItemEntity: ItemEntity) {
        val metadata = item.metadata
        if (!metadata.performDdl) {
            logger.info("DDL performing flag is disabled for item [{}]. Updating skipped", metadata.name)
            return
        }

        if (metadata.readOnly) {
            logger.info("Item [{}] is read only. Updating skipped", metadata.name)
            return
        }

        if (isTableChanged(item, existingItemEntity)) {
            logger.info("Updating table [{}]", metadata.tableName)
            updateTable(item, existingItemEntity)
        } else {
            logger.info("Table [{}] is unchanged. Nothing to update", item.metadata.tableName)
        }
    }

    override fun delete(existingItemEntity: ItemEntity) {
        if (!existingItemEntity.performDdl) {
            logger.info("DDL performing flag is disabled for item [{}]. Deleting skipped", existingItemEntity.name)
            return
        }

        logger.info("Deleting the table [{}]", existingItemEntity.tableName)
        dropTable(existingItemEntity)
    }

    private fun createTable(item: Item) {
        val metadata = item.metadata
        val databaseChangeLog = DatabaseChangeLog()
        val changeSet = addChangeSet(databaseChangeLog, "create-${metadata.tableName}")

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
        val tableName = requireNotNull(metadata.tableName)
        if (isTableRenamedOnly(item, existingItemEntity)) {
            logger.warn("Table {} will only be renamed into {}", existingItemEntity.tableName, tableName)
            renameTable(item, existingItemEntity)
        } else {
            if (isTableRenamed(item, existingItemEntity)) {
                logger.warn("Table {} will be renamed into {}", existingItemEntity.tableName, tableName)
                renameTable(item, existingItemEntity)
            }

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
                    existingItemEntity.tableName, existingAttribute.getColumnName(attrName), tableName, attribute.getColumnName(attrName)
                )
                modifyColumn(item, existingItemEntity, attrName)
                logger.info(
                    "Column {}.{}/{}.{} is UPDATED successfully.",
                    existingItemEntity.tableName, existingAttribute.getColumnName(attrName), tableName, attribute.getColumnName(attrName)
                )
            }

            // Remove columns
            for ((attrName, _) in attributesToRemove) {
                val existingAttribute = requireNotNull(existingAttributes[attrName])
                logger.warn("Column {}.{} will be DELETED", existingItemEntity.tableName, existingAttribute.getColumnName(attrName))
                dropColumn(existingItemEntity, tableName, attrName)
                logger.info("Column {}.{} is DELETED successfully.", existingItemEntity.tableName, existingAttribute.getColumnName(attrName))
            }

            // Add columns
            for ((attrName, attribute) in attributesToAdd) {
                logger.warn("Column {}.{} will be CREATED", tableName, attribute.getColumnName(attrName))
                addColumn(item, attrName)
                logger.info("Column {}.{} is CREATED successfully.", tableName, attribute.getColumnName(attrName))
            }
        }
    }

    private fun isTableRenamedOnly(item: Item, itemEntity: ItemEntity) =
        isTableRenamed(item, itemEntity) &&
            item.metadata.versioned == itemEntity.versioned &&
            item.metadata.localized == itemEntity.localized &&
            item.spec.hashCode() == itemEntity.spec.hashCode()

    private fun isTableRenamed(item: Item, itemEntity: ItemEntity) =
        item.metadata.tableName != itemEntity.tableName

    private fun renameTable(item: Item, itemEntity: ItemEntity) {
        val metadata = item.metadata
        val databaseChangeLog = DatabaseChangeLog()
        val changeSet = addChangeSet(databaseChangeLog, "rename-${metadata.tableName}")

        // Rename table
        addRenameTableChange(
            changeSet,
            requireNotNull(itemEntity.tableName),
            requireNotNull(metadata.tableName)
        )

        val liquibase = newLiquibase(item.metadata.dataSource, databaseChangeLog)
        liquibase.update("")
        liquibase.close()
    }

    private fun addRenameTableChange(changeSet: ChangeSet, oldTableName: String, newTableName: String) {
        val renameTableChange = RenameTableChange().apply {
            this.oldTableName = oldTableName
            this.newTableName = newTableName
        }
        changeSet.addChange(renameTableChange)
    }

    private fun dropTable(itemEntity: ItemEntity) {
        val databaseChangeLog = DatabaseChangeLog()
        val changeSet = addChangeSet(databaseChangeLog, "drop-${itemEntity.tableName}")

        addDropTableChange(changeSet, requireNotNull(itemEntity.tableName), false) // drop table

        // Run changelog
        val liquibase = newLiquibase(itemEntity.ds, databaseChangeLog)
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

    private fun addColumn(item: Item, attrName: String) {
        val metadata = item.metadata
        val databaseChangeLog = DatabaseChangeLog()
        val columnName = item.spec.getColumnName(attrName)
        val changeSet = addChangeSet(databaseChangeLog, "add-${metadata.tableName}.$columnName-column")

        addAddColumnChange(changeSet, item, attrName)

        // Run changelog
        val liquibase = newLiquibase(metadata.dataSource, databaseChangeLog)
        liquibase.update("")
        liquibase.close()
    }

    private fun addAddColumnChange(changeSet: ChangeSet, item: Item, attrName: String) {
        // Add column
        changeSet.addChange(liquibaseColumns.addColumnChange(item, attrName))

        // Add attribute indexes
        liquibaseIndexes.createAttributeIndexes(item, attrName).forEach { changeSet.addChange(it) }

        // Add indexes
        val attribute = item.spec.getAttribute(attrName)
        item.spec.indexes
            .filter { (_, index: Index) -> (attribute.getColumnName(attrName)) in index.columns }
            .forEach { (indexName: String, index: Index) ->
                changeSet.addChange(liquibaseIndexes.indexFromIndex(item, indexName, index))
            }
    }

    private fun dropColumn(existingItemEntity: ItemEntity, tableName: String, attrName: String) {
        val databaseChangeLog = DatabaseChangeLog()
        val attribute = existingItemEntity.spec.getAttribute(attrName)
        val columnName = attribute.columnName ?: attrName.lowercase()
        val changeSet = addChangeSet(databaseChangeLog, "drop-${tableName}.$columnName-column")
        changeSet.addChange(
            liquibaseColumns.dropColumnChange(tableName, columnName)
        )

        // Run changelog
        val liquibase = newLiquibase(existingItemEntity.ds, databaseChangeLog)
        liquibase.update("")
        liquibase.close()
    }

    private fun modifyColumn(item: Item, existingItemEntity: ItemEntity, attrName: String) {
        val metadata = item.metadata
        val databaseChangeLog = DatabaseChangeLog()
        val newAttribute = item.spec.getAttribute(attrName)
        val existingAttribute = existingItemEntity.spec.getAttribute(attrName)
        val existingColumnName = existingAttribute.getColumnName(attrName)
        val newColumnName = newAttribute.getColumnName(attrName)
        val tableName = requireNotNull(metadata.tableName)
        val changeSet = addChangeSet(databaseChangeLog, "update-$tableName.$existingColumnName-column")

        // Rename column
        if (newColumnName != existingColumnName) {
            val renameColumnChange = liquibaseColumns.renameColumnChange(tableName, existingColumnName, newColumnName)
            changeSet.addChange(renameColumnChange)
        }

        // Change data type
        if (newAttribute.type != existingAttribute.type ||
            newAttribute.length != existingAttribute.length ||
            newAttribute.precision != existingAttribute.precision ||
            newAttribute.scale != existingAttribute.scale) {
            changeSet.addChange(liquibaseColumns.modifyDataTypeChange(item, attrName))
        }

        // Drop not null
        if (existingAttribute.required && !newAttribute.required) {
            changeSet.addChange(liquibaseColumns.dropNotNullConstraintChange(tableName, newColumnName))
        }

        // Change default value
        if (newAttribute.defaultValue != existingAttribute.defaultValue) {
            existingAttribute.defaultValue?.let {
                changeSet.addChange(liquibaseColumns.dropDefaultValueChange(tableName, newColumnName))
            }

            newAttribute.defaultValue?.let {
                changeSet.addChange(liquibaseColumns.addDefaultValueChange(tableName, newColumnName, it))
            }
        }

        // Add not null
        if (newAttribute.required && !existingAttribute.required) {
            changeSet.addChange(liquibaseColumns.addNotNullConstraintChange(tableName, newColumnName))
        }

        // Drop primary key
        if (existingAttribute.keyed && !newAttribute.keyed) {
            changeSet.addChange(liquibaseColumns.dropPrimaryKeyChange(tableName, newColumnName))
        }

        // Add primary key
        if (newAttribute.keyed && !existingAttribute.keyed) {
            changeSet.addChange(liquibaseColumns.addPrimaryKeyChange(tableName, newColumnName))
        }

        // Run changelog
        val liquibase = newLiquibase(metadata.dataSource, databaseChangeLog)
        liquibase.update("")
        liquibase.close()
    }

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