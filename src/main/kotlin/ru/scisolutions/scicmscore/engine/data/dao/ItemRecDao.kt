package ru.scisolutions.scicmscore.engine.data.dao

import com.healthmarketscience.sqlbuilder.SelectQuery
import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.persistence.entity.Item

interface ItemRecDao {
    fun findById(item: Item, id: String, selectAttrNames: Set<String>? = null): ItemRec?

    fun findByIdOrThrow(item: Item, id: String, selectAttrNames: Set<String>? = null): ItemRec

    fun findByIdForRead(item: Item, id: String, selectAttrNames: Set<String>? = null): ItemRec?

    fun findByIdForWrite(item: Item, id: String, selectAttrNames: Set<String>? = null): ItemRec?

    fun findByIdForCreate(item: Item, id: String, selectAttrNames: Set<String>? = null): ItemRec?

    fun findByIdForDelete(item: Item, id: String, selectAttrNames: Set<String>? = null): ItemRec?

    fun findByIdForAdministration(item: Item, id: String, selectAttrNames: Set<String>? = null): ItemRec?

    fun existsById(item: Item, id: String): Boolean

    fun existAllByIds(item: Item, ids: Set<String>): Boolean

    fun countByIds(item: Item, ids: Set<String>): Int

    fun count(item: Item, query: SelectQuery): Int

    fun insert(item: Item, itemRec: ItemRec)

    fun insertWithDefaults(item: Item, itemRec: ItemRec)

    fun updateById(item: Item, id: String, itemRec: ItemRec)

    fun lockById(item: Item, id: String): Boolean

    fun lockByIdOrThrow(item: Item, id: String)

    fun unlockById(item: Item, id: String): Boolean

    fun unlockByIdOrThrow(item: Item, id: String)
}