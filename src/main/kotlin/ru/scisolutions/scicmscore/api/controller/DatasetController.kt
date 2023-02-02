package ru.scisolutions.scicmscore.api.controller

import com.qs.core.QS
import org.springframework.web.bind.annotation.*
import ru.scisolutions.scicmscore.engine.Engine
import ru.scisolutions.scicmscore.engine.mapper.DatasetInputMapper
import ru.scisolutions.scicmscore.engine.mapper.DatasetInputMapper.Companion.AGGREGATE_ARG_NAME
import ru.scisolutions.scicmscore.engine.mapper.DatasetInputMapper.Companion.AGGREGATE_FIELD_ARG_NAME
import ru.scisolutions.scicmscore.engine.mapper.DatasetInputMapper.Companion.FIELDS_ARG_NAME
import ru.scisolutions.scicmscore.engine.mapper.DatasetInputMapper.Companion.FILTERS_ARG_NAME
import ru.scisolutions.scicmscore.engine.mapper.DatasetInputMapper.Companion.GROUP_FIELD_ARG_NAME
import ru.scisolutions.scicmscore.engine.mapper.DatasetInputMapper.Companion.PAGINATION_ARG_NAME
import ru.scisolutions.scicmscore.engine.mapper.DatasetInputMapper.Companion.SORT_ARG_NAME
import ru.scisolutions.scicmscore.engine.model.response.DatasetResponse
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/dataset")
class DatasetController(
    private val engine: Engine
) {
    @GetMapping("/{datasetName}")
    fun load(
        req: HttpServletRequest,
        @PathVariable("datasetName") datasetName: String
    ): DatasetResponse {
        val qsObject = QS.parse(req.queryString)
        val input = datasetInputMapper.map(qsObject.filterKeys { it in  datasetInputKeys}, "$")

        return engine.loadDataset(datasetName, input)
    }

    companion object {
        private val datasetInputKeys = setOf(
            FILTERS_ARG_NAME,
            FIELDS_ARG_NAME,
            PAGINATION_ARG_NAME,
            SORT_ARG_NAME,
            AGGREGATE_ARG_NAME,
            AGGREGATE_FIELD_ARG_NAME,
            GROUP_FIELD_ARG_NAME
        )
        private val datasetInputMapper = DatasetInputMapper()
    }
}