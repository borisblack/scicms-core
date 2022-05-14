package ru.scisolutions.scicmscore.engine.data.dao

import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.persistence.entity.Item

interface ACLItemRecDao {
    fun findByIdForRead(item: Item, id: String, selectAttrNames: Set<String>? = null): ItemRec?

    fun findByIdForWrite(item: Item, id: String, selectAttrNames: Set<String>? = null): ItemRec?

    fun findByIdForCreate(item: Item, id: String, selectAttrNames: Set<String>? = null): ItemRec?

    fun findByIdForDelete(item: Item, id: String, selectAttrNames: Set<String>? = null): ItemRec?

    fun findByIdForAdministration(item: Item, id: String, selectAttrNames: Set<String>? = null): ItemRec?

    fun existsByIdForRead(item: Item, id: String): Boolean

    fun existsByIdForWrite(item: Item, id: String): Boolean

    fun existsByIdForCreate(item: Item, id: String): Boolean

    fun existsByIdForDelete(item: Item, id: String): Boolean

    fun existsByIdForAdministration(item: Item, id: String): Boolean
}