package ru.scisolutions.scicmscore.engine.schema.applier.impl

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.schema.applier.ModelApplier
import ru.scisolutions.scicmscore.engine.schema.applier.ModelsApplier
import ru.scisolutions.scicmscore.engine.schema.model.AbstractModel
import ru.scisolutions.scicmscore.engine.schema.model.ModelApplyResult

@Service
class ModelsApplierImpl(private val appliers: List<ru.scisolutions.scicmscore.engine.schema.applier.ModelApplier>): ModelsApplier {
    override fun apply(model: AbstractModel): ModelApplyResult {
        for (applier in appliers) {
            if (applier.supports(model::class.java)) {
                return applier.apply(model)
            }
        }

        throw UnsupportedOperationException("Unsupported model [${model.metadata.name}]")
    }
}