package ru.scisolutions.scicmscore.engine.data.dao.impl

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.JdbcTemplateMap
import ru.scisolutions.scicmscore.engine.data.dao.ACLItemRecDao
import ru.scisolutions.scicmscore.engine.data.db.ItemRecMapper
import ru.scisolutions.scicmscore.engine.data.db.query.DaoQueryBuilder
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.PermissionService
import ru.scisolutions.scicmscore.util.ACL.Mask

@Service
class ACLItemRecDaoImpl(
    private val permissionService: PermissionService,
    private val jdbcTemplateMap: JdbcTemplateMap
) : BaseItemRecDao(jdbcTemplateMap), ACLItemRecDao {
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
        val query =  daoQueryBuilder.buildFindByIdQuery(item, id, selectAttrNames, permissionIds)
        return findOne(item, query.toString())
    }

    override fun existsByIdForRead(item: Item, id: String): Boolean = existsByIdFor(item, id, Mask.READ)

    override fun existsByIdForWrite(item: Item, id: String): Boolean = existsByIdFor(item, id, Mask.WRITE)

    override fun existsByIdForCreate(item: Item, id: String): Boolean = existsByIdFor(item, id, Mask.CREATE)

    override fun existsByIdForDelete(item: Item, id: String): Boolean = existsByIdFor(item, id, Mask.DELETE)

    override fun existsByIdForAdministration(item: Item, id: String): Boolean = existsByIdFor(item, id, Mask.ADMINISTRATION)

    private fun existsByIdFor(item: Item, id: String, accessMask: Mask): Boolean = countByIdsFor(item, setOf(id), accessMask) > 0

    private fun countByIdsFor(item: Item, ids: Set<String>, accessMask: Mask): Int {
        val permissionIds: Set<String> = permissionService.findIdsFor(accessMask)
        val query = daoQueryBuilder.buildFindByIdsQuery(item, ids, permissionIds)
        return count(item, query.toString())
    }

    override fun findAllByAttributeForRead(item: Item, attrName: String, attrValue: Any): List<ItemRec> =
        findAllByAttributeFor(item, attrName, attrValue, Mask.READ)

    override fun findAllByAttributeForWrite(item: Item, attrName: String, attrValue: Any): List<ItemRec> =
        findAllByAttributeFor(item, attrName, attrValue, Mask.WRITE)

    override fun findAllByAttributeForCreate(item: Item, attrName: String, attrValue: Any): List<ItemRec> =
        findAllByAttributeFor(item, attrName, attrValue, Mask.CREATE)

    override fun findAllByAttributeForDelete(item: Item, attrName: String, attrValue: Any): List<ItemRec> =
        findAllByAttributeFor(item, attrName, attrValue, Mask.DELETE)

    override fun findAllByAttributeForAdministration(item: Item, attrName: String, attrValue: Any): List<ItemRec> =
        findAllByAttributeFor(item, attrName, attrValue, Mask.ADMINISTRATION)

    private fun findAllByAttributeFor(item: Item, attrName: String, attrValue: Any, accessMask: Mask): List<ItemRec> {
        val permissionIds: Set<String> = permissionService.findIdsFor(accessMask)
        val query = daoQueryBuilder.buildFindAllByAttributeQuery(item, attrName, attrValue, permissionIds)
        val sql = query.toString()
        logger.debug("Running SQL: {}", sql)
        return jdbcTemplateMap.getOrThrow(item.dataSource).query(sql, ItemRecMapper(item))
    }

    companion object {
        private const val ID_ATTR_NAME = "id"

        private val logger = LoggerFactory.getLogger(ACLItemRecDaoImpl::class.java)
        private val daoQueryBuilder = DaoQueryBuilder()
    }
}