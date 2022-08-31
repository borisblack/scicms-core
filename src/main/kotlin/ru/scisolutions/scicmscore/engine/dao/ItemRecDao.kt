package ru.scisolutions.scicmscore.engine.dao

import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.persistence.entity.Item
import java.util.UUID

interface ItemRecDao {
    fun findById(item: Item, id: UUID, selectAttrNames: Set<String>? = null): ItemRec?

    fun findByIdOrThrow(item: Item, id: UUID, selectAttrNames: Set<String>? = null): ItemRec

    fun existsById(item: Item, id: UUID): Boolean

    fun existAllByIds(item: Item, ids: Set<String>): Boolean

    fun count(item: Item, sql: String): Int

    fun findAll(item: Item, sql: String): List<ItemRec>

    fun findAllByAttribute(item: Item, attrName: String, attrValue: Any): List<ItemRec>

    fun insert(item: Item, itemRec: ItemRec): Int

    fun insertWithDefaults(item: Item, itemRec: ItemRec): Int

    fun updateById(item: Item, id: UUID, itemRec: ItemRec): Int

    fun updateByAttribute(item: Item, attrName: String, attrValue: Any, itemRec: ItemRec): Int

    fun deleteById(item: Item, id: UUID): Int

    fun deleteByAttribute(item: Item, attrName: String, attrValue: Any): Int

    fun deleteVersionedById(item: Item, id: UUID): Int

    fun deleteVersionedByAttribute(item: Item, attrName: String, attrValue: Any): Int

    fun lockById(item: Item, id: UUID): Boolean

    fun lockByIdOrThrow(item: Item, id: UUID)

    fun lockByAttribute(item: Item, attrName: String, attrValue: Any): Int

    fun unlockById(item: Item, id: UUID): Boolean

    fun unlockByIdOrThrow(item: Item, id: UUID)

    fun unlockByAttribute(item: Item, attrName: String, attrValue: Any): Int
}