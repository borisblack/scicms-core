package ru.scisolutions.scicmscore.api.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.scisolutions.scicmscore.engine.dao.DatasetDao
import ru.scisolutions.scicmscore.persistence.service.DatasetService

@RestController
@RequestMapping("/api/dataset")
class DatasetController(
    private val datasetService: DatasetService,
    private val datasetDao: DatasetDao
) {
    @GetMapping("/{datasetName}")
    fun findAll(
        @PathVariable("datasetName") datasetName: String,
        @RequestParam(name = "start", required = false) start: String?,
        @RequestParam(name = "end", required = false) end: String?,
    ): ResponseEntity<*> {
        val dataset = datasetService.findByNameForRead(datasetName) ?: return ResponseEntity.notFound().build<Unit>()

        return ResponseEntity.ok(datasetDao.findAll(dataset, start, end))
    }
}