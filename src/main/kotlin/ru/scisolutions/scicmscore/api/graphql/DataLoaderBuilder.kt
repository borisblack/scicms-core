package ru.scisolutions.scicmscore.api.graphql

import org.dataloader.MappedBatchLoader
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.engine.persistence.dao.ACLItemRecDao
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.persistence.entity.Item
import ru.scisolutions.scicmscore.engine.persistence.service.ItemService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

@Component
class DataLoaderBuilder(
    private val itemService: ItemService,
    private val aclItemRecDao: ACLItemRecDao,
    @Qualifier("taskExecutor") private val executor: Executor,
    private val dataProps: DataProps
) {
    fun build(itemName: String): MappedBatchLoader<String, ItemRec> =
        MappedBatchLoader { ids ->
            logger.trace("Starting loading data for item [{}] by IDs {}", itemName, ids)
            val item = itemService.getByName(itemName)

            val res = CompletableFuture.supplyAsync({
                findAllByIds(item, ids).associateBy { rec ->
                    rec[item.idAttribute]?.let {
                        if (it is String) it else it.toString()
                    } ?: throw IllegalArgumentException("ID attribute is null.")
                }
            }, executor)

            logger.trace("Loading data for item [{}] by IDs {} completed.", itemName, ids)

            res
        }

    private fun findAllByIds(item: Item, ids: Set<String>): List<ItemRec> {
        if (ids.size <= dataProps.dataLoaderChunkSize)
            return aclItemRecDao.findAllByIdsForRead(item, ids)

        return ids.asSequence()
            .chunked(dataProps.dataLoaderChunkSize)
            .map { it.toSet() }
            .map { aclItemRecDao.findAllByIdsForRead(item, it) }
            .flatten()
            .toList()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DataLoaderBuilder::class.java)
    }
}