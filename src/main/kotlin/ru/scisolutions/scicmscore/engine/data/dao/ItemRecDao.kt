package ru.scisolutions.scicmscore.engine.data.dao

import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.persistence.entity.Item

interface ItemRecDao {
    fun findById(item: Item, id: String, selectAttrNames: Set<String>? = null): ItemRec?

    fun findByIdOrThrow(item: Item, id: String, selectAttrNames: Set<String>? = null): ItemRec

    fun existsById(item: Item, id: String): Boolean

    fun existAllByIds(item: Item, ids: Set<String>): Boolean

    fun count(item: Item, sql: String): Int

    fun findAll(item: Item, sql: String): List<ItemRec>

    fun findAllByAttribute(item: Item, attrName: String, attrValue: Any): List<ItemRec>

    fun insert(item: Item, itemRec: ItemRec)

    fun insertWithDefaults(item: Item, itemRec: ItemRec)

    fun updateById(item: Item, id: String, itemRec: ItemRec)

    fun updateByAttribute(item: Item, attrName: String, attrValue: Any, itemRec: ItemRec)

    fun deleteById(item: Item, id: String)

    fun deleteByAttribute(item: Item, attrName: String, attrValue: Any)

    fun lockById(item: Item, id: String): Boolean

    fun lockByIdOrThrow(item: Item, id: String)

    fun unlockById(item: Item, id: String): Boolean

    fun unlockByIdOrThrow(item: Item, id: String)
}