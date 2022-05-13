package ru.scisolutions.scicmscore.engine.data.dao

import com.healthmarketscience.sqlbuilder.SelectQuery
import org.slf4j.LoggerFactory
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.JdbcTemplateMap
import ru.scisolutions.scicmscore.engine.data.db.ItemRecMapper
import ru.scisolutions.scicmscore.engine.data.db.query.QueryBuilder
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.engine.data.service.AuditManager
import ru.scisolutions.scicmscore.engine.data.service.SequenceManager
import ru.scisolutions.scicmscore.engine.data.service.VersionManager
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.PermissionService
import ru.scisolutions.scicmscore.service.UserService
import ru.scisolutions.scicmscore.util.ACL.Mask
import java.util.UUID

@Service
class ItemRecDaoImpl(
    private val userService: UserService,
    private val permissionService: PermissionService,
    private val versionManager: VersionManager,
    private val sequenceManager: SequenceManager,
    private val auditManager: AuditManager,
    private val jdbcTemplateMap: JdbcTemplateMap
) : ItemRecDao {
    override fun findById(item: Item, id: String, selectAttrNames: Set<String>?): ItemRec? {
        val query =  queryBuilder.buildFindByIdQuery(item, id, selectAttrNames)
        return findOne(item, query)
    }

    override fun findByIdOrThrow(item: Item, id: String, selectAttrNames: Set<String>?): ItemRec =
        findById(item, id, selectAttrNames) ?: throw IllegalArgumentException("Item [${item.name}] with ID [$id] not found")

    override fun findByIdForRead(item: Item, id: String, selectAttrNames: Set<String>?): ItemRec? =
        findByIdFor(item, id, selectAttrNames, Mask.READ)

    override fun findByIdForWrite(item: Item, id: String, selectAttrNames: Set<String>?): ItemRec? =
        findByIdFor(item, id, selectAttrNames, Mask.WRITE)

    override fun findByIdForCreate(item: Item, id: String, selectAttrNames: Set<String>?): ItemRec? =
        findByIdFor(item, id, selectAttrNames, Mask.CREATE)

    override fun findByIdForDelete(item: Item, id: String, selectAttrNames: Set<String>?): ItemRec? =
        findByIdFor(item, id, selectAttrNames, Mask.DELETE)

    override fun findByIdForAdministration(item: Item, id: String, selectAttrNames: Set<String>?): ItemRec? =
        findByIdFor(item, id, selectAttrNames, Mask.ADMINISTRATION)

    private fun findByIdFor(item: Item, id: String, selectAttrNames: Set<String>?, accessMask: Mask): ItemRec? {
        val permissionIds: Set<String> = permissionService.findIdsFor(accessMask)
        val query =  queryBuilder.buildFindByIdQuery(item, id, selectAttrNames, permissionIds)
        return findOne(item, query)
    }

    private fun findOne(item: Item, query: SelectQuery): ItemRec? {
        val sql = query.toString()

        logger.debug("Running SQL: {}", sql)
        val itemRec: ItemRec? =
            try {
                jdbcTemplateMap.getOrThrow(item.dataSource).queryForObject(sql, ItemRecMapper(item))
            } catch (e: EmptyResultDataAccessException) {
                null
            }

        return itemRec
    }

    override fun existsById(item: Item, id: String): Boolean = countByIds(item, setOf(id)) > 0

    override fun existsByIdForRead(item: Item, id: String): Boolean = existsByIdFor(item, id, Mask.READ)

    override fun existsByIdForWrite(item: Item, id: String): Boolean = existsByIdFor(item, id, Mask.WRITE)

    override fun existsByIdForCreate(item: Item, id: String): Boolean = existsByIdFor(item, id, Mask.CREATE)

    override fun existsByIdForDelete(item: Item, id: String): Boolean = existsByIdFor(item, id, Mask.DELETE)

    override fun existsByIdForAdministration(item: Item, id: String): Boolean = existsByIdFor(item, id, Mask.ADMINISTRATION)

    private fun existsByIdFor(item: Item, id: String, accessMask: Mask): Boolean = countByIdsFor(item, setOf(id), accessMask) > 0

    override fun existAllByIds(item: Item, ids: Set<String>): Boolean = countByIds(item, ids) == ids.size

    private fun countByIds(item: Item, ids: Set<String>): Int {
        val query = queryBuilder.buildFindByIdsQuery(item, ids)
        return count(item, query)
    }

    private fun countByIdsFor(item: Item, ids: Set<String>, accessMask: Mask): Int {
        val permissionIds: Set<String> = permissionService.findIdsFor(accessMask)
        val query = queryBuilder.buildFindByIdsQuery(item, ids, permissionIds)
        return count(item, query)
    }

    override fun count(item: Item, query: SelectQuery): Int {
        val countSQL = "SELECT COUNT(*) FROM ($query) t"
        logger.debug("Running SQL: {}", countSQL)

        return jdbcTemplateMap.getOrThrow(item.dataSource).queryForObject(countSQL, Int::class.java) as Int
    }

    override fun insert(item: Item, itemRec: ItemRec) {
        val query = queryBuilder.buildInsertQuery(item, itemRec)
        val sql = query.toString()
        logger.debug("Running SQL: {}", sql)
        jdbcTemplateMap.getOrThrow(item.dataSource).update(sql)
    }

    override fun insertWithDefaults(item: Item, itemRec: ItemRec) {
        with(itemRec) {
            id = UUID.randomUUID().toString()
            configId = id
        }

        sequenceManager.assignSequenceAttributes(item, itemRec)
        versionManager.assignVersionAttributes(item, itemRec, itemRec.majorRev)
        auditManager.assignAuditAttributes(itemRec)

        insert(item, itemRec)
    }

    override fun updateById(item: Item, id: String, itemRec: ItemRec) {
        val query = queryBuilder.buildUpdateByIdQuery(item, id, itemRec)
        val sql = query.toString()
        logger.debug("Running SQL: {}", sql)
        jdbcTemplateMap.getOrThrow(item.dataSource).update(sql)
    }

    override fun lockById(item: Item, id: String): Boolean {
        if (item.notLockable)
            throw IllegalArgumentException("Item [${item.name}] is not lockable")

        val user = userService.getCurrentUser()
        val query = queryBuilder.buildLockByIdQuery(item, id, user.id)
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
        val query = queryBuilder.buildUnlockByIdQuery(item, id, user.id)
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
        private const val LOCK_FAIL_MSG = "Cannot lock item %s with ID [%s]. It was locked by another user"
        private const val UNLOCK_FAIL_MSG = "Cannot unlock item %s with ID [%s]"

        private val logger = LoggerFactory.getLogger(ItemRecDaoImpl::class.java)
        private val queryBuilder = QueryBuilder()
    }
}