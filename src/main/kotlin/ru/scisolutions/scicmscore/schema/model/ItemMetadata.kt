package ru.scisolutions.scicmscore.schema.model

data class ItemMetadata(
    override val name: String,
    val displayName: String = name,
    val pluralName: String,
    val displayPluralName: String = pluralName,
    val dataSource: String,
    val tableName: String = whitespaceRegex.replace(pluralName.lowercase(), "_"),
    val titleAttribute: String = ID_ATTR_NAME,
    val description: String? = null,
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
) : BaseMetadata(name) {
    companion object {
        private const val ID_ATTR_NAME = "id"

        private val whitespaceRegex = "\\s+".toRegex()
    }
}