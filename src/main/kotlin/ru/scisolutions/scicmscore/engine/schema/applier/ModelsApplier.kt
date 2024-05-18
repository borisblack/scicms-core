package ru.scisolutions.scicmscore.engine.schema.applier

import ru.scisolutions.scicmscore.engine.schema.model.AbstractModel
import ru.scisolutions.scicmscore.engine.schema.model.ModelApplyResult

interface ModelsApplier {
    fun apply(model: AbstractModel, clearCachesAndReload: Boolean = false): ModelApplyResult
}