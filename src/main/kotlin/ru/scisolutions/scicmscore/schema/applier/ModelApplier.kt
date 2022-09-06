package ru.scisolutions.scicmscore.schema.applier

import ru.scisolutions.scicmscore.schema.model.AbstractModel

interface ModelApplier {
    fun supports(clazz: Class<*>): Boolean

    fun apply(model: AbstractModel): String
}