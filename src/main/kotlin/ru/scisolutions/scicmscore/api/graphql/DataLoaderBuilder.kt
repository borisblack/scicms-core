package ru.scisolutions.scicmscore.api.graphql

import org.dataloader.MappedBatchLoader
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.engine.dao.ACLItemRecDao
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.service.ItemCache
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

@Component
class DataLoaderBuilder(
    private val itemCache: ItemCache,
    private val aclItemRecDao: ACLItemRecDao,
    private val executor: Executor,
    private val dataProps: DataProps
) {
    fun build(itemName: String): MappedBatchLoader<String, ItemRec> =
        MappedBatchLoader { ids ->
            val item = itemCache.getOrThrow(itemName)

            CompletableFuture.supplyAsync({
                findAllBiIds(item, ids).associateBy { it.id as String }
            }, executor)
        }

    private fun findAllBiIds(item: Item, ids: Set<String>): List<ItemRec> {
        if (ids.size <= dataProps.dataLoaderChunkSize)
            aclItemRecDao.findAllByIdsForRead(item, ids)

        return ids.asSequence()
            .chunked(dataProps.dataLoaderChunkSize)
            .map { it.toSet() }
            .map { aclItemRecDao.findAllByIdsForRead(item, it) }
            .flatten()
            .toList()
    }
}