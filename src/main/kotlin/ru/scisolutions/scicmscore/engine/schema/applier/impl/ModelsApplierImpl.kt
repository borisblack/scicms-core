package ru.scisolutions.scicmscore.engine.schema.applier.impl

import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.api.graphql.ReloadIndicator
import ru.scisolutions.scicmscore.engine.persistence.service.CacheService
import ru.scisolutions.scicmscore.engine.schema.applier.ModelApplier
import ru.scisolutions.scicmscore.engine.schema.applier.ModelsApplier
import ru.scisolutions.scicmscore.engine.schema.model.AbstractModel
import ru.scisolutions.scicmscore.engine.schema.model.ModelApplyResult
import ru.scisolutions.scicmscore.engine.service.ItemCacheManager

@Service
class ModelsApplierImpl(
    private val appliers: List<ModelApplier>,
    private val reloadIndicator: ReloadIndicator,
    private val cacheService: CacheService,
    private val itemCacheManager: ItemCacheManager
): ModelsApplier {
    override fun apply(model: AbstractModel, clearCachesAndReload: Boolean): ModelApplyResult {
        for (applier in appliers) {
            if (applier.supports(model::class.java)) {
                val appliedModelResult = applier.apply(model)

                if (appliedModelResult.applied && clearCachesAndReload) {
                    cacheService.clearAllSchemaCaches()
                    itemCacheManager.clearAll()
                    reloadIndicator.setNeedReload(true)
                }

                return appliedModelResult
            }
        }

        throw UnsupportedOperationException("Unsupported model [${model.metadata.name}]")
    }
}