package ru.scisolutions.scicmscore.engine.schema.applier

import ru.scisolutions.scicmscore.engine.schema.model.AbstractModel
import ru.scisolutions.scicmscore.engine.schema.model.ModelApplyResult

interface ModelApplier {
    fun supports(clazz: Class<*>): Boolean

    fun apply(model: AbstractModel): ModelApplyResult
}