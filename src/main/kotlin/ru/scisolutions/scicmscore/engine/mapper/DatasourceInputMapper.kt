package ru.scisolutions.scicmscore.engine.mapper

import ru.scisolutions.scicmscore.engine.model.input.DatasourceTablesInput
import ru.scisolutions.scicmscore.engine.model.input.PaginationInput

class DatasourceInputMapper() {
    fun map(arguments: Map<String, Any?>): DatasourceTablesInput {
        val q = arguments[Q_ARG_NAME] as String?
        val paginationMap = arguments[PAGINATION_ARG_NAME] as Map<String, Int>?

        return DatasourceTablesInput(
            q = q,
            pagination = paginationMap?.let { PaginationInput.fromMap(it) }
        )
    }
    companion object {
        const val Q_ARG_NAME = "q"
        const val PAGINATION_ARG_NAME = "pagination"
    }
}