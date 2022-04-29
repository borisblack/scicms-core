package ru.scisolutions.scicmscore.engine.schema.model

import ru.scisolutions.scicmscore.domain.model.ItemSpec

data class Item(
    override val coreVersion: String,
    val includeTemplates: Set<String>,
    override val metadata: ItemMetadata,
    val spec: ItemSpec
) : AbstractModel(coreVersion, metadata) {
    fun includeTemplate(itemTemplate: ItemTemplate) = Item(
        coreVersion = this.coreVersion,
        includeTemplates = this.includeTemplates,
        metadata = this.metadata,
        spec = this.spec.merge(itemTemplate.spec)
    )

    companion object {
        const val KIND = "Item"
    }
}