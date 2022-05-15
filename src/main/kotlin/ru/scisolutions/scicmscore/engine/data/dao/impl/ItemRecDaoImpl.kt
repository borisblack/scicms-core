package ru.scisolutions.scicmscore.engine.data.dao.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.JdbcTemplateMap
import ru.scisolutions.scicmscore.engine.data.dao.ItemRecDao
import ru.scisolutions.scicmscore.engine.data.db.ItemRecMapper
import ru.scisolutions.scicmscore.engine.data.db.query.DaoQueryBuilder
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.service.AuditManager
import ru.scisolutions.scicmscore.engine.data.service.SequenceManager
import ru.scisolutions.scicmscore.engine.data.service.VersionManager
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.UserService
import java.util.UUID

@Service
class ItemRecDaoImpl(
    private val userService: UserService,
    private val versionManager: VersionManager,
    private val sequenceManager: SequenceManager,
    private val auditManager: AuditManager,
    private val jdbcTemplateMap: JdbcTemplateMap
) : BaseItemRecDao(jdbcTemplateMap), ItemRecDao {
    override fun findById(item: Item, id: String, selectAttrNames: Set<String>?): ItemRec? {
        val query =  daoQueryBuilder.buildFindByIdQuery(item, id, selectAttrNames)
        return findOne(item, query.toString())
    }

    override fun findByIdOrThrow(item: Item, id: String, selectAttrNames: Set<String>?): ItemRec =
        findById(item, id, selectAttrNames) ?: throw IllegalArgumentException("Item [${item.name}] with ID [$id] not found")

    override fun existsById(item: Item, id: String): Boolean = countByIds(item, setOf(id)) > 0

    override fun existAllByIds(item: Item, ids: Set<String>): Boolean = countByIds(item, ids) == ids.size

    private fun countByIds(item: Item, ids: Set<String>): Int {
        val query = daoQueryBuilder.buildFindByIdsQuery(item, ids)
        return count(item, query.toString())
    }

    override fun findAll(item: Item, sql: String): List<ItemRec> {
        logger.debug("Running SQL: {}", sql)
        return jdbcTemplateMap.getOrThrow(item.dataSource).query(sql, ItemRecMapper(item))
    }

    override fun findAllByAttribute(item: Item, attrName: String, attrValue: Any): List<ItemRec> {
        val query = daoQueryBuilder.buildFindAllByAttributeQuery(item, attrName, attrValue)
        val sql = query.toString()
        logger.debug("Running SQL: {}", sql)
        return jdbcTemplateMap.getOrThrow(item.dataSource).query(sql, ItemRecMapper(item))
    }

    override fun insert(item: Item, itemRec: ItemRec): Int {
        val query = daoQueryBuilder.buildInsertQuery(item, itemRec)
        val sql = query.toString()
        logger.debug("Running SQL: {}", sql)
        return jdbcTemplateMap.getOrThrow(item.dataSource).update(sql)
    }

    override fun insertWithDefaults(item: Item, itemRec: ItemRec): Int {
        with(itemRec) {
            id = UUID.randomUUID().toString()
            configId = id
        }

        sequenceManager.assignSequenceAttributes(item, itemRec)
        versionManager.assignVersionAttributes(item, itemRec, itemRec.majorRev)
        auditManager.assignAuditAttributes(itemRec)

        return insert(item, itemRec)
    }

    override fun updateById(item: Item, id: String, itemRec: ItemRec): Int =
        updateByAttribute(item, ID_ATTR_NAME, id, itemRec)

    override fun updateByAttribute(item: Item, attrName: String, attrValue: Any, itemRec: ItemRec): Int {
        val query = daoQueryBuilder.buildUpdateByAttributeQuery(item, attrName, attrValue, itemRec)
        val sql = query.toString()
        logger.debug("Running SQL: {}", sql)
        return jdbcTemplateMap.getOrThrow(item.dataSource).update(sql)
    }

    override fun deleteById(item: Item, id: String): Int = deleteByAttribute(item, ID_ATTR_NAME, id)

    override fun deleteByAttribute(item: Item, attrName: String, attrValue: Any): Int {
        val query = daoQueryBuilder.buildDeleteByAttributeQuery(item, attrName, attrValue)
        val sql = query.toString()
        logger.debug("Running SQL: {}", sql)
        return jdbcTemplateMap.getOrThrow(item.dataSource).update(sql)
    }

    override fun lockById(item: Item, id: String): Boolean {
        if (item.notLockable)
            throw IllegalArgumentException("Item [${item.name}] is not lockable")

        val user = userService.getCurrentUser()
        val query = daoQueryBuilder.buildLockByIdQuery(item, id, user.id)
        val sql = query.toString()

        logger.debug("Running SQL: {}", sql)
        val result = jdbcTemplateMap.getOrThrow(item.dataSource).update(sql)

        return if (result == 1) {
            logger.info("Item [${item.name}] with ID [$id] successfully locked")
            true
        } else {
            logger.warn(LOCK_FAIL_MSG.format(item.name, id))
            false
        }
    }

    override fun lockByIdOrThrow(item: Item, id: String) {
        if (!lockById(item, id))
            throw IllegalStateException(LOCK_FAIL_MSG.format(item.name, id))
    }

    override fun unlockById(item: Item, id: String): Boolean {
        if (item.notLockable)
            throw IllegalArgumentException("Item [${item.name}] is not lockable")

        val user = userService.getCurrentUser()
        val query = daoQueryBuilder.buildUnlockByIdQuery(item, id, user.id)
        val sql = query.toString()

        logger.debug("Running SQL: {}", sql)
        val result = jdbcTemplateMap.getOrThrow(item.dataSource).update(sql)

        return if (result == 1) {
            logger.info("Item [${item.name}] with ID [$id] successfully unlocked")
            true
        } else {
            logger.warn(UNLOCK_FAIL_MSG.format(item.name, id))
            false
        }
    }

    override fun unlockByIdOrThrow(item: Item, id: String) {
        if (!unlockById(item, id))
            throw IllegalStateException(UNLOCK_FAIL_MSG.format(item.name, id))
    }

    companion object {
        private const val ID_ATTR_NAME = "id"
        private const val LOCK_FAIL_MSG = "Cannot lock item %s with ID [%s]. It was locked by another user"
        private const val UNLOCK_FAIL_MSG = "Cannot unlock item %s with ID [%s]"

        private val logger = LoggerFactory.getLogger(ItemRecDaoImpl::class.java)
        private val daoQueryBuilder = DaoQueryBuilder()
    }
}