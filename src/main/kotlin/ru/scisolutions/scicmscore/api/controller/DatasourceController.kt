package ru.scisolutions.scicmscore.api.controller

import com.qs.core.QS
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.scisolutions.scicmscore.engine.Engine
import ru.scisolutions.scicmscore.engine.mapper.DatasourceInputMapper
import ru.scisolutions.scicmscore.engine.mapper.DatasourceInputMapper.Companion.PAGINATION_ARG_NAME
import ru.scisolutions.scicmscore.engine.mapper.DatasourceInputMapper.Companion.Q_ARG_NAME
import ru.scisolutions.scicmscore.engine.model.response.DatasourceTablesResponse

@RestController
@RequestMapping("/api/datasource")
class DatasourceController(
    private val engine: Engine
) {
    @GetMapping("/{datasourceName}/tables")
    fun loadTables(
        req: HttpServletRequest,
        @PathVariable("datasourceName") datasourceName: String
    ): DatasourceTablesResponse {
        val qsObject = QS.parse(req.queryString ?: "")
        val input = datasourceInputMapper.map(qsObject.filterKeys { it in  datasourceInputKeys})

        return engine.loadDatasourceTables(datasourceName, input)
    }

    companion object {
        private val datasourceInputKeys = setOf(
            Q_ARG_NAME,
            PAGINATION_ARG_NAME
        )
        private val datasourceInputMapper = DatasourceInputMapper()
    }
}