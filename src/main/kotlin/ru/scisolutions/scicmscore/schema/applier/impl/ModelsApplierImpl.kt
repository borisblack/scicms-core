package ru.scisolutions.scicmscore.schema.applier.impl

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.schema.applier.ModelApplier
import ru.scisolutions.scicmscore.schema.applier.ModelsApplier
import ru.scisolutions.scicmscore.schema.model.AbstractModel

@Service
class ModelsApplierImpl(private val appliers: List<ModelApplier>): ModelsApplier {
    override fun apply(model: AbstractModel): String {
        for (applier in appliers) {
            if (applier.supports(model::class.java)) {
                return applier.apply(model)
            }
        }

        throw UnsupportedOperationException("Unsupported model [${model.metadata.name}]")
    }
}