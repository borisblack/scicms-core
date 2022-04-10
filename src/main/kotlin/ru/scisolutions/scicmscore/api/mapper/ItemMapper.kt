package ru.scisolutions.scicmscore.api.mapper

import ru.scisolutions.scicmscore.api.model.Item
import ru.scisolutions.scicmscore.entity.Item as ItemEntity

class ItemMapper : Mapper<Item, ItemEntity> {
    override fun map(source: Item): ItemEntity {
        val metadata = source.metadata

        return ItemEntity(
            name = metadata.name,
            displayName = metadata.displayName,
            singularName = metadata.singularName,
            pluralName = metadata.pluralName,
            tableName = metadata.tableName,
            description = metadata.description,
            dataSource = metadata.dataSource ?: ItemEntity.DEFAULT_DATASOURCE,
            icon = metadata.icon,
            core = metadata.core,
            performDdl = metadata.performDdl,
            versioned = metadata.versioned,
            manualVersioning = metadata.manualVersioning,
            revisionPolicyId = metadata.revisionPolicy,
            notLockable = metadata.notLockable,
            localized = metadata.localized,
            implementation = metadata.implementation,
            spec = source.spec,
            checksum = source.hashCode().toString()
        )
    }
}