package ru.scisolutions.scicmscore.engine.schema.model

import ru.scisolutions.scicmscore.domain.model.ItemSpec

data class Item(
    override val coreVersion: String,
    override val metadata: ItemMetadata,
    override var checksum: String?,
    val includeTemplates: Set<String>,
    val spec: ItemSpec
) : AbstractModel(coreVersion, metadata, checksum) {
    fun includeTemplate(itemTemplate: ItemTemplate) = Item(
        coreVersion = this.coreVersion,
        metadata = this.metadata,
        checksum = this.checksum,
        includeTemplates = this.includeTemplates,
        spec = this.spec.merge(itemTemplate.spec)
    )

    companion object {
        const val KIND = "Item"
    }
}