package ru.scisolutions.scicmscore.api.controller

import com.qs.core.QS
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.scisolutions.scicmscore.engine.Engine
import ru.scisolutions.scicmscore.engine.mapper.DatasetInputMapper
import ru.scisolutions.scicmscore.engine.mapper.DatasetInputMapper.Companion.FIELDS_ARG_NAME
import ru.scisolutions.scicmscore.engine.mapper.DatasetInputMapper.Companion.FILTERS_ARG_NAME
import ru.scisolutions.scicmscore.engine.mapper.DatasetInputMapper.Companion.PAGINATION_ARG_NAME
import ru.scisolutions.scicmscore.engine.mapper.DatasetInputMapper.Companion.SORT_ARG_NAME
import ru.scisolutions.scicmscore.engine.model.response.DatasetResponse

@RestController
@RequestMapping("/api/dataset")
class DatasetController(
    private val engine: Engine
) {
    @GetMapping("/{datasetName}")
    fun load(req: HttpServletRequest, @PathVariable("datasetName") datasetName: String): DatasetResponse {
        val qsObject = QS.parse(req.queryString ?: "")
        val input = datasetInputMapper.map(qsObject.filterKeys { it in datasetInputKeys }, "$")

        return engine.loadDataset(datasetName, input)
    }

    companion object {
        private val datasetInputKeys =
            setOf(
                FILTERS_ARG_NAME,
                FIELDS_ARG_NAME,
                PAGINATION_ARG_NAME,
                SORT_ARG_NAME
            )
        private val datasetInputMapper = DatasetInputMapper()
    }
}
