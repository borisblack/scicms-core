package ru.scisolutions.scicmscore.dbschema.seeder.impl

import liquibase.Liquibase
import liquibase.change.AddColumnConfig
import liquibase.change.ColumnConfig
import liquibase.change.ConstraintsConfig
import liquibase.change.core.CreateIndexChange
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
import ru.scisolutions.scicmscore.api.model.Index
import ru.scisolutions.scicmscore.api.model.Item
import ru.scisolutions.scicmscore.api.model.Property
import ru.scisolutions.scicmscore.api.model.Property.Type
import ru.scisolutions.scicmscore.api.model.Property.RelType
import ru.scisolutions.scicmscore.entity.Item as ItemEntity
import ru.scisolutions.scicmscore.dbschema.seeder.ItemSeeder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.sql.DataSource

@Service
class LiquibaseItemSeeder(private val dataSource: DataSource) : ItemSeeder {
    override fun seed(item: Item, itemEntity: ItemEntity?) {
        val metadata = item.metadata
        if (!metadata.performDdl) {
            logger.info("Item [{}]: DDL performing flag is disabled. Seeding skipped", metadata.name)
            return
        }

        if (itemEntity == null) {
            logger.info("Creating the table [{}]", metadata.tableName)
            createTable(item)
        } else if (isChanged(item, itemEntity)) {
            logger.info("Updating the table [{}]", metadata.tableName)
            updateTable(item, itemEntity)
        } else {
            logger.info("Item [{}] is unchanged. Nothing to seed", item.metadata.name)
        }
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
        val spec = item.spec
        val createTableChange = CreateTableChange().apply {
            this.tableName = metadata.tableName
        }
        changeSet.addChange(createTableChange)

        // Add columns
        for ((_, property) in spec.properties) {
            // Skip list relation types
            if (property.type == Type.RELATION.value &&
                (property.relType == RelType.ONE_TO_MANY.value || property.relType == RelType.MANY_TO_MANY.value))
                break

            createTableChange.addColumn(newColumn(metadata.tableName, property))

            // Add index if required
            if (property.indexed && !property.keyed && !property.unique) { // the primary key or unique column is already indexed
                addCreateIndexChange(changeSet, metadata.tableName, property)
            }
        }

        // Add indexes
        for ((name, index) in spec.indexes) {
            addCreateIndexChange(changeSet, metadata.tableName, name, index)
        }
    }

    private fun newColumn(tableName: String, property: Property) = ColumnConfig().apply {
        this.name = property.columnName
        this.type = getLiquibaseType(tableName, property)

        if (property.defaultValue != null)
            this.defaultValue = property.defaultValue

        this.constraints = ConstraintsConfig().apply {
            this.isNullable = !property.required

            if (property.keyed) {
                this.isPrimaryKey = true
                this.primaryKeyName = "${tableName}_${property.columnName}_pk"
            } else if (property.unique) {
                this.isUnique = true
                this.uniqueConstraintName = "${tableName}_${property.columnName}_uk"
            }
        }
    }

    private fun getLiquibaseType(tableName: String, property: Property): String = when (property.type) {
        Type.UUID.value, Type.MEDIA.value -> "varchar(36)"
        Type.STRING.value -> {
            if (property.length == null || property.length <= 0)
                throw IllegalArgumentException("Column [$tableName.${property.columnName}]: Invalid string length (${property.length})")

            "varchar(${property.length})"
        }
        Type.TEXT.value, Type.ARRAY.value, Type.JSON.value -> "text"
        Type.ENUM.value, Type.SEQUENCE.value, Type.EMAIL.value, Type.PASSWORD.value -> "varchar(50)"
        Type.INT.value -> {
            if (property.minRange != null && property.maxRange != null && property.minRange > property.maxRange)
                throw IllegalArgumentException("Column [$tableName.${property.columnName}]: Invalid range ratio (minRange=${property.minRange} > maxRange=${property.maxRange})")

            "int"
        }
        Type.FLOAT.value -> {
            if (property.minRange != null && property.maxRange != null && property.minRange > property.maxRange)
                throw IllegalArgumentException("Column [$tableName.${property.columnName}]: Invalid range ratio (minRange=${property.minRange} > maxRange=${property.maxRange})")

            "float"
        }
        Type.DECIMAL.value -> {
            if (property.precision == null || property.precision <= 0 || property.scale == null || property.scale < 0)
                throw IllegalArgumentException("Column [$tableName.${property.columnName}]: Invalid precision and/or scale (${property.precision}, ${property.scale})")

            if (property.minRange != null && property.maxRange != null && property.minRange > property.maxRange)
                throw IllegalArgumentException("Column [$tableName.${property.columnName}]: Invalid range ratio (minRange=${property.minRange} > maxRange=${property.maxRange})")

            "decimal(${property.precision},${property.scale})"
        }
        Type.DATE.value -> "date"
        Type.TIME.value -> "time"
        Type.DATETIME.value -> "datetime"
        Type.TIMESTAMP.value -> "timestamp"
        Type.BOOL.value -> "boolean"
        Type.RELATION.value -> {
            if (property.relType == RelType.ONE_TO_MANY.value || property.relType == RelType.MANY_TO_MANY.value)
                throw IllegalArgumentException("Column [$tableName.${property.columnName}]: Invalid relation type (${property.relType})")

            "varchar(36)"
        }
        else -> throw IllegalArgumentException("Column [$tableName.${property.columnName}]: Invalid property type (${property.type})")
    }

    private fun addCreateIndexChange(changeSet: ChangeSet, tableName: String, property: Property) {
        val createIndexChange = newCreateIndexChange(tableName, property)
        changeSet.addChange(createIndexChange)
    }

    private fun newCreateIndexChange(tableName: String, property: Property) = CreateIndexChange().apply {
        this.tableName = tableName
        this.indexName = "${tableName}_${property.columnName}_idx"
        this.addColumn(AddColumnConfig().apply {
            this.name = property.columnName
        })
    }

    private fun newCreateIndexChange(tableName: String, indexName: String, index: Index) = CreateIndexChange().apply {
        this.tableName = tableName
        this.indexName = indexName
        this.isUnique = index.unique
        this.columns = index.columns.map {
            AddColumnConfig().apply { this.name = it }
        }
    }

    private fun addCreateIndexChange(changeSet: ChangeSet, tableName: String, indexName: String, index: Index) {
        val createIndexChange = newCreateIndexChange(tableName, indexName, index)
        changeSet.addChange(createIndexChange)
    }

    private fun isChanged(item: Item, itemEntity: ItemEntity): Boolean =
        item.hashCode().toString() != itemEntity.checksum &&
            (item.metadata.tableName != itemEntity.tableName || item.spec.hashCode() != itemEntity.spec.hashCode())

    private fun updateTable(item: Item, itemEntity: ItemEntity) {
        val metadata = item.metadata

        val databaseChangeLog = DatabaseChangeLog()
        val changeSet = addChangeSet(databaseChangeLog, "update-${metadata.tableName}")

        if (metadata.tableName != itemEntity.tableName && item.spec.hashCode() == itemEntity.spec.hashCode()) {
            addRenameTableChange(changeSet, itemEntity.tableName, metadata.tableName) // rename table
        } else {
            addDropTableChange(changeSet, itemEntity.tableName, false) // drop table

            addCreateTableChange(changeSet, item) // create table
        }

        // Run changelog
        val liquibase = newLiquibase(databaseChangeLog)
        liquibase.update("")
    }

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

    private fun newLiquibase(databaseChangeLog: DatabaseChangeLog) = Liquibase(
        databaseChangeLog,
        null,
        DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
            JdbcConnection(dataSource.connection)
        )
    )

    companion object {
        private val logger = LoggerFactory.getLogger(LiquibaseItemSeeder::class.java)
    }
}