package ru.scisolutions.scicmscore.engine.handler

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.model.DatasetRec
import ru.scisolutions.scicmscore.engine.model.DatasourceType
import ru.scisolutions.scicmscore.engine.model.input.DatasetInput
import ru.scisolutions.scicmscore.engine.model.response.CacheStatistic
import ru.scisolutions.scicmscore.engine.model.response.DatasetResponse
import ru.scisolutions.scicmscore.engine.model.response.ResponseCollectionMeta
import ru.scisolutions.scicmscore.engine.persistence.dao.DatasetDao
import ru.scisolutions.scicmscore.engine.persistence.entity.Dataset
import ru.scisolutions.scicmscore.engine.persistence.query.DatasetQueryBuilder
import ru.scisolutions.scicmscore.engine.persistence.query.DatasetSqlParameterSource
import ru.scisolutions.scicmscore.engine.persistence.service.DatasetService
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Row
import java.io.File
import kotlin.collections.Map
import kotlin.collections.List
import kotlin.collections.mutableMapOf
import org.apache.poi.ss.usermodel.CellType
import ru.scisolutions.scicmscore.engine.model.input.DatasetFieldInput
import ru.scisolutions.scicmscore.engine.model.input.DatasetFiltersInput
import ru.scisolutions.scicmscore.engine.model.input.PrimitiveFilterInput

@Service
class DatasetHandler(
    private val datasetService: DatasetService,
    private val datasetQueryBuilder: DatasetQueryBuilder,
    private val datasetDao: DatasetDao
) {
    fun load(datasetName: String, input: DatasetInput): DatasetResponse {
        val dataset = datasetService.findByNameForRead(datasetName) ?: return DatasetResponse()

        return when (dataset.datasource?.sourceType) {
            DatasourceType.SPREADSHEET -> {
                loadExcelData(datasetName, input)
                TODO("CSV file processing")
            }
            DatasourceType.CSV -> {
                TODO("CSV file processing")
            }
            else -> {
                load(dataset, input)
            }
        }
    }

    /**
     * Fetches data from DBMS.
     */
    private fun load(dataset: Dataset, input: DatasetInput): DatasetResponse {
        val sourceType = dataset.datasource?.sourceType
        if (sourceType != DatasourceType.DATABASE)
            throw IllegalArgumentException("Unsupported source type: ${sourceType?.name}.")

        val paramSource = DatasetSqlParameterSource()
        val loadQuery = datasetQueryBuilder.buildLoadQuery(dataset, input, paramSource)
        val res: CacheStatistic<List<DatasetRec>> = datasetDao.load(dataset, loadQuery.sql, paramSource)

        return DatasetResponse(
            data = res.result,
            query = loadQuery.sql,
            params = paramSource.parameterNames.associateWith { paramSource.getValue(it) },
            timeMs = res.timeMs,
            cacheHit = res.cacheHit,
            meta =
                ResponseCollectionMeta(
                    pagination = loadQuery.pagination
                )
        )
    }

    fun loadExcelData(datasetName: String, input: DatasetInput): DatasetResponse {
        val workbook = WorkbookFactory.create(File(datasetName))
        val sheet = workbook.getSheet(datasetName) ?: throw IllegalArgumentException("Sheet $datasetName not found")

        val headers = readHeaders(sheet)
        val rows = readRows(sheet, headers)

        val processedData = processData(rows, input)

        return DatasetResponse(
            data = processedData,
            meta = ResponseCollectionMeta(pagination = null)
        )
    }

    private fun readHeaders(sheet: Sheet): List<String> {
        val headerRow = sheet.getRow(0) ?: return emptyList()
        val headers = mutableListOf<String>()
        var colIndex = 0

        while (true) {
            val cell = headerRow.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)
            if (cell == null) break

            val headerName = when (cell.cellType) {
                CellType.STRING -> cell.stringCellValue
                CellType.NUMERIC -> cell.numericCellValue.toString()
                CellType.BOOLEAN -> cell.booleanCellValue.toString()
                else -> "Column_${colIndex + 1}"
            }.trim()

            headers.add(headerName)
            colIndex++
        }

        return headers.distinct()
    }

    private fun readRows(sheet: Sheet, headers: List<String>): List<Map<String, Any?>> {
        val rows = mutableListOf<Map<String, Any?>>()

        for (rowIndex in 1 until sheet.physicalNumberOfRows) {
            val row = sheet.getRow(rowIndex) ?: continue
            val rowData = mutableMapOf<String, Any?>()

            headers.forEachIndexed { colIndex, headerName ->
                val cell = row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)
                rowData[headerName] = when (cell?.cellType) {
                    CellType.STRING -> cell.stringCellValue
                    CellType.NUMERIC -> cell.numericCellValue
                    CellType.BOOLEAN -> cell.booleanCellValue
                    CellType.FORMULA -> cell.cellFormula
                    else -> null
                }
            }

            rows.add(rowData)
        }

        return rows
    }

    private fun processData(rows: List<Map<String, Any?>>, input: DatasetInput): List<DatasetRec> {
        var result = rows

        input.filters?.let { result = applyFilters(result, it) }

        result = applyAggregations(result, input.fields)

        return result.map { DatasetRec(it) }
    }

    private fun applyFilters(
        rows: List<Map<String, Any?>>,
        filter: DatasetFiltersInput
    ): List<Map<String, Any?>> {
        val filtered = when {
            filter.andFilterList != null -> {
                filter.andFilterList.fold(rows) { acc, f ->
                    applyFilters(acc, f)
                }
            }

            filter.orFilterList != null -> {
                rows.filter { row ->
                    filter.orFilterList.any { orFilter ->
                        applyFilters(listOf(row), orFilter).isNotEmpty()
                    }
                }
            }

            filter.notFilter != null -> {
                val excluded = applyFilters(rows, filter.notFilter)
                rows.minus(excluded.toSet())
            }

            else -> {
                rows.filter { row ->
                    filter.fieldFilters.all { (fieldName, filterInput) ->
                        checkFieldCondition(
                            value = row[fieldName],
                            filter = filterInput
                        )
                    }
                }
            }
        }

        return filtered
    }

    private fun checkFieldCondition(value: Any?, filter: PrimitiveFilterInput): Boolean {
        return filter.operators.entries.all { (operator, expected) ->
            when (operator) {
                "\$eq" -> value == expected
                "\$ne" -> value != expected
                "\$gte" -> (value as? Comparable<Any>)?.compareTo(expected) >= 0
                "\$gt" -> (value as? Comparable<Any>)?.compareTo(expected) > 0
                "\$lte" -> (value as? Comparable<Any>)?.compareTo(expected) <= 0
                "\$lt" -> (value as? Comparable<Any>)?.compareTo(expected) < 0
                "\$in" -> (expected as? Collection<*>)?.contains(value) ?: false
                "\$nin" -> !(expected as? Collection<*>)?.contains(value) ?: false
                "\$regex" -> (value as? String)?.matches(expected.toString().toRegex()) ?: false
                else -> throw IllegalArgumentException("Unsupported operator: $operator")
            }
        }
    }

    private fun checkCondition(value: Any?, condition: Any): Boolean {
        return when (condition) {
            is Map<*, *> -> {
                condition.entries.all { (operator, expected) ->
                    when (operator as String) {
                        "\$gte" -> (value as? Comparable<Any>)?.compareTo(expected) >= 0
                        "\$lte" -> (value as? Comparable<Any>)?.compareTo(expected) <= 0
                        "\$eq" -> value == expected
                        else -> throw IllegalArgumentException("Unsupported operator: $operator")
                    }
                }
            }
            else -> value == condition
        }
    }

    private fun applyAggregations(rows: List<Map<String, Any?>>, fields: List<DatasetFieldInput>?): List<Map<String, Any?>> {
        val aggregationFields = fields.filter { it.aggregate != null }

        return if (aggregationFields.isNotEmpty()) {
            val groupingFields = fields.filter { it.aggregate == null }.map { it.name }
            rows.groupBy { row -> groupingFields.map { row[it] } }
                .map { (groupKey, groupRows) ->
                    mutableMapOf<String, Any?>().apply {
                        groupingFields.forEachIndexed { index, field ->
                            put(field, groupKey[index])
                        }
                        aggregationFields.forEach { field ->
                            put(field.name, calculateAggregate(field, groupRows))
                        }
                    }
                }
        } else {
            rows.map { row ->
                fields.associate { field ->
                    field.name to row[field.name]
                }
            }
        }
    }

    private fun calculateAggregate(field: DatasetFieldInput, rows: List<Map<String, Any?>>): Any? {
        val sourceField = field.source ?: return null
        return when (field.aggregate) {
            "count" -> rows.count { it[sourceField] != null }
            "sum" -> rows.sumOf { (it[sourceField] as? Number)?.toDouble() ?: 0.0 }
            "avg" -> rows.mapNotNull { (it[sourceField] as? Number)?.toDouble() }.average()
            else -> throw IllegalArgumentException("Unsupported aggregate: ${field.aggregate}")
        }
    }
}
