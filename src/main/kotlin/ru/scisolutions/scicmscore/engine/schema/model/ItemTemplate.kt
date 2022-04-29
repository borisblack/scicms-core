package ru.scisolutions.scicmscore.engine.schema.model

import ru.scisolutions.scicmscore.domain.model.ItemSpec

class ItemTemplate(
    coreVersion: String,
    metadata: BaseMetadata,
    val spec: ItemSpec
) : AbstractModel(coreVersion, metadata) {
    companion object {
        const val KIND = "ItemTemplate"
    }
}