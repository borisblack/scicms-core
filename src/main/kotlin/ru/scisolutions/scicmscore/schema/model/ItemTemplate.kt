package ru.scisolutions.scicmscore.schema.model

import ru.scisolutions.scicmscore.model.ItemSpec

class ItemTemplate(
    coreVersion: String,
    metadata: BaseMetadata,
    val spec: ItemSpec
) : AbstractModel(coreVersion, metadata) {
    companion object {
        const val KIND = "ItemTemplate"
    }
}