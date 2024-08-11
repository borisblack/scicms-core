package ru.scisolutions.scicmscore.engine.model

data class DatasetSources(
    val mainTable: Table? = null,
    val joinedTables: List<JoinedTable>
)
