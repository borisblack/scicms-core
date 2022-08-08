package ru.scisolutions.scicmscore.api.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.scisolutions.scicmscore.api.graphql.ReloadIndicator
import ru.scisolutions.scicmscore.engine.schema.applier.ModelsApplier
import ru.scisolutions.scicmscore.engine.schema.model.AbstractModel
import ru.scisolutions.scicmscore.service.ItemLockService

@RestController
@RequestMapping("/api/model")
class ModelController(
    private val modelsApplier: ModelsApplier,
    private val itemLockService: ItemLockService,
    private val reloadIndicator: ReloadIndicator
) {
    @PostMapping("/apply")
    fun apply(@RequestBody model: AbstractModel) {
        itemLockService.lockOrThrow()
        modelsApplier.apply(model)
        itemLockService.unlockOrThrow()

        reloadIndicator.setNeedReloadOnce(true)
    }
}