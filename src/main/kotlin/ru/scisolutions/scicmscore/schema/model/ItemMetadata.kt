package ru.scisolutions.scicmscore.schema.model

import ru.scisolutions.scicmscore.util.Schema

data class ItemMetadata(
    override val name: String,
    val displayName: String = name,
    override val pluralName: String,
    val displayPluralName: String = pluralName,
    val dataSource: String = Schema.MAIN_DATA_SOURCE_NAME,
    val tableName: String? = null,
    val query: String? = null,
    val titleAttribute: String = ID_ATTR_NAME,
    val description: String? = null,
    val readOnly: Boolean = false,
    val icon: String? = null,
    val core: Boolean = false,
    val performDdl: Boolean = true,
    val versioned: Boolean = false,
    val manualVersioning: Boolean = false,
    val localized: Boolean = false,
    val revisionPolicy: String? = null,
    val lifecycle: String? = null,
    val permission: String? = null,
    val implementation: String? = null,
    val notLockable: Boolean = false
) : BaseMetadata(name, pluralName) {
    companion object {
        const val ID_ATTR_NAME = "id"
    }
}