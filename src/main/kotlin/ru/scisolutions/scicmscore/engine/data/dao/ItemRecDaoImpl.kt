package ru.scisolutions.scicmscore.engine.data.dao

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

    override fun findByKeyAttrNameFor(
        item: Item,
        keyAttrName: String,
        keyAttrValue: String,
        selectAttrNames: Set<String>,
        accessMask: Mask
    ): ItemRec? {
        val permissionIds: Set<String> = permissionService.findIdsFor(accessMask)
        val query =  queryBuilder.buildFindByKeyAttrNameQuery(item, keyAttrName, keyAttrValue, selectAttrNames, permissionIds)
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

    companion object {
        private const val ID_ATTR_NAME = "id"

        private val logger = LoggerFactory.getLogger(ItemRecDaoImpl::class.java)
        private val queryBuilder = QueryBuilder()
    }
}