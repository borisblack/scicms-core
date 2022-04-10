package ru.scisolutions.scicmscore.api.model

data class Item(
    override val coreVersion: String,
    val includeTemplates: Set<String>,
    override val metadata: ItemMetadata,
    val spec: Spec
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