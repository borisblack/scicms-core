package ru.scisolutions.scicmscore.engine.schema.seeder.liquibase

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
import ru.scisolutions.scicmscore.config.DataSourceMap
import ru.scisolutions.scicmscore.config.props.I18nProps
import ru.scisolutions.scicmscore.config.props.VersioningProps
import ru.scisolutions.scicmscore.engine.schema.model.Item
import ru.scisolutions.scicmscore.engine.schema.seeder.TableSeeder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import ru.scisolutions.scicmscore.persistence.entity.Item as ItemEntity

@Service
class LiquibaseTableSeeder(
    versioningProps: VersioningProps,
    i18nProps: I18nProps,
    private val dataSourceMap: DataSourceMap
) : TableSeeder {
    private val liquibaseIndexes = LiquibaseIndexes(
        versioningProps.includeInUniqueIndex,
        i18nProps.includeInUniqueIndex
    )

    override fun create(item: Item) {
        val metadata = item.metadata
        if (!metadata.performDdl) {
            logger.info("Item [{}]: DDL performing flag is disabled. Creating skipped", metadata.name)
            return
        }

        logger.info("Creating the table [{}]", metadata.tableName)
        createTable(item)
    }

    override fun update(item: Item, itemEntity: ItemEntity) {
        val metadata = item.metadata
        if (!metadata.performDdl) {
            logger.info("Item [{}]: DDL performing flag is disabled. Updating skipped", metadata.name)
            return
        }

        if (isChanged(item, itemEntity)) {
            logger.info("Updating the table [{}]", metadata.tableName)
            updateTable(item, itemEntity)
        } else {
            logger.info("Item [{}] is unchanged. Nothing to update", item.metadata.name)
        }
    }

    override fun delete(itemEntity: ItemEntity) {
        if (!itemEntity.performDdl) {
            logger.info("Item [{}]: DDL performing flag is disabled. Deleting skipped", itemEntity.name)
            return
        }

        logger.info("Deleting the table [{}]", itemEntity.tableName)
        dropTable(itemEntity)
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

    private fun isChanged(item: Item, itemEntity: ItemEntity): Boolean =
        item.hashCode().toString() != itemEntity.checksum && (
            item.metadata.tableName != itemEntity.tableName ||
                item.metadata.dataSource != itemEntity.dataSource ||
                item.metadata.versioned != itemEntity.versioned ||
                item.metadata.localized != itemEntity.localized ||
                item.spec.hashCode() != itemEntity.spec.hashCode())

    private fun updateTable(item: Item, itemEntity: ItemEntity) {
        if (isOnlyTableNameChanged(item, itemEntity)) {
            renameTable(item, itemEntity)
        } else {
            dropTable(itemEntity)
            createTable(item)
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
        addRenameTableChange(changeSet, itemEntity.tableName, metadata.tableName) // rename table
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

        addDropTableChange(changeSet, itemEntity.tableName, false) // drop table

        // Run changelog
        val liquibase = newLiquibase(itemEntity.dataSource, databaseChangeLog)
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

    private fun newLiquibase(dataSourceName: String, databaseChangeLog: DatabaseChangeLog): Liquibase {
        val dataSource = dataSourceMap.getOrThrow(dataSourceName)

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