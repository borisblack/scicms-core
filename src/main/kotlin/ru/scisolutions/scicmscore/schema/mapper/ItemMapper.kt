package ru.scisolutions.scicmscore.schema.mapper

import ru.scisolutions.scicmscore.schema.model.Item
import ru.scisolutions.scicmscore.persistence.entity.Permission
import ru.scisolutions.scicmscore.persistence.entity.Item as ItemEntity

class ItemMapper {
    fun map(source: Item): ItemEntity {
        val metadata = source.metadata
        val target = ItemEntity(
            name = metadata.name,
            pluralName = metadata.pluralName,
            dataSource = metadata.dataSource
        )
        copy(source, target)

        return target
    }

    fun copy(source: Item, target: ItemEntity) {
        val metadata = source.metadata
        
        target.name = metadata.name
        target.displayName = metadata.displayName
        target.pluralName = metadata.pluralName
        target.displayPluralName = metadata.displayPluralName
        target.dataSource = metadata.dataSource
        target.tableName = metadata.tableName
        target.titleAttribute = metadata.titleAttribute
        target.description = metadata.description
        target.icon = metadata.icon
        target.core = metadata.core
        target.performDdl = metadata.performDdl
        target.versioned = metadata.versioned
        target.manualVersioning = metadata.manualVersioning
        target.localized = metadata.localized
        target.revisionPolicyId = metadata.revisionPolicy
        target.lifecycleId = metadata.lifecycle
        target.permissionId = metadata.permission ?: Permission.DEFAULT_PERMISSION_ID.toString()
        target.implementation = metadata.implementation
        target.notLockable = metadata.notLockable
        target.spec = source.spec

        // Update the checksum only if it's a change from a file
        if (source.checksum != null)
            target.checksum = source.checksum

        target.hash = source.hashCode().toString()
    }
}