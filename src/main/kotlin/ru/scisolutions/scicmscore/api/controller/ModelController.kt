package ru.scisolutions.scicmscore.api.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.scisolutions.scicmscore.api.graphql.ReloadIndicator
import ru.scisolutions.scicmscore.persistence.service.SchemaLockService
import ru.scisolutions.scicmscore.schema.applier.ModelsApplier
import ru.scisolutions.scicmscore.schema.model.AbstractModel

@RestController
@RequestMapping("/api/model")
class ModelController(
    private val modelsApplier: ModelsApplier,
    private val schemaLockService: SchemaLockService,
    private val reloadIndicator: ReloadIndicator
) {
    @PostMapping("/apply")
    fun apply(@RequestBody model: AbstractModel) {
        model.checksum = null

        schemaLockService.lockOrThrow()
        modelsApplier.apply(model)
        schemaLockService.unlockOrThrow()

        reloadIndicator.setNeedReloadOnce(true)
    }
}