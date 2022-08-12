package ru.scisolutions.scicmscore.engine.schema.model

data class ItemMetadata(
    override val name: String,
    val displayName: String = name,
    val displayAttrName: String? = null,
    val pluralName: String,
    val tableName: String = whitespaceRegex.replace(pluralName.lowercase(), "_"),
    val description: String? = null,
    val dataSource: String,
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
        private val whitespaceRegex = "\\s+".toRegex()
    }
}