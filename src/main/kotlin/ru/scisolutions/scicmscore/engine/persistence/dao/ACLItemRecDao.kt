package ru.scisolutions.scicmscore.engine.persistence.dao

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.engine.model.itemrec.ItemRec
import ru.scisolutions.scicmscore.engine.persistence.entity.Item
import ru.scisolutions.scicmscore.engine.persistence.mapper.ItemRecMapper
import ru.scisolutions.scicmscore.engine.persistence.query.AttributeSqlParameterSource
import ru.scisolutions.scicmscore.engine.persistence.query.ItemQueryBuilder
import ru.scisolutions.scicmscore.engine.persistence.service.PermissionService
import ru.scisolutions.scicmscore.engine.service.DatasourceManager
import ru.scisolutions.scicmscore.engine.service.ItemCacheManager
import ru.scisolutions.scicmscore.engine.util.Acl

@Service
class ACLItemRecDao(
    private val permissionService: PermissionService,
    private val dsManager: DatasourceManager,
    private val itemCacheManager: ItemCacheManager
) : BaseItemRecDao(dsManager, itemCacheManager) {
    override val logger: Logger = LoggerFactory.getLogger(ACLItemRecDao::class.java)

    fun findByIdForRead(item: Item, id: String, selectAttrNames: Set<String>?): ItemRec? = findByIdFor(item, id, selectAttrNames, Acl.Mask.READ)

    fun findByKeyForRead(item: Item, keyAttrName: String, key: String, selectAttrNames: Set<String>?): ItemRec? =
        findByKeyFor(item, keyAttrName, key, selectAttrNames, Acl.Mask.READ)

    fun findByIdForWrite(item: Item, id: String, selectAttrNames: Set<String>? = null): ItemRec? = findByIdFor(item, id, selectAttrNames, Acl.Mask.WRITE)

    fun findByIdForDelete(item: Item, id: String, selectAttrNames: Set<String>? = null): ItemRec? = findByIdFor(item, id, selectAttrNames, Acl.Mask.DELETE)

    fun findByIdForAdministration(item: Item, id: String, selectAttrNames: Set<String>?): ItemRec? = findByIdFor(item, id, selectAttrNames, Acl.Mask.ADMINISTRATION)

    private fun findByIdFor(item: Item, id: String, selectAttrNames: Set<String>?, accessMask: Acl.Mask): ItemRec? {
        val permissionIds: Set<String> = permissionService.idsByAccessMask(accessMask)
        val paramSource = AttributeSqlParameterSource()
        val query = itemQueryBuilder.buildFindByIdQuery(item, id, paramSource, selectAttrNames, permissionIds)
        return findOne(item, query.toString(), paramSource)
    }

    private fun findByKeyFor(item: Item, keyAttrName: String, key: String, selectAttrNames: Set<String>?, accessMask: Acl.Mask): ItemRec? {
        val permissionIds: Set<String> = permissionService.idsByAccessMask(accessMask)
        val paramSource = AttributeSqlParameterSource()
        val query = itemQueryBuilder.buildFindByKeyQuery(item, keyAttrName, key, paramSource, selectAttrNames, permissionIds)
        return findOne(item, query.toString(), paramSource)
    }

    fun existsByIdForRead(item: Item, id: String): Boolean = existsByIdFor(item, id, Acl.Mask.READ)

    fun existsByIdForWrite(item: Item, id: String): Boolean = existsByIdFor(item, id, Acl.Mask.WRITE)

    fun existsByIdForDelete(item: Item, id: String): Boolean = existsByIdFor(item, id, Acl.Mask.DELETE)

    fun existsByIdForAdministration(item: Item, id: String): Boolean = existsByIdFor(item, id, Acl.Mask.ADMINISTRATION)

    fun existsByIdFor(item: Item, id: String, accessMask: Acl.Mask): Boolean = countByIdsFor(item, setOf(id), accessMask) > 0

    fun findAllByIdsForRead(item: Item, ids: Set<String>): List<ItemRec> = findAllByIdsFor(item, ids, Acl.Mask.READ)

    fun findAllByKeysForRead(item: Item, keyAttrName: String, keys: Set<String>): List<ItemRec> = findAllByKeysFor(
        item,
        keyAttrName,
        keys,
        Acl.Mask.READ
    )

    private fun findAllByIdsFor(item: Item, ids: Set<String>, accessMask: Acl.Mask): List<ItemRec> = findAllByKeysFor(item, item.idAttribute, ids, accessMask)

    private fun findAllByKeysFor(item: Item, keyAttrName: String, keys: Set<String>, accessMask: Acl.Mask): List<ItemRec> {
        val permissionIds: Set<String> = permissionService.idsByAccessMask(accessMask)
        val paramSource = AttributeSqlParameterSource()
        val query = itemQueryBuilder.buildFindAllByKeysQuery(item, keyAttrName, keys, paramSource, permissionIds)
        val sql = query.toString()

        return itemCacheManager.get(item, sql, paramSource) {
            traceSqlAndParameters(sql, paramSource)
            dsManager.template(item.ds).query(sql, paramSource, ItemRecMapper(item))
        }
    }

    private fun countByIdsFor(item: Item, ids: Set<String>, accessMask: Acl.Mask): Int {
        val permissionIds: Set<String> = permissionService.idsByAccessMask(accessMask)
        val paramSource = AttributeSqlParameterSource()
        val query = itemQueryBuilder.buildFindAllByIdsQuery(item, ids, paramSource, permissionIds)
        return count(item, query.toString(), paramSource)
    }

    fun findAllByAttributeForRead(item: Item, attrName: String, attrValue: Any): List<ItemRec> = findAllByAttributeFor(item, attrName, attrValue, Acl.Mask.READ)

    fun findAllByAttributeForWrite(item: Item, attrName: String, attrValue: Any): List<ItemRec> = findAllByAttributeFor(item, attrName, attrValue, Acl.Mask.WRITE)

    fun findAllByAttributeForCreate(item: Item, attrName: String, attrValue: Any): List<ItemRec> = findAllByAttributeFor(item, attrName, attrValue, Acl.Mask.CREATE)

    fun findAllByAttributeForDelete(item: Item, attrName: String, attrValue: Any): List<ItemRec> = findAllByAttributeFor(item, attrName, attrValue, Acl.Mask.DELETE)

    fun findAllByAttributeForAdministration(item: Item, attrName: String, attrValue: Any): List<ItemRec> = findAllByAttributeFor(item, attrName, attrValue, Acl.Mask.ADMINISTRATION)

    private fun findAllByAttributeFor(item: Item, attrName: String, attrValue: Any, accessMask: Acl.Mask): List<ItemRec> {
        val permissionIds: Set<String> = permissionService.idsByAccessMask(accessMask)
        val paramSource = AttributeSqlParameterSource()
        val query = itemQueryBuilder.buildFindAllByAttributeQuery(item, attrName, attrValue, paramSource, permissionIds)
        val sql = query.toString()

        return itemCacheManager.get(item, sql, paramSource) {
            traceSqlAndParameters(sql, paramSource)
            dsManager.template(item.ds).query(sql, paramSource, ItemRecMapper(item))
        }
    }

    companion object {
        private val itemQueryBuilder = ItemQueryBuilder()
    }
}
