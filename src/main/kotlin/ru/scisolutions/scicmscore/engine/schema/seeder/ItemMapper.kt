package ru.scisolutions.scicmscore.engine.schema.seeder

import ru.scisolutions.scicmscore.engine.schema.model.Item
import ru.scisolutions.scicmscore.persistence.converter.ItemImplementationConverter
import ru.scisolutions.scicmscore.persistence.entity.Item as ItemEntity

class ItemMapper {
    fun map(source: Item): ItemEntity {
        val metadata = source.metadata
        val target = ItemEntity(
            name = metadata.name,
            pluralName = metadata.pluralName
        )
        copy(source, target)

        return target
    }

    fun copy(source: Item, target: ItemEntity) {
        val metadata = source.metadata
        
        target.name = metadata.name
        target.displayName = metadata.displayName
        target.singularName = metadata.singularName
        target.pluralName = metadata.pluralName
        target.tableName = metadata.tableName
        target.description = metadata.description
        target.dataSource = metadata.dataSource ?: ru.scisolutions.scicmscore.persistence.entity.Item.DEFAULT_DATASOURCE
        target.icon = metadata.icon
        target.core = metadata.core
        target.performDdl = metadata.performDdl
        target.versioned = metadata.versioned
        target.manualVersioning = metadata.manualVersioning
        target.revisionPolicyId = metadata.revisionPolicy
        target.notLockable = metadata.notLockable
        target.localized = metadata.localized
        target.implementation = itemImplementationConverter.convertToEntityAttribute(metadata.implementation)
        target.spec = source.spec
        target.checksum = source.hashCode().toString()
    }

    companion object {
        private val itemImplementationConverter = ItemImplementationConverter()
    }
}