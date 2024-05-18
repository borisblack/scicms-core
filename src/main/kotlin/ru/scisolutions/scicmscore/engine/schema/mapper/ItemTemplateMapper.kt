package ru.scisolutions.scicmscore.engine.schema.mapper

import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.config.props.AppProps
import ru.scisolutions.scicmscore.engine.model.ItemSpec
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemTemplateItemRec
import ru.scisolutions.scicmscore.engine.persistence.entity.Lifecycle
import ru.scisolutions.scicmscore.engine.persistence.entity.Permission
import ru.scisolutions.scicmscore.engine.schema.model.ItemTemplate
import ru.scisolutions.scicmscore.engine.schema.model.ItemTemplateMetadata
import ru.scisolutions.scicmscore.util.Json
import ru.scisolutions.scicmscore.engine.persistence.entity.ItemTemplate as ItemTemplateEntity

@Component
class ItemTemplateMapper(private val appProps: AppProps) {
    fun map(source: ItemTemplate): ItemTemplateEntity {
        val metadata = source.metadata
        val target = ItemTemplateEntity(
            name = metadata.name,
            pluralName = metadata.pluralName
        )
        copy(source, target)

        return target
    }

    fun copy(source: ItemTemplate, target: ItemTemplateEntity) {
        val metadata = source.metadata
        
        target.name = metadata.name
        target.pluralName = metadata.pluralName
        target.core = metadata.core
        target.lifecycleId = Lifecycle.DEFAULT_LIFECYCLE_ID
        target.permissionId = Permission.DEFAULT_PERMISSION_ID
        target.spec = source.spec

        // Update the checksum only if it's a change from a file
        if (source.checksum != null)
            target.checksum = source.checksum

        target.hash = source.hashCode().toString()
    }

    fun mapToModel(source: ItemTemplateItemRec): ItemTemplate = ItemTemplate(
        coreVersion = appProps.coreVersion,
        metadata = ItemTemplateMetadata(
            name = requireNotNull(source.name),
            pluralName = requireNotNull(source.pluralName),
            core = source.core ?: false
        ),
        spec = source.spec?.let { Json.objectMapper.convertValue(it, ItemSpec::class.java) } ?: ItemSpec(),
        checksum = null
    )
}