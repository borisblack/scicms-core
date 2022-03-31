package ru.scisolutions.scicmscore.dbschema.model

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "kind"
)
@JsonSubTypes(value = [
    JsonSubTypes.Type(value = ItemTemplateModel::class, name = ItemTemplateModel.KIND),
    JsonSubTypes.Type(value = ItemModel::class, name = ItemModel.KIND)
])
abstract class AbstractModel(
    val coreVersion: String,
    open val metadata: Metadata
)