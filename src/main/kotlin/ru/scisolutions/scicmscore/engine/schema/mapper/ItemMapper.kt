package ru.scisolutions.scicmscore.engine.schema.mapper

import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.extension.isUUID
import ru.scisolutions.scicmscore.engine.persistence.entity.Permission
import ru.scisolutions.scicmscore.engine.persistence.service.DatasourceService
import ru.scisolutions.scicmscore.engine.schema.model.Item
import ru.scisolutions.scicmscore.engine.schema.model.ItemMetadata
import ru.scisolutions.scicmscore.engine.persistence.entity.Item as ItemEntity

@Component
class ItemMapper(private val datasourceService: DatasourceService) {
    fun mapToEntity(source: Item): ItemEntity {
        val metadata = source.metadata
        val datasource = datasourceService.findByName(metadata.dataSource)
        val target = ItemEntity(
            name = metadata.name,
            pluralName = metadata.pluralName,
            datasourceId = datasource?.id,
            datasource = datasource
        )
        copyToEntity(source, target)

        return target
    }

    fun copyToEntity(source: Item, target: ItemEntity) {
        val metadata = source.metadata
        val datasource =
            if (metadata.dataSource.isUUID()) datasourceService.findById(metadata.dataSource)
            else datasourceService.findByName(metadata.dataSource)

        target.name = metadata.name
        target.displayName = metadata.displayName.ifBlank { metadata.name }
        target.pluralName = metadata.pluralName
        target.displayPluralName = metadata.displayPluralName.ifBlank { metadata.pluralName }
        target.datasourceId = datasource?.id
        target.datasource = datasource
        target.tableName = metadata.tableName
        target.query = metadata.query
        target.cacheTtl = metadata.cacheTtl
        target.titleAttribute = metadata.titleAttribute.ifBlank { ItemMetadata.ID_ATTR_NAME }
        target.defaultSortAttribute = metadata.defaultSortAttribute
        target.defaultSortOrder = metadata.defaultSortOrder
        target.includeTemplates = source.includeTemplates
        target.description = metadata.description
        target.readOnly = metadata.readOnly
        target.icon = metadata.icon
        target.core = metadata.core
        target.performDdl = metadata.performDdl
        target.versioned = metadata.versioned
        target.manualVersioning = metadata.manualVersioning
        target.localized = metadata.localized
        target.revisionPolicyId = metadata.revisionPolicy
        target.lifecycleId = metadata.lifecycle
        target.permissionId = metadata.permission ?: Permission.DEFAULT_PERMISSION_ID
        target.implementation = metadata.implementation
        target.notLockable = metadata.notLockable
        target.spec = source.spec

        // Update the checksum only if it's a change from a file
        if (source.checksum != null)
            target.checksum = source.checksum

        target.hash = source.hashCode().toString()
    }
}