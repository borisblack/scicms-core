package ru.scisolutions.scicmscore.engine.schema.applier

import ru.scisolutions.scicmscore.engine.schema.model.AbstractModel

interface ModelApplier {
    fun supports(clazz: Class<*>): Boolean

    fun apply(model: AbstractModel)
}