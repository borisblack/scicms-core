package ru.scisolutions.scicmscore.engine.data.dao

import com.healthmarketscience.sqlbuilder.SelectQuery
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.util.ACL.Mask

interface ItemRecDao {
    fun findByIdForRead(item: Item, id: String, selectAttrNames: Set<String>): ItemRec?

    fun findByIdForWrite(item: Item, id: String, selectAttrNames: Set<String>): ItemRec?

    fun findByIdForCreate(item: Item, id: String, selectAttrNames: Set<String>): ItemRec?

    fun findByIdForDelete(item: Item, id: String, selectAttrNames: Set<String>): ItemRec?

    fun findByIdForAdministration(item: Item, id: String, selectAttrNames: Set<String>): ItemRec?

    fun findByIdFor(item: Item, id: String, selectAttrNames: Set<String>, accessMask: Mask): ItemRec?

    fun findById(item: Item, id: String, selectAttrNames: Set<String>): ItemRec?

    fun findByKeyAttrNameForRead(item: Item, keyAttrName: String, keyAttrValue: String, selectAttrNames: Set<String>): ItemRec?

    fun findByKeyAttrNameForWrite(item: Item, keyAttrName: String, keyAttrValue: String, selectAttrNames: Set<String>): ItemRec?

    fun findByKeyAttrNameForCreate(item: Item, keyAttrName: String, keyAttrValue: String, selectAttrNames: Set<String>): ItemRec?

    fun findByKeyAttrNameForDelete(item: Item, keyAttrName: String, keyAttrValue: String, selectAttrNames: Set<String>): ItemRec?

    fun findByKeyAttrNameForAdministration(item: Item, keyAttrName: String, keyAttrValue: String, selectAttrNames: Set<String>): ItemRec?

    fun findByKeyAttrNameFor(item: Item, keyAttrName: String, keyAttrValue: String, selectAttrNames: Set<String>, accessMask: Mask): ItemRec?

    fun findByKeyAttrName(item: Item, keyAttrName: String, keyAttrValue: String, selectAttrNames: Set<String>): ItemRec?

    fun existsById(item: Item, id: String): Boolean

    fun existAllByIds(item: Item, ids: Set<String>): Boolean

    fun countByIds(item: Item, ids: Set<String>): Int

    fun count(item: Item, query: SelectQuery): Int

    fun insert(item: Item, itemRec: ItemRec)

    fun updateById(item: Item, id: String, itemRec: ItemRec)
}