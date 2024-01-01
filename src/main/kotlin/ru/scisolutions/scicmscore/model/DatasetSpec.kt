package ru.scisolutions.scicmscore.model

data class DatasetSpec(
    val columns: Map<String, Column> = emptyMap(),
    val sources: DatasetSources? = null
) {
    fun getColumn(columnName: String): Column =
        columns[columnName] ?: throw IllegalArgumentException("Column [$columnName] not found.")

    fun getSource(columnName: String): String =
        getColumn(columnName).source ?: columnName
}