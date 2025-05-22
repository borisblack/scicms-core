package ru.scisolutions.scicmscore.engine.schema.model

import ru.scisolutions.scicmscore.engine.model.ItemSpec
import java.util.Objects

class ItemTemplate(
    override val coreVersion: String,
    override val metadata: ItemTemplateMetadata,
    override var checksum: String?,
    override val spec: ItemSpec
) : AbstractModel(coreVersion, metadata, checksum, spec) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (javaClass != other?.javaClass) {
            return false
        }

        other as ItemTemplate

        return coreVersion == other.coreVersion &&
            metadata == other.metadata &&
            // checksum == other.checksum && // ignore
            spec == other.spec
    }

    override fun hashCode(): Int = Objects.hash(
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
