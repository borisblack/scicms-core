package ru.scisolutions.scicmscore.engine.schema.model

import ru.scisolutions.scicmscore.engine.model.ItemSpec
import java.util.Objects

class Item(
    override val coreVersion: String,
    override val metadata: ItemMetadata,
    override var checksum: String?,
    val includeTemplates: LinkedHashSet<String>,
    val spec: ItemSpec
) : AbstractModel(coreVersion, metadata, checksum) {
    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as Item

        return coreVersion == other.coreVersion &&
            metadata == other.metadata &&
            // checksum == other.checksum && // ignore
            includeTemplates == other.includeTemplates &&
            spec == other.spec
    }

    override fun hashCode(): Int =
        Objects.hash(
            coreVersion,
            metadata,
            // checksum, // ignore
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