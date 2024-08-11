package ru.scisolutions.scicmscore.engine.persistence.dao

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.persistence.entity.Item
import ru.scisolutions.scicmscore.engine.persistence.mapper.ItemRecMapper
import ru.scisolutions.scicmscore.engine.persistence.query.AttributeSqlParameterSource
import ru.scisolutions.scicmscore.engine.persistence.query.ItemQueryBuilder
import ru.scisolutions.scicmscore.engine.persistence.service.UserService
import ru.scisolutions.scicmscore.engine.service.AuditManager
import ru.scisolutions.scicmscore.engine.service.DatasourceManager
import ru.scisolutions.scicmscore.engine.service.DefaultIdGenerator
import ru.scisolutions.scicmscore.engine.service.ItemCacheManager
import ru.scisolutions.scicmscore.engine.service.SequenceManager
import ru.scisolutions.scicmscore.engine.service.VersionManager

@Service
class ItemRecDao(
    private val userService: UserService,
    private val versionManager: VersionManager,
    private val sequenceManager: SequenceManager,
    private val auditManager: AuditManager,
    private val dsManager: DatasourceManager,
    private val itemCacheManager: ItemCacheManager,
    private val idGenerator: DefaultIdGenerator
) : BaseItemRecDao(dsManager, itemCacheManager) {
    override val logger: Logger = LoggerFactory.getLogger(ItemRecDao::class.java)

    fun findById(item: Item, id: String, selectAttrNames: Set<String>?): ItemRec? {
        val paramSource = AttributeSqlParameterSource()
        val query = itemQueryBuilder.buildFindByIdQuery(item, id, paramSource, selectAttrNames)
        return findOne(item, query.toString(), paramSource)
    }

    fun findByIdOrThrow(item: Item, id: String, selectAttrNames: Set<String>? = null): ItemRec =
        findById(item, id, selectAttrNames) ?: throw IllegalArgumentException("Item [${item.name}] with ID [$id] not found.")

    fun existsById(item: Item, id: String): Boolean = countByIds(item, setOf(id)) > 0

    fun existsByKey(item: Item, keyAttrName: String, key: String): Boolean = countByKeys(item, keyAttrName, setOf(key)) > 0

    fun existAllByIds(item: Item, ids: Set<String>): Boolean = countByIds(item, ids) == ids.size

    private fun countByIds(item: Item, ids: Set<String>): Int = countByKeys(item, item.idAttribute, ids)

    private fun countByKeys(item: Item, keyAttrName: String, keys: Set<String>): Int {
        val paramSource = AttributeSqlParameterSource()
        val query = itemQueryBuilder.buildFindAllByKeysQuery(item, keyAttrName, keys, paramSource)
        return count(item, query.toString(), paramSource)
    }

    fun findAll(item: Item, sql: String, paramSource: AttributeSqlParameterSource): List<ItemRec> = itemCacheManager.get(item, sql, paramSource) {
        traceSqlAndParameters(sql, paramSource)
        dsManager.template(item.ds).query(sql, paramSource, ItemRecMapper(item))
    }

    fun findAllByAttribute(item: Item, attrName: String, attrValue: Any): List<ItemRec> {
        val paramSource = AttributeSqlParameterSource()
        val query = itemQueryBuilder.buildFindAllByAttributeQuery(item, attrName, attrValue, paramSource)
        val sql = query.toString()

        return itemCacheManager.get(item, sql, paramSource) {
            traceSqlAndParameters(sql, paramSource)
            dsManager.template(item.ds).query(sql, paramSource, ItemRecMapper(item))
        }
    }

    fun insert(item: Item, itemRec: ItemRec): Int {
        val paramSource = AttributeSqlParameterSource()
        val query = itemQueryBuilder.buildInsertQuery(item, itemRec, paramSource)
        val sql = query.toString()
        traceSqlAndParameters(sql, paramSource)
        val res = dsManager.template(item.ds).update(sql, paramSource)
        itemCacheManager.clear(item)

        return res
    }

    fun insertWithDefaults(item: Item, itemRec: ItemRec): Int {
        val id = idGenerator.generateId()
        if (itemRec[item.idAttribute] == null) {
            itemRec[item.idAttribute] = id
        }
        itemRec.id = id
        itemRec.configId = id

        sequenceManager.assignSequenceAttributes(item, itemRec)
        versionManager.assignVersionAttributes(item, itemRec, itemRec.majorRev)
        auditManager.assignAuditAttributes(itemRec)

        return insert(item, itemRec)
    }

    fun updateById(item: Item, id: String, updateAttributes: Map<String, Any?>): Int = updateByAttribute(item, item.idAttribute, id, updateAttributes)

    fun updateByAttribute(item: Item, whereAttrName: String, whereAttrValue: Any?, updateAttributes: Map<String, Any?>): Int =
        updateByAttributes(item, mapOf(whereAttrName to whereAttrValue), updateAttributes)

    fun updateByAttributes(item: Item, whereAttributes: Map<String, Any?>, updateAttributes: Map<String, Any?>): Int {
        val paramSource = AttributeSqlParameterSource()
        val query = itemQueryBuilder.buildUpdateByAttributesQuery(item, whereAttributes, updateAttributes, paramSource)
        val sql = query.toString()
        traceSqlAndParameters(sql, paramSource)
        val res = dsManager.template(item.ds).update(sql, paramSource)
        itemCacheManager.clear(item)

        return res
    }

    fun deleteById(item: Item, id: String): Int = deleteByAttribute(item, item.idAttribute, id)

    fun deleteByAttribute(item: Item, attrName: String, attrValue: Any): Int {
        val paramSource = AttributeSqlParameterSource()
        val query = itemQueryBuilder.buildDeleteByAttributeQuery(item, attrName, attrValue, paramSource)
        val sql = query.toString()
        traceSqlAndParameters(sql, paramSource)
        val res = dsManager.template(item.ds).update(sql, paramSource)
        itemCacheManager.clear(item)

        return res
    }

    fun deleteVersionedById(item: Item, id: String): Int {
        if (!item.versioned) {
            throw IllegalArgumentException("Item [${item.name}] is not versioned")
        }

        val itemRec = findByIdOrThrow(item, id)

        if (!item.notLockable) {
            lockByIdOrThrow(item, id)
        }

        val rows = deleteById(item, id)
        if (itemRec.current == true) {
            logger.debug("Versioned item [${item.name}] with ID [$id] is current. Updating group before deleting")
            assignNewCurrent(item, itemRec)
        }

        if (!item.notLockable) {
            unlockById(item, id)
        }

        return rows
    }

    fun deleteVersionedByAttribute(item: Item, attrName: String, attrValue: Any): Int {
        if (!item.versioned) {
            throw IllegalArgumentException("Item [${item.name}] is not versioned")
        }

        val itemsToDelete = findAllByAttribute(item, attrName, attrValue)

        // Lock
        if (!item.notLockable) {
            val lockedRows = lockByAttribute(item, attrName, attrValue)
            if (lockedRows != itemsToDelete.size) {
                unlockByAttribute(item, attrName, attrValue)
                throw IllegalStateException("Failed to lock deleting items")
            }
        }

        itemsToDelete.forEach {
            val id = it.getString(item.idAttribute)
            deleteById(item, id)
            if (it.current == true) {
                logger.debug("Versioned item [${item.name}] with ID [$id] is current. Updating group before deleting")
                assignNewCurrent(item, it)
            }
        }

        // Unlock
        if (!item.notLockable) {
            unlockByAttribute(item, attrName, attrValue)
        }

        return itemsToDelete.size
    }

    private fun assignNewCurrent(item: Item, deletedItemRec: ItemRec) {
        if (!item.versioned) {
            throw IllegalArgumentException("Item [${item.name}] is not versioned")
        }

        val deletedId = deletedItemRec.getString(item.idAttribute)
        if (deletedItemRec.current != true) {
            throw IllegalArgumentException("Item [${item.name}] with ID [$deletedId] is not current")
        }

        val group = findAllByAttribute(item, ItemRec.CONFIG_ID_ATTR_NAME, deletedItemRec.configId as String)
        if (!item.notLockable) {
            val lockedRows = lockByAttribute(item, ItemRec.CONFIG_ID_ATTR_NAME, deletedItemRec.configId as String)
            if (lockedRows != group.size) {
                unlockByAttribute(item, ItemRec.CONFIG_ID_ATTR_NAME, deletedItemRec.configId as String)
                throw IllegalStateException("Failed to lock items group")
            }
        }
        val lastItemRec =
            group
                .filter { it.getString(item.idAttribute) != deletedId && it.locale == deletedItemRec.locale }
                .maxByOrNull { it.generation as Int }

        if (lastItemRec != null) {
            val lastId = lastItemRec.getString(item.idAttribute)
            logger.debug("Setting current flag for the last versioned item [${item.name}] with ID $lastId")
            lastItemRec.current = true
            auditManager.assignUpdateAttributes(lastItemRec)
            updateById(item, lastId, lastItemRec)
        } else {
            logger.debug("There are no another items [${item.name}] within group.")
        }

        if (!item.notLockable) {
            unlockByAttribute(item, ItemRec.CONFIG_ID_ATTR_NAME, deletedItemRec.configId as String)
        }
    }

    fun lockByIdOrThrow(item: Item, id: String) {
        if (!lockById(item, id)) {
            throw IllegalStateException(LOCK_FAIL_MSG.format(item.name, id))
        }
    }

    fun lockById(item: Item, id: String): Boolean {
        val rows = lockByAttribute(item, item.idAttribute, id)
        return if (rows == 1) {
            logger.info("Item [${item.name}] with ID [$id] successfully locked")
            true
        } else {
            logger.warn(LOCK_FAIL_MSG.format(item.name, id))
            false
        }
    }

    fun lockByAttribute(item: Item, attrName: String, attrValue: Any): Int {
        if (item.notLockable) {
            throw IllegalArgumentException("Item [${item.name}] is not lockable")
        }

        val user = userService.getCurrent()
        val paramSource = AttributeSqlParameterSource()
        val query = itemQueryBuilder.buildLockByAttributeQuery(item, attrName, attrValue, user.id, paramSource)
        val sql = query.toString()
        traceSqlAndParameters(sql, paramSource)
        val res = dsManager.template(item.ds).update(sql, paramSource)
        itemCacheManager.clear(item)

        return res
    }

    fun unlockByIdOrThrow(item: Item, id: String) {
        if (!unlockById(item, id)) {
            throw IllegalStateException(UNLOCK_FAIL_MSG.format(item.name, id))
        }
    }

    fun unlockById(item: Item, id: String): Boolean {
        if (item.notLockable) {
            throw IllegalArgumentException("Item [${item.name}] is not lockable")
        }

        val rows = unlockByAttribute(item, item.idAttribute, id)
        return if (rows == 1) {
            logger.info("Item [${item.name}] with ID [$id] successfully unlocked")
            true
        } else {
            logger.warn(UNLOCK_FAIL_MSG.format(item.name, id))
            false
        }
    }

    fun unlockByAttribute(item: Item, attrName: String, attrValue: Any): Int {
        if (item.notLockable) {
            throw IllegalArgumentException("Item [${item.name}] is not lockable")
        }

        val user = userService.getCurrent()
        val paramSource = AttributeSqlParameterSource()
        val query = itemQueryBuilder.buildUnlockByAttributeQuery(item, attrName, attrValue, user.id, paramSource)
        val sql = query.toString()
        traceSqlAndParameters(sql, paramSource)
        val res = dsManager.template(item.ds).update(sql, paramSource)
        itemCacheManager.clear(item)

        return res
    }

    companion object {
        private const val LOCK_FAIL_MSG = "Cannot lock item %s with ID [%s]. It was locked by another user"
        private const val UNLOCK_FAIL_MSG = "Cannot unlock item %s with ID [%s]"

        private val itemQueryBuilder = ItemQueryBuilder()
    }
}
