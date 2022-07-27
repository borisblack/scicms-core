package ru.scisolutions.scicmscore.engine.schema.model

data class ItemMetadata(
    override val name: String, // item name must start with a lowercase character!
    val displayName: String = name,
    val displayAttrName: String? = null,
    val singularName: String = name,
    val pluralName: String,
    val tableName: String = pluralName.lowercase(),
    val description: String? = null,
    val dataSource: String,
    val icon: String? = null,
    val core: Boolean = false,
    val performDdl: Boolean = true,
    val versioned: Boolean = false,
    val manualVersioning: Boolean = false,
    val revisionPolicy: String? = null,
    val notLockable: Boolean = false,
    val localized: Boolean = false,
    val implementation: String? = null
) : BaseMetadata(name)