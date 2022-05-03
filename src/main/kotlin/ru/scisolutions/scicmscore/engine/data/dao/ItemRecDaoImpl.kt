package ru.scisolutions.scicmscore.engine.data.dao

import org.slf4j.LoggerFactory
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.data.db.ItemRecMapper
import ru.scisolutions.scicmscore.engine.data.db.query.QueryBuilder
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.util.AccessUtil

@Service
class ItemRecDaoImpl(
    private val jdbcTemplate: JdbcTemplate
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

    override fun findByIdFor(item: Item, id: String, selectAttrNames: Set<String>, accessMask: Set<Int>): ItemRec? =
        findByKeyAttrNameFor(item, ID_ATTR_NAME, id, selectAttrNames, accessMask)

    override fun findByKeyAttrNameForRead(item: Item, keyAttrName: String, keyAttrValue: String, selectAttrNames: Set<String>): ItemRec? =
        findByKeyAttrNameFor(item, keyAttrName, keyAttrValue, selectAttrNames, AccessUtil.readMask)

    override fun findByKeyAttrNameForWrite(item: Item, keyAttrName: String, keyAttrValue: String, selectAttrNames: Set<String>): ItemRec? =
        findByKeyAttrNameFor(item, keyAttrName, keyAttrValue, selectAttrNames, AccessUtil.writeMask)

    override fun findByKeyAttrNameForCreate(item: Item, keyAttrName: String, keyAttrValue: String, selectAttrNames: Set<String>): ItemRec? =
        findByKeyAttrNameFor(item, keyAttrName, keyAttrValue, selectAttrNames, AccessUtil.createMask)

    override fun findByKeyAttrNameForDelete(item: Item, keyAttrName: String, keyAttrValue: String, selectAttrNames: Set<String>) =
        findByKeyAttrNameFor(item, keyAttrName, keyAttrValue, selectAttrNames, AccessUtil.deleteMask)

    override fun findByKeyAttrNameForAdministration(item: Item, keyAttrName: String, keyAttrValue: String, selectAttrNames: Set<String>): ItemRec? =
        findByKeyAttrNameFor(item, keyAttrName, keyAttrValue, selectAttrNames, AccessUtil.administrationMask)

    override fun findByKeyAttrNameFor(
        item: Item,
        keyAttrName: String,
        keyAttrValue: String,
        selectAttrNames: Set<String>,
        accessMask: Set<Int>
    ): ItemRec? {
        val query =  queryBuilder.buildFindByKeyAttrNameQueryForRead(item, keyAttrName, keyAttrValue, selectAttrNames)
        val sql = query.toString()

        logger.debug("Running SQL: {}", sql)
        val itemRec: ItemRec? =
            try {
                jdbcTemplate.queryForObject(sql, ItemRecMapper(item))
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