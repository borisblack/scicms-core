package ru.scisolutions.scicmscore.schema.applier

import ru.scisolutions.scicmscore.schema.model.AbstractModel
import ru.scisolutions.scicmscore.schema.model.ModelApplyResult

interface ModelsApplier {
    fun apply(model: AbstractModel): ModelApplyResult
}