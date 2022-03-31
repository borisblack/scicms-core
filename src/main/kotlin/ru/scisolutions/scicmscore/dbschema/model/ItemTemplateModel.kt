package ru.scisolutions.scicmscore.dbschema.model

class ItemTemplateModel(
    coreVersion: String,
    metadata: Metadata,
    val spec: Spec
) : AbstractModel(coreVersion, metadata) {
    companion object {
        const val KIND = "ItemTemplate"
    }
}