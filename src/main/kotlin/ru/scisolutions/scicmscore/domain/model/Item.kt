package ru.scisolutions.scicmscore.domain.model

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