package ru.scisolutions.scicmscore.dbschema.model

class ItemMetadata(
    name: String,
    val dataSource: String = DEFAULT_DATASOURCE,
    val displayName: String = name,
    val singularName: String = name,
    val pluralName: String,
    val tableName: String = pluralName.lowercase(),
    val description: String? = null,
    val icon: String? = null,
    val core: Boolean = false,
    val performDdl: Boolean = true,
    val versioned: Boolean = false,
    val manualVersioning: Boolean = false,
    val revisionPolicy: String? = null,
    val notLockable: Boolean = false,
    val localized: Boolean = false,
    val implementation: String? = null
) : Metadata(name) {
    companion object {
        private const val DEFAULT_DATASOURCE = "main"
    }
}