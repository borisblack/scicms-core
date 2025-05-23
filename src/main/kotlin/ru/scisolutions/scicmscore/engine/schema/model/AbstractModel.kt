package ru.scisolutions.scicmscore.engine.schema.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import ru.scisolutions.scicmscore.engine.model.ItemSpec

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "kind"
)
@JsonSubTypes(
    value = [
        JsonSubTypes.Type(value = ItemTemplate::class, name = ItemTemplate.KIND),
        JsonSubTypes.Type(value = Item::class, name = Item.KIND)
    ]
)
abstract class AbstractModel(
    open val coreVersion: String,
    open val metadata: BaseMetadata,

    @JsonIgnore
    open var checksum: String? = null,

    open val spec: ItemSpec
)
