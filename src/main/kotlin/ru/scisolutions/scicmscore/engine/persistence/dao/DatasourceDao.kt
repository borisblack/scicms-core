package ru.scisolutions.scicmscore.engine.persistence.dao

import com.healthmarketscience.sqlbuilder.SelectQuery
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.engine.model.Column
import ru.scisolutions.scicmscore.engine.model.FieldType
import ru.scisolutions.scicmscore.engine.model.Table
import ru.scisolutions.scicmscore.engine.model.input.DatasourceTablesInput
import ru.scisolutions.scicmscore.engine.model.response.DatasourceTablesResponse
import ru.scisolutions.scicmscore.engine.persistence.mapper.ColumnsMapper
import ru.scisolutions.scicmscore.engine.persistence.mapper.TablesMapper
import ru.scisolutions.scicmscore.engine.service.DatasourceManager
import java.io.File


private fun getColumnType(sheet: Sheet, columnIndex: Int): FieldType {
    for (rowIndex in 1 until sheet.physicalNumberOfRows) {
        val row = sheet.getRow(rowIndex)
        val cell = row?.getCell(columnIndex)

        if (cell != null) {
            return when (cell.cellType) {
                CellType.STRING -> FieldType.string
                CellType.NUMERIC -> FieldType.float
                CellType.BOOLEAN -> FieldType.bool
                else -> FieldType.unknown
            }
        }
    }
    return FieldType.unknown
}

@Service
class DatasourceDao(
    dataProps: DataProps,
    private val dsManager: DatasourceManager
) {
    private val tablesMapper = TablesMapper(dataProps)

    fun loadMetaData(datasource: String, queryString: String): SqlRowSetMetaData {
        val spec = DbSpec()
        val schema: DbSchema = spec.addDefaultSchema()
        val table = schema.addTable(queryString)
        val query =
            SelectQuery()
                .addAllColumns()
                .addFromTable(table)
                .setFetchNext(1)
                .validate()

        val jdbcTemplate = dsManager.template(datasource)
        val sql = query.toString()
        logger.debug("Running loadMetaData SQL: {}", sql)
        return jdbcTemplate.queryForRowSet(sql, MapSqlParameterSource()).metaData
    }

    fun loadTables(datasource: String, input: DatasourceTablesInput): DatasourceTablesResponse {
        val dataSource = dsManager.dataSource(datasource)
        dataSource.connection.use {
            val metaData = it.metaData

            logger.debug("Fetching metaData tables")
            val tablesResultSet =
                metaData.getTables(
                    it.catalog,
                    if (input.schema.isNullOrBlank()) it.schema else input.schema,
                    null,
                    arrayOf("TABLE")
                )
            logger.debug("Fetched metaData tables.")

            val response =
                tablesMapper.map(tablesResultSet, input) { tableName ->
                    columnsMapper.map(loadMetaData(datasource, tableName))
                }

            return response
        }
    }

    fun loadExcelTables(filePath: String, input: DatasourceTablesInput): DatasourceTablesResponse {
        val logger = LoggerFactory.getLogger(this::class.java)

        val file = File(filePath)
        if (!file.exists()) {
            throw IllegalArgumentException("File not found: $filePath")
        }

        val workbook = WorkbookFactory.create(file)
        val tableList = mutableListOf<Table>()
        logger.debug("Fetching Excel file sheets")
        for (sheetIndex in 0 until workbook.numberOfSheets) {
            val sheet: Sheet = workbook.getSheetAt(sheetIndex)

            val metadata = mutableMapOf<String, Any>()
            metadata["sheetName"] = sheet.sheetName
            metadata["rowCount"] = sheet.physicalNumberOfRows
            metadata["columnCount"] = sheet.getRow(0)?.physicalNumberOfCells ?: 0
            metadata["isHidden"] = sheet.sheetHidden


            val headerRow = sheet.getRow(0) // заголовок
            val numberOfColumns = headerRow?.physicalNumberOfCells ?: 0
            val columns = mutableMapOf<String, Column>()

            for (colIndex in 0 until numberOfColumns) {
                val columnName = headerRow?.getCell(colIndex)?.stringCellValue ?: "Unknown"
                val col = Column(type = getColumnType(sheet, colIndex))
                columns[columnName] = col
            }
            tableList.add(Table(name = sheet.sheetName, columns = columns))
        }
        logger.debug("Fetched Excel file sheets.")
        val response = DatasourceTablesResponse(data = tableList)
        return response
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DatasourceDao::class.java)
        private val columnsMapper = ColumnsMapper()
    }

}

