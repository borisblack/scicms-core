package ru.scisolutions.scicmscore.api.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "kind"
)
@JsonSubTypes(value = [
    JsonSubTypes.Type(value = ItemTemplate::class, name = ItemTemplate.KIND),
    JsonSubTypes.Type(value = Item::class, name = Item.KIND)
])
abstract class AbstractModel(
    open val coreVersion: String,
    open val metadata: BaseMetadata
)