package ru.scisolutions.scicmscore.engine.dao

import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.persistence.entity.Item
import java.util.UUID

interface ACLItemRecDao {
    fun findByIdForRead(item: Item, id: UUID, selectAttrNames: Set<String>? = null): ItemRec?

    fun findByIdForWrite(item: Item, id: UUID, selectAttrNames: Set<String>? = null): ItemRec?

    fun findByIdForCreate(item: Item, id: UUID, selectAttrNames: Set<String>? = null): ItemRec?

    fun findByIdForDelete(item: Item, id: UUID, selectAttrNames: Set<String>? = null): ItemRec?

    fun findByIdForAdministration(item: Item, id: UUID, selectAttrNames: Set<String>? = null): ItemRec?

    fun existsByIdForRead(item: Item, id: UUID): Boolean

    fun existsByIdForWrite(item: Item, id: UUID): Boolean

    fun existsByIdForCreate(item: Item, id: UUID): Boolean

    fun existsByIdForDelete(item: Item, id: UUID): Boolean

    fun existsByIdForAdministration(item: Item, id: UUID): Boolean

    fun findAllByAttributeForRead(item: Item, attrName: String, attrValue: Any): List<ItemRec>

    fun findAllByAttributeForWrite(item: Item, attrName: String, attrValue: Any): List<ItemRec>

    fun findAllByAttributeForCreate(item: Item, attrName: String, attrValue: Any): List<ItemRec>

    fun findAllByAttributeForDelete(item: Item, attrName: String, attrValue: Any): List<ItemRec>

    fun findAllByAttributeForAdministration(item: Item, attrName: String, attrValue: Any): List<ItemRec>
}