package ru.scisolutions.scicmscore.schema.mapper

import ru.scisolutions.scicmscore.persistence.entity.Lifecycle
import ru.scisolutions.scicmscore.persistence.entity.Permission
import ru.scisolutions.scicmscore.schema.model.ItemTemplate
import ru.scisolutions.scicmscore.persistence.entity.ItemTemplate as ItemTemplateEntity

class ItemTemplateMapper {
    fun map(source: ItemTemplate): ItemTemplateEntity {
        val metadata = source.metadata
        val target = ItemTemplateEntity(
            name = metadata.name,
        )
        copy(source, target)

        return target
    }

    fun copy(source: ItemTemplate, target: ItemTemplateEntity) {
        val metadata = source.metadata
        
        target.name = metadata.name
        target.core = metadata.core
        target.lifecycleId = Lifecycle.DEFAULT_LIFECYCLE_ID
        target.permissionId = Permission.DEFAULT_PERMISSION_ID
        target.spec = source.spec

        // Update the checksum only if it's a change from a file
        if (source.checksum != null)
            target.checksum = source.checksum

        target.hash = source.hashCode().toString()
    }
}