package ru.scisolutions.scicmscore.engine.persistence.mapper

import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.engine.model.Column
import ru.scisolutions.scicmscore.engine.model.Table
import ru.scisolutions.scicmscore.engine.model.input.DatasourceTablesInput
import ru.scisolutions.scicmscore.engine.model.response.DatasourceTablesResponse
import ru.scisolutions.scicmscore.engine.model.response.ListPagination
import ru.scisolutions.scicmscore.engine.model.response.ResponseCollectionMeta
import ru.scisolutions.scicmscore.engine.persistence.paginator.ListPaginator
import java.sql.ResultSet

class TablesMapper(dataProps: DataProps) {
    private val paginator = ListPaginator(dataProps)

    fun map(
        tablesResultSet: ResultSet,
        input: DatasourceTablesInput,
        getColumns: (tableName: String) -> Map<String, Column>
    ): DatasourceTablesResponse {
        val tableNamesPagination = extractTableNames(tablesResultSet, input)
        val tables =
            tableNamesPagination.list.map {
                Table(
                    name = if (input.schema.isNullOrBlank()) it else "${input.schema.lowercase()}.$it",
                    columns = getColumns(it)
                )
            }

        return DatasourceTablesResponse(
            data = tables,
            meta =
            ResponseCollectionMeta(
                pagination = tableNamesPagination.toBasePagination()
            )
        )
    }

    private fun extractTableNames(tablesResultSet: ResultSet, input: DatasourceTablesInput): ListPagination<String> {
        val tableNames = mutableListOf<String>()
        while (tablesResultSet.next()) {
            val tableName = tablesResultSet.getString(TABLE_NAME_INDEX).lowercase()
            if (input.q == null || tableName.contains(input.q, true)) {
                tableNames.add(tableName)
            }
        }

        return paginator.paginate(input.pagination, tableNames)
    }

    companion object {
        private const val TABLE_NAME_INDEX = 3
    }
}
