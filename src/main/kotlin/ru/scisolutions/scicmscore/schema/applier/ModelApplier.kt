package ru.scisolutions.scicmscore.schema.applier

import ru.scisolutions.scicmscore.schema.model.AbstractModel
import ru.scisolutions.scicmscore.schema.model.ModelApplyResult

interface ModelApplier {
    fun supports(clazz: Class<*>): Boolean

    fun apply(model: AbstractModel): ModelApplyResult
}