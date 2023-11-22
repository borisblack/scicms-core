package ru.scisolutions.scicmscore.schema.service.impl.liquibase

import liquibase.Liquibase
import liquibase.change.core.*
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.I18nProps
import ru.scisolutions.scicmscore.config.props.SchemaProps
import ru.scisolutions.scicmscore.config.props.VersioningProps
import ru.scisolutions.scicmscore.engine.service.DatasourceManager
import ru.scisolutions.scicmscore.model.Attribute
import ru.scisolutions.scicmscore.model.Attribute.RelType
import ru.scisolutions.scicmscore.model.FieldType
import ru.scisolutions.scicmscore.model.Index
import ru.scisolutions.scicmscore.schema.model.Item
import ru.scisolutions.scicmscore.schema.service.TableSeeder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import ru.scisolutions.scicmscore.persistence.entity.Item as ItemEntity

@Service
class LiquibaseTableSeeder(
    private val schemaProps: SchemaProps,
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
            logger.info("Updating the table [{}]", metadata.tableName)
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
        val indexes = liquibaseIndexes.list(item)
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
        if (isOnlyTableNameChanged(item, existingItemEntity)) {
            logger.warn("Table {} will be renamed into {}", existingItemEntity.tableName, item.metadata.tableName)
            renameTable(item, existingItemEntity)
        } else {
            logger.info("Updating table {}/{}. The tryRecreateAttributes flag is {}", existingItemEntity.tableName, item.metadata.tableName, schemaProps.tryRecreateAttributes)
            if (!schemaProps.tryRecreateAttributes) {
                dropTable(existingItemEntity)
                createTable(item)
                return
            }

            // Try to recreate attributes only
            val uniqueIndexColumns = item.spec.indexes.values
                .filter { it.unique }
                .flatMap { it.columns }
                .toSet()

            var isNeedRecreateTable = false
            val attributesToUpdate = mutableSetOf<String>()
            for ((attrName, attribute) in item.spec.attributes) {
                if (attribute.type == FieldType.relation && (attribute.relType == RelType.oneToMany || attribute.relType == RelType.manyToMany))
                    continue

                val existingAttribute = existingItemEntity.spec.attributes[attrName]
                if (existingAttribute == null || attribute.hashCode() == existingAttribute.hashCode())
                    continue

                if (cannotRecreateAttribute(attrName, attribute, existingAttribute, uniqueIndexColumns)) {
                    isNeedRecreateTable = true
                    break
                } else {
                    attributesToUpdate.add(attrName)
                }
            }

            if (isNeedRecreateTable) {
                logger.warn("Table {}/{} will be deleted/created", existingItemEntity.tableName, item.metadata.tableName)
                dropTable(existingItemEntity)
                createTable(item)
            } else {
                val attributesToRemove: Set<String> = existingItemEntity.spec.attributes
                    .filter { (_, attribute) -> attribute.type != FieldType.relation }
                    .filter { (attrName, _) -> attrName !in item.spec.attributes }
                    .keys

                attributesToRemove.forEach {
                    val existingAttribute = existingItemEntity.spec.attributes[it] as Attribute
                    logger.warn("Column {}.{} will be DELETED", existingItemEntity.tableName, existingAttribute.columnName ?: it.lowercase())
                    dropColumn(existingItemEntity, it)
                }

                attributesToUpdate.forEach {
                    val attribute = item.spec.attributes[it] as Attribute
                    val existingAttribute = existingItemEntity.spec.attributes[it] as Attribute
                    logger.warn(
                        "Column {}.{}/{}.{} will be DELETED/CREATED",
                        existingItemEntity.tableName, existingAttribute.columnName ?: it.lowercase(),
                        item.metadata.tableName, attribute.columnName ?: it.lowercase()
                    )
                    dropColumn(existingItemEntity, it)
                    addColumn(item, it)
                }

                val attributesToAdd: Set<String> = item.spec.attributes
                    .filter { (_, attribute) -> attribute.type != FieldType.relation }
                    .filter { (attrName, _) -> attrName !in existingItemEntity.spec.attributes }
                    .keys

                attributesToAdd.forEach {
                    val attribute = item.spec.attributes[it] as Attribute
                    logger.warn("Column {}.{} will be CREATED", existingItemEntity.tableName, attribute.columnName ?: it.lowercase())
                    addColumn(item, it)
                }
            }
        }
    }

    private fun isOnlyTableNameChanged(item: Item, itemEntity: ItemEntity) =
        item.metadata.tableName != itemEntity.tableName &&
            item.metadata.versioned == itemEntity.versioned &&
            item.metadata.localized == itemEntity.localized &&
            item.spec.hashCode() == itemEntity.spec.hashCode()

    private fun renameTable(item: Item, itemEntity: ItemEntity) {
        val metadata = item.metadata
        val databaseChangeLog = DatabaseChangeLog()
        val changeSet = addChangeSet(databaseChangeLog, "rename-${metadata.tableName}")
        addRenameTableChange(changeSet, requireNotNull(itemEntity.tableName), requireNotNull(metadata.tableName)) // rename table
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
        val changeSet = addChangeSet(databaseChangeLog, "update-${itemEntity.tableName}")

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

    private fun cannotRecreateAttribute(attrName: String, attribute: Attribute, existingAttribute: Attribute, uniqueIndexColumns: Set<String>): Boolean =
        (attribute.keyed && !existingAttribute.keyed)
            || (attribute.type == FieldType.relation && (attribute.relType == RelType.oneToOne || attribute.relType == RelType.manyToOne))
            || (attribute.required && !existingAttribute.required)
            || (attribute.unique && !existingAttribute.unique)
            || (attribute.columnName ?: attrName.lowercase()) in uniqueIndexColumns
            || (attribute.length == null && existingAttribute.length != null)
            || (attribute.length != null && existingAttribute.length != null && attribute.length < existingAttribute.length)
            || (attribute.precision == null && existingAttribute.precision != null)
            || (attribute.precision != null && existingAttribute.precision != null && attribute.precision < existingAttribute.precision)
            || (attribute.scale == null && existingAttribute.scale != null)
            || (attribute.scale != null && existingAttribute.scale != null && attribute.scale < existingAttribute.scale)

    private fun dropColumn(existingItemEntity: ItemEntity, attrName: String) {
        val databaseChangeLog = DatabaseChangeLog()
        val changeSet = addChangeSet(databaseChangeLog, "drop-${existingItemEntity.tableName}-column")

        val attribute = existingItemEntity.spec.attributes[attrName] as Attribute
        addDropColumnChange(changeSet, requireNotNull(existingItemEntity.tableName), attribute.columnName ?: attrName.lowercase())

        // Run changelog
        val liquibase = newLiquibase(existingItemEntity.ds, databaseChangeLog)
        liquibase.update("")
        liquibase.close()
    }

    private fun addDropColumnChange(changeSet: ChangeSet, tableName: String, columnName: String) {
        val dropTableChange = DropColumnChange().apply {
            this.tableName = tableName
            this.columnName = columnName
        }
        changeSet.addChange(dropTableChange)
    }

    private fun addColumn(item: Item, attrName: String) {
        val metadata = item.metadata
        val databaseChangeLog = DatabaseChangeLog()
        val changeSet = addChangeSet(databaseChangeLog, "add-${metadata.tableName}-column")

        val attribute = item.spec.attributes[attrName] as Attribute
        addAddColumnChange(changeSet, item, attrName, attribute)

        // Run changelog
        val liquibase = newLiquibase(metadata.dataSource, databaseChangeLog)
        liquibase.update("")
        liquibase.close()
    }

    private fun addAddColumnChange(changeSet: ChangeSet, item: Item, attrName: String, attribute: Attribute) {
        val metadata = item.metadata

        // Add column
        val addColumnChange = AddColumnChange().apply {
            this.tableName = metadata.tableName
            this.columns = listOf(liquibaseColumns.getAddColumn(item, attrName, attribute))
        }
        changeSet.addChange(addColumnChange)

        // Add attribute indexes
        liquibaseIndexes.listAttributeIndexes(item, attrName).forEach { changeSet.addChange(it) }

        // Add indexes
        item.spec.indexes
            .filter { (_, index: Index) -> (attribute.columnName ?: attrName.lowercase()) in index.columns }
            .forEach { (indexName: String, index: Index) ->
                changeSet.addChange(liquibaseIndexes.indexFromIndex(item, indexName, index))
            }
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