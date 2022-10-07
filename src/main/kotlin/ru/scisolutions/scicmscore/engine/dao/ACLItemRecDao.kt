package ru.scisolutions.scicmscore.engine.dao

import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.persistence.entity.Item

interface ACLItemRecDao {
    fun findByIdForRead(item: Item, id: String, selectAttrNames: Set<String>? = null): ItemRec?

    fun findByIdForWrite(item: Item, id: String, selectAttrNames: Set<String>? = null): ItemRec?

    fun findByIdForDelete(item: Item, id: String, selectAttrNames: Set<String>? = null): ItemRec?

    fun findByIdForAdministration(item: Item, id: String, selectAttrNames: Set<String>? = null): ItemRec?

    fun existsByIdForRead(item: Item, id: String): Boolean

    fun existsByIdForWrite(item: Item, id: String): Boolean

    fun existsByIdForDelete(item: Item, id: String): Boolean

    fun existsByIdForAdministration(item: Item, id: String): Boolean

    fun findAllByIdsForRead(item: Item, ids: Set<String>): List<ItemRec>

    fun findAllByAttributeForRead(item: Item, attrName: String, attrValue: Any): List<ItemRec>

    fun findAllByAttributeForWrite(item: Item, attrName: String, attrValue: Any): List<ItemRec>

    fun findAllByAttributeForCreate(item: Item, attrName: String, attrValue: Any): List<ItemRec>

    fun findAllByAttributeForDelete(item: Item, attrName: String, attrValue: Any): List<ItemRec>

    fun findAllByAttributeForAdministration(item: Item, attrName: String, attrValue: Any): List<ItemRec>
}