package ru.scisolutions.scicmscore.engine.dao

import ru.scisolutions.scicmscore.engine.db.query.AttributeSqlParameterSource
import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.persistence.entity.Item

interface ItemRecDao {
    fun findById(item: Item, id: String, selectAttrNames: Set<String>? = null): ItemRec?

    fun findByIdOrThrow(item: Item, id: String, selectAttrNames: Set<String>? = null): ItemRec

    fun existsById(item: Item, id: String): Boolean

    fun existAllByIds(item: Item, ids: Set<String>): Boolean

    fun count(item: Item, sql: String, paramSource: AttributeSqlParameterSource): Int

    fun findAll(item: Item, sql: String, paramSource: AttributeSqlParameterSource): List<ItemRec>

    fun findAllByAttribute(item: Item, attrName: String, attrValue: Any): List<ItemRec>

    fun insert(item: Item, itemRec: ItemRec): Int

    fun insertWithDefaults(item: Item, itemRec: ItemRec): Int

    fun updateById(item: Item, id: String, itemRec: ItemRec): Int

    fun updateByAttribute(item: Item, attrName: String, attrValue: Any?, itemRec: ItemRec): Int

    fun updateByAttributes(item: Item, attributes: Map<String, Any?>, itemRec: ItemRec): Int

    fun deleteById(item: Item, id: String): Int

    fun deleteByAttribute(item: Item, attrName: String, attrValue: Any): Int

    fun deleteVersionedById(item: Item, id: String): Int

    fun deleteVersionedByAttribute(item: Item, attrName: String, attrValue: Any): Int

    fun lockById(item: Item, id: String): Boolean

    fun lockByIdOrThrow(item: Item, id: String)

    fun lockByAttribute(item: Item, attrName: String, attrValue: Any): Int

    fun unlockById(item: Item, id: String): Boolean

    fun unlockByIdOrThrow(item: Item, id: String)

    fun unlockByAttribute(item: Item, attrName: String, attrValue: Any): Int
}