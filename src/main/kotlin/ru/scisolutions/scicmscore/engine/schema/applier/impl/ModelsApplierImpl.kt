package ru.scisolutions.scicmscore.engine.schema.applier.impl

import ru.scisolutions.scicmscore.engine.schema.applier.ModelApplier
import ru.scisolutions.scicmscore.engine.schema.applier.ModelsApplier
import ru.scisolutions.scicmscore.engine.schema.model.AbstractModel

class ModelsApplierImpl : ModelsApplier {
    private val appliers = mutableListOf<ModelApplier>()

    fun registerApplier(applier: ModelApplier) {
        appliers.add(applier)
    }

    override fun apply(model: AbstractModel) {
        var isApplied = false
        for (applier in appliers) {
            if (applier.supports(model::class.java)) {
                applier.apply(model)
                isApplied = true
            }
        }

        if (!isApplied)
            throw UnsupportedOperationException("Unsupported model [${model.metadata.name}]")
    }
}