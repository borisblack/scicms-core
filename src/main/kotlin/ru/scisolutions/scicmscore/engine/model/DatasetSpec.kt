package ru.scisolutions.scicmscore.engine.model

data class DatasetSpec(
    val columns: Map<String, Column> = emptyMap(),
    val sources: DatasetSources? = null,
    val useDesigner: Boolean? = null
) {
    fun getField(fieldName: String): Column = columns[fieldName] ?: throw IllegalArgumentException("Column [$fieldName] not found.")

    fun validate(): DatasetSpec {
        if (!isColumnsValid()) {
            throw IllegalArgumentException("Illegal dataset columns.")
        }

        return this
    }

    private fun isColumnsValid(): Boolean {
        if (!columns.values.all { (it.source == null && it.formula == null && it.aggregate == null) || it.custom }) {
            return false
        }

        if (!columns.values.filter { it.custom }.all { it.source != null || it.formula != null }) {
            return false
        }

        val ownColNames = columns.filterValues { !it.custom }.keys
        return columns.values.mapNotNull { it.source }.all { it in ownColNames }
    }
}
