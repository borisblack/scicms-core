package ru.scisolutions.scicmscore.schema.model

data class ItemTemplateMetadata(
    override val name: String,
    val core: Boolean = false
) : BaseMetadata(name)