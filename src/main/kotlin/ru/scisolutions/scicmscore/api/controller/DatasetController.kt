package ru.scisolutions.scicmscore.api.controller

import com.qs.core.QS
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.scisolutions.scicmscore.engine.dao.DatasetDao
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
    fun loadData(
        req: HttpServletRequest,
        @PathVariable("datasetName") datasetName: String,
        @RequestParam(name = "start", required = false) start: String?,
        @RequestParam(name = "end", required = false) end: String?,
        @RequestParam(name = "aggregate", required = false) aggregateType: AggregateType?,
        @RequestParam(name = "groupBy", required = false) groupBy: String?,
    ): ResponseEntity<*> {
        val qsObject = QS.parse(req.queryString)
        val dataset = datasetService.findByNameForRead(datasetName) ?: return ResponseEntity.notFound().build<Unit>()

        return ResponseEntity.ok(datasetDao.findAll(dataset, start, end, aggregateType, groupBy))
    }
}