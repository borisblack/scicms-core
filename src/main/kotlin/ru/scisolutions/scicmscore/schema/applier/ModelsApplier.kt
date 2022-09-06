package ru.scisolutions.scicmscore.schema.applier

import ru.scisolutions.scicmscore.schema.model.AbstractModel

interface ModelsApplier {
    fun apply(model: AbstractModel): String
}