package ru.scisolutions.scicmscore.api.graphql

import org.dataloader.MappedBatchLoader
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.persistence.dao.ACLItemRecDao
import ru.scisolutions.scicmscore.engine.persistence.entity.Item
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

@Component
class DataLoaderBuilder(
    private val aclItemRecDao: ACLItemRecDao,
    @Qualifier("taskExecutor") private val executor: Executor,
    private val dataProps: DataProps
) {
    fun build(parentItem: Item, parentAttrName: String, item: Item): MappedBatchLoader<String, ItemRec> {
        val parentAttribute = parentItem.spec.getAttribute(parentAttrName)
        val keyAttrName = parentAttribute.referencedBy ?: item.idAttribute
        return MappedBatchLoader { keys ->
            logger.trace("Starting loading data for item [{}] by keys {}", item.name, keys)

            val res =
                CompletableFuture.supplyAsync({
                    findAllByKeys(item, keyAttrName, keys).associateBy { rec ->
                        rec[keyAttrName]?.let {
                            if (it is String) it else it.toString()
                        } ?: throw IllegalArgumentException("ID attribute is null.")
                    }
                }, executor)

            logger.trace("Loading data for item [{}] by keys {} completed.", item.name, keys)

            res
        }
    }

    private fun findAllByKeys(item: Item, keyAttrName: String, keys: Set<String>): List<ItemRec> {
        if (keys.size <= dataProps.dataLoaderChunkSize) {
            return aclItemRecDao.findAllByKeysForRead(item, keyAttrName, keys)
        }

        return keys.asSequence()
            .chunked(dataProps.dataLoaderChunkSize)
            .map { it.toSet() }
            .map { aclItemRecDao.findAllByKeysForRead(item, keyAttrName, keys) }
            .flatten()
            .toList()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DataLoaderBuilder::class.java)
    }
}
