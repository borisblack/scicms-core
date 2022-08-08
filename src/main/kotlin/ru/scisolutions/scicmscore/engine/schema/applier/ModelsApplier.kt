package ru.scisolutions.scicmscore.engine.schema.applier

import ru.scisolutions.scicmscore.engine.schema.model.AbstractModel

interface ModelsApplier {
    fun apply(model: AbstractModel)
}