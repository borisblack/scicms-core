package ru.scisolutions.scicmscore.engine.data.dao

import com.healthmarketscience.sqlbuilder.SelectQuery
import org.slf4j.LoggerFactory
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.JdbcTemplateMap
import ru.scisolutions.scicmscore.engine.data.db.ItemRecMapper
import ru.scisolutions.scicmscore.engine.data.db.query.QueryBuilder
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.service.PermissionService
import ru.scisolutions.scicmscore.util.ACL.Mask

@Service
class ItemRecDaoImpl(
    private val permissionService: PermissionService,
    private val jdbcTemplateMap: JdbcTemplateMap
) : ItemRecDao {
    override fun findByIdForRead(item: Item, id: String, selectAttrNames: Set<String>): ItemRec? =
        findByKeyAttrNameForRead(item, ID_ATTR_NAME, id, selectAttrNames)

    override fun findByIdForWrite(item: Item, id: String, selectAttrNames: Set<String>): ItemRec? =
        findByKeyAttrNameForWrite(item, ID_ATTR_NAME, id, selectAttrNames)

    override fun findByIdForCreate(item: Item, id: String, selectAttrNames: Set<String>): ItemRec? =
        findByKeyAttrNameForCreate(item, ID_ATTR_NAME, id, selectAttrNames)

    override fun findByIdForDelete(item: Item, id: String, selectAttrNames: Set<String>): ItemRec? =
        findByKeyAttrNameForDelete(item, ID_ATTR_NAME, id, selectAttrNames)

    override fun findByIdForAdministration(item: Item, id: String, selectAttrNames: Set<String>): ItemRec? =
        findByKeyAttrNameForAdministration(item, ID_ATTR_NAME, id, selectAttrNames)

    override fun findByIdFor(item: Item, id: String, selectAttrNames: Set<String>, accessMask: Mask): ItemRec? =
        findByKeyAttrNameFor(item, ID_ATTR_NAME, id, selectAttrNames, accessMask)

    override fun findById(item: Item, id: String, selectAttrNames: Set<String>): ItemRec? =
        findByKeyAttrName(item, ID_ATTR_NAME, id, selectAttrNames)

    override fun findByKeyAttrNameForRead(item: Item, keyAttrName: String, keyAttrValue: String, selectAttrNames: Set<String>): ItemRec? =
        findByKeyAttrNameFor(item, keyAttrName, keyAttrValue, selectAttrNames, Mask.READ)

    override fun findByKeyAttrNameForWrite(item: Item, keyAttrName: String, keyAttrValue: String, selectAttrNames: Set<String>): ItemRec? =
        findByKeyAttrNameFor(item, keyAttrName, keyAttrValue, selectAttrNames, Mask.WRITE)

    override fun findByKeyAttrNameForCreate(item: Item, keyAttrName: String, keyAttrValue: String, selectAttrNames: Set<String>): ItemRec? =
        findByKeyAttrNameFor(item, keyAttrName, keyAttrValue, selectAttrNames, Mask.CREATE)

    override fun findByKeyAttrNameForDelete(item: Item, keyAttrName: String, keyAttrValue: String, selectAttrNames: Set<String>) =
        findByKeyAttrNameFor(item, keyAttrName, keyAttrValue, selectAttrNames, Mask.DELETE)

    override fun findByKeyAttrNameForAdministration(item: Item, keyAttrName: String, keyAttrValue: String, selectAttrNames: Set<String>): ItemRec? =
        findByKeyAttrNameFor(item, keyAttrName, keyAttrValue, selectAttrNames, Mask.ADMINISTRATION)

    override fun findByKeyAttrNameFor(item: Item, keyAttrName: String, keyAttrValue: String, selectAttrNames: Set<String>, accessMask: Mask): ItemRec? {
        val permissionIds: Set<String> = permissionService.findIdsFor(accessMask)
        val query =  queryBuilder.buildFindByKeyAttrNameQuery(item, keyAttrName, keyAttrValue, selectAttrNames, permissionIds)
        return findOne(item, query)
    }

    override fun findByKeyAttrName(item: Item, keyAttrName: String, keyAttrValue: String, selectAttrNames: Set<String>): ItemRec? {
        val query =  queryBuilder.buildFindByKeyAttrNameQuery(item, keyAttrName, keyAttrValue, selectAttrNames)
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

    override fun existAllByIds(item: Item, ids: Set<String>): Boolean = countByIds(item, ids) == ids.size

    override fun countByIds(item: Item, ids: Set<String>): Int {
        val query = queryBuilder.buildFindByIdsQuery(item, ids)
        return count(item, query)
    }

    override fun count(item: Item, query: SelectQuery): Int {
        val countSQL = "SELECT COUNT(*) FROM ($query) t"
        return jdbcTemplateMap.getOrThrow(item.dataSource).queryForObject(countSQL, Int::class.java) as Int
    }

    override fun insert(item: Item, itemRec: ItemRec) {
        val query = queryBuilder.buildInsertQuery(item, itemRec)
        jdbcTemplateMap.getOrThrow(item.dataSource).update(query.toString())
    }

    override fun updateById(item: Item, id: String, itemRec: ItemRec) {
        val query = queryBuilder.buildUpdateByIdQuery(item, id, itemRec)
        jdbcTemplateMap.getOrThrow(item.dataSource).update(query.toString())
    }

    companion object {
        private const val ID_ATTR_NAME = "id"

        private val logger = LoggerFactory.getLogger(ItemRecDaoImpl::class.java)
        private val queryBuilder = QueryBuilder()
    }
}