package ru.scisolutions.scicmscore.api.graphql

import org.dataloader.MappedBatchLoader
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.engine.dao.ACLItemRecDao
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.persistence.service.ItemCache
import java.util.concurrent.CompletableFuture

@Component
class DataLoaderBuilder(
    private val itemCache: ItemCache,
    private val aclItemRecDao: ACLItemRecDao
) {
    fun build(itemName: String): MappedBatchLoader<String, ItemRec> =
        MappedBatchLoader { ids ->
            val item = itemCache.getOrThrow(itemName)

            CompletableFuture.supplyAsync {
                aclItemRecDao.findAllByIdsForRead(item, ids).associateBy { it.id as String }
            }
        }
}