package ru.scisolutions.scicmscore.api.model

data class ItemMetadata(
    override val name: String,
    val displayName: String = name,
    val singularName: String = name,
    val pluralName: String,
    val tableName: String = pluralName.lowercase(),
    val description: String? = null,
    val dataSource: String?,
    val icon: String? = null,
    val core: Boolean = false,
    val performDdl: Boolean = true,
    val versioned: Boolean = false,
    val manualVersioning: Boolean = false,
    val revisionPolicy: String? = null,
    val notLockable: Boolean = false,
    val localized: Boolean = false,
    val implementation: String? = null
) : Metadata(name)