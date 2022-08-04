package ru.scisolutions.scicmscore.engine.schema.mapper

import ru.scisolutions.scicmscore.engine.schema.model.Item
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
        target.displayAttrName = metadata.displayAttrName
        target.singularName = metadata.singularName
        target.pluralName = metadata.pluralName
        target.tableName = metadata.tableName
        target.description = metadata.description
        target.dataSource = metadata.dataSource
        target.icon = metadata.icon
        target.core = metadata.core
        target.performDdl = metadata.performDdl
        target.versioned = metadata.versioned
        target.manualVersioning = metadata.manualVersioning
        target.localized = metadata.localized
        target.revisionPolicyId = metadata.revisionPolicy
        target.lifecycleId = metadata.lifecycle
        target.permissionId = metadata.permission
        target.implementation = metadata.implementation
        target.notLockable = metadata.notLockable
        target.spec = source.spec
        target.checksum = source.hashCode().toString()
    }
}