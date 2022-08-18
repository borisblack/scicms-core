package ru.scisolutions.scicmscore.schema.applier.impl

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.schema.applier.ModelApplier
import ru.scisolutions.scicmscore.schema.applier.ModelsApplier
import ru.scisolutions.scicmscore.schema.model.AbstractModel

@Service
class ModelsApplierImpl(private val appliers: List<ModelApplier>): ModelsApplier {
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