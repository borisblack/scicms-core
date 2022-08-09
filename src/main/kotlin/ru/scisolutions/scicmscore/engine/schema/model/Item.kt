package ru.scisolutions.scicmscore.engine.schema.model

import ru.scisolutions.scicmscore.domain.model.ItemSpec
import java.util.Objects

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

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as Item

        return coreVersion == other.coreVersion &&
            metadata == other.metadata &&
            includeTemplates == other.includeTemplates &&
            spec == other.spec
    }

    override fun hashCode(): Int =
        Objects.hash(
            coreVersion,
            metadata,
            includeTemplates,
            spec
        )

    override fun toString(): String {
        return "Item(name=[${metadata.name}])"
    }

    companion object {
        const val KIND = "Item"
    }
}