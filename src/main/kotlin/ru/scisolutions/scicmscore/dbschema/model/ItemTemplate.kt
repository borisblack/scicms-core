package ru.scisolutions.scicmscore.dbschema.model

class ItemTemplate(
    coreVersion: String,
    metadata: Metadata,
    val spec: Spec
) : AbstractModel(coreVersion, metadata) {
    companion object {
        const val KIND = "ItemTemplate"
    }
}