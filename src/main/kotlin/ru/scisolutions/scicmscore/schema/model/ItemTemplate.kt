package ru.scisolutions.scicmscore.schema.model

import ru.scisolutions.scicmscore.model.ItemSpec
import java.util.Objects

class ItemTemplate(
    coreVersion: String,
    metadata: BaseMetadata,
    val spec: ItemSpec
) : AbstractModel(coreVersion, metadata) {
    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true

        if (javaClass != other?.javaClass)
            return false

        other as ItemTemplate

        return coreVersion == other.coreVersion &&
            metadata == other.metadata &&
            // checksum == other.checksum && // ignore
            spec == other.spec
    }

    override fun hashCode(): Int =
        Objects.hash(
            coreVersion,
            metadata,
            // checksum, // ignore
            spec
        )

    override fun toString(): String {
        return "ItemTemplate(name=[${metadata.name}])"
    }

    companion object {
        const val KIND = "ItemTemplate"
    }
}