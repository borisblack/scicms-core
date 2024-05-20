package ru.scisolutions.scicmscore.engine.schema.mapper

import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.config.props.AppProps
import ru.scisolutions.scicmscore.engine.model.ItemSpec
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemItemRec
import ru.scisolutions.scicmscore.extension.isUUID
import ru.scisolutions.scicmscore.engine.persistence.entity.Permission
import ru.scisolutions.scicmscore.engine.persistence.service.DatasourceService
import ru.scisolutions.scicmscore.engine.schema.model.Item
import ru.scisolutions.scicmscore.engine.schema.model.ItemMetadata
import ru.scisolutions.scicmscore.util.Json
import java.util.LinkedHashSet
import ru.scisolutions.scicmscore.engine.persistence.entity.Item as ItemEntity

@Component
class ItemMapper(
    private val appProps: AppProps,
    private val datasourceService: DatasourceService
) {
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
        target.idAttribute = metadata.idAttribute.ifBlank { ItemMetadata.ID_ATTR_NAME }
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

    fun mapToModel(source: ItemItemRec): Item = Item(
        coreVersion = appProps.coreVersion,
        includeTemplates = LinkedHashSet(requireNotNull(source.includeTemplates).toMutableList()),
        metadata = ItemMetadata(
            name = requireNotNull(source.name),
            displayName = requireNotNull(source.displayName ?: source.name),
            pluralName = requireNotNull(source.pluralName),
            displayPluralName = requireNotNull(source.displayPluralName ?: source.pluralName),
            dataSource = source.datasource ?: ItemMetadata.MAIN_DATASOURCE_NAME,
            performDdl = source.performDdl ?: false,
            tableName = source.tableName,
            query = source.query,
            cacheTtl = source.cacheTtl,
            idAttribute = source.idAttribute ?: ItemMetadata.ID_ATTR_NAME,
            titleAttribute = source.titleAttribute ?: ItemMetadata.ID_ATTR_NAME,
            defaultSortAttribute = source.defaultSortAttribute,
            defaultSortOrder = source.defaultSortOrder,
            description = source.description,
            icon = source.icon,
            core = source.core ?: false,
            readOnly = source.readOnly ?: false,
            versioned = source.versioned ?: false,
            manualVersioning = source.manualVersioning ?: false,
            localized = source.localized ?: false,
            notLockable = source.notLockable ?: false,
            implementation = source.implementation,
            revisionPolicy = source.revisionPolicy,
            lifecycle = source.lifecycle,
            permission = source.permission
        ),
        spec = source.spec?.let { Json.objectMapper.convertValue(it, ItemSpec::class.java) } ?: ItemSpec(),
        checksum = null
    )
}