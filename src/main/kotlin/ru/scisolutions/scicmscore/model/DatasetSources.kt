package ru.scisolutions.scicmscore.model

data class DatasetSources(
    val mainTable: Table? = null,
    val joinedTables: List<JoinedTable>
)
