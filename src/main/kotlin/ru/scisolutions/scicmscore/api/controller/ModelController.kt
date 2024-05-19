package ru.scisolutions.scicmscore.api.controller

import org.springframework.web.bind.annotation.*
import ru.scisolutions.scicmscore.api.graphql.ReloadIndicator
import ru.scisolutions.scicmscore.engine.Engine
import ru.scisolutions.scicmscore.engine.model.input.DeleteInput
import ru.scisolutions.scicmscore.engine.model.input.DeleteInput.DeletingStrategy
import ru.scisolutions.scicmscore.engine.persistence.service.SchemaLockService
import ru.scisolutions.scicmscore.engine.schema.applier.ModelsApplier
import ru.scisolutions.scicmscore.engine.schema.model.AbstractModel

@RestController
@RequestMapping("/api/model")
class ModelController(
    private val schemaLockService: SchemaLockService,
    private val modelsApplier: ModelsApplier,
    private val reloadIndicator: ReloadIndicator,
    private val engine: Engine
) {
    @PostMapping("/apply")
    fun apply(@RequestBody model: AbstractModel): String {
        model.checksum = null

        schemaLockService.lockOrThrow()
        val appliedModelResult = modelsApplier.apply(model, true)
        schemaLockService.unlockOrThrow()

        return appliedModelResult.id
    }

    @PostMapping("/lock/{modelName}/{id}")
    fun lock(@PathVariable("modelName") modelName: String, @PathVariable("id") id: String) {
        engine.lock(modelName, id, emptySet())
    }

    @PostMapping("/unlock/{modelName}/{id}")
    fun unlock(@PathVariable("modelName") modelName: String, @PathVariable("id") id: String) {
        engine.unlock(modelName, id, emptySet())
    }

    @PostMapping("/delete/{modelName}/{id}")
    fun delete(@PathVariable("modelName") modelName: String, @PathVariable("id") id: String) {
        val deleteInput = DeleteInput(
            id = id,
            deletingStrategy = DeletingStrategy.CASCADE
        )
        engine.delete(modelName, deleteInput, emptySet())
        reloadIndicator.setNeedReload(true)
    }
}