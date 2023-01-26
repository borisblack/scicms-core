package ru.scisolutions.scicmscore.api.controller

import com.qs.core.QS
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.scisolutions.scicmscore.engine.dao.DatasetDao
import ru.scisolutions.scicmscore.engine.mapper.DatasetInputMapper
import ru.scisolutions.scicmscore.engine.mapper.DatasetInputMapper.Companion.FILTERS_ARG_NAME
import ru.scisolutions.scicmscore.engine.mapper.DatasetInputMapper.Companion.PAGINATION_ARG_NAME
import ru.scisolutions.scicmscore.engine.mapper.DatasetInputMapper.Companion.SORT_ARG_NAME
import ru.scisolutions.scicmscore.model.AggregateType
import ru.scisolutions.scicmscore.persistence.service.DatasetService
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/dataset")
class DatasetController(
    private val datasetService: DatasetService,
    private val datasetDao: DatasetDao
) {
    @GetMapping("/{datasetName}")
    fun load(
        req: HttpServletRequest,
        @PathVariable("datasetName") datasetName: String,
        @RequestParam(name = "start", required = false) start: String?,
        @RequestParam(name = "end", required = false) end: String?,
        @RequestParam(name = "aggregate", required = false) aggregateType: AggregateType?,
        @RequestParam(name = "groupBy", required = false) groupBy: String?,
    ): ResponseEntity<*> {
        val qsObject = QS.parse(req.queryString)
        val dataset = datasetService.findByNameForRead(datasetName) ?: return ResponseEntity.notFound().build<Unit>()
        val input = datasetInputMapper.map(qsObject.filterKeys { it in  datasetInputKeys})

        return ResponseEntity.ok(datasetDao.load(dataset, start, end, aggregateType, groupBy))
    }

    companion object {
        private val datasetInputKeys = setOf(FILTERS_ARG_NAME, PAGINATION_ARG_NAME, SORT_ARG_NAME)
        private val datasetInputMapper = DatasetInputMapper()
    }
}