package ru.scisolutions.scicmscore.engine.schema.model

data class ItemTemplateMetadata(
    override val name: String,
    val core: Boolean = false,
) : BaseMetadata(name)
