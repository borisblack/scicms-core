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
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.schema.seeder.ItemSeeder
import ru.scisolutions.scicmscore.engine.schema.model.Item
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.sql.DataSource
import ru.scisolutions.scicmscore.entity.Item as ItemEntity

@Service
class LiquibaseItemSeeder(
    @Value("\${scicms-core.schema.versioning.include-in-unique-index:true}")
    private val versioningIncludeInUniqueIndex: Boolean,
    @Value("\${scicms-core.schema.i18n.include-in-unique-index:true}")
    private val i18nIncludeInUniqueIndex: Boolean,
    private val dataSource: DataSource
) : ItemSeeder {
    private val liquibaseIndexes = LiquibaseIndexes(versioningIncludeInUniqueIndex, i18nIncludeInUniqueIndex)

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
        val liquibase = newLiquibase(databaseChangeLog)
        liquibase.update("")
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
                item.metadata.versioned != itemEntity.versioned ||
                item.metadata.localized != itemEntity.localized ||
                item.spec.hashCode() != itemEntity.spec.hashCode())

    private fun updateTable(item: Item, itemEntity: ItemEntity) {
        val metadata = item.metadata

        val databaseChangeLog = DatabaseChangeLog()
        val changeSet = addChangeSet(databaseChangeLog, "update-${metadata.tableName}")

        if (isOnlyTableNameChanged(item, itemEntity)) {
            addRenameTableChange(changeSet, itemEntity.tableName, metadata.tableName) // rename table
        } else {
            addDropTableChange(changeSet, itemEntity.tableName, false) // drop table

            addCreateTableChange(changeSet, item) // create table
        }

        // Run changelog
        val liquibase = newLiquibase(databaseChangeLog)
        liquibase.update("")
    }

    private fun isOnlyTableNameChanged(item: Item, itemEntity: ItemEntity) =
        item.metadata.tableName != itemEntity.tableName &&
            item.metadata.versioned == itemEntity.versioned &&
            item.metadata.localized == itemEntity.localized &&
            item.spec.hashCode() == itemEntity.spec.hashCode()

    private fun addRenameTableChange(changeSet: ChangeSet, oldTableName: String, newTableName: String) {
        val renameTableChange = RenameTableChange().apply {
            this.oldTableName = oldTableName
            this.newTableName = newTableName
        }
        changeSet.addChange(renameTableChange)
    }

    private fun addDropTableChange(changeSet: ChangeSet, tableName: String, cascade: Boolean) {
        val dropTableChange = DropTableChange().apply {
            this.tableName = tableName
            this.isCascadeConstraints = cascade
        }
        changeSet.addChange(dropTableChange)
    }

    private fun dropTable(itemEntity: ItemEntity) {
        val databaseChangeLog = DatabaseChangeLog()
        val changeSet = addChangeSet(databaseChangeLog, "update-${itemEntity.tableName}")

        addDropTableChange(changeSet, itemEntity.tableName, false) // drop table

        // Run changelog
        val liquibase = newLiquibase(databaseChangeLog)
        liquibase.update("")
    }

    private fun newLiquibase(databaseChangeLog: DatabaseChangeLog) = Liquibase(
        databaseChangeLog,
        null,
        DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
            JdbcConnection(dataSource.connection)
        )
    )

    companion object {
        private val logger = LoggerFactory.getLogger(LiquibaseItemSeeder::class.java)
        private val liquibaseColumns = LiquibaseColumns()
    }
}