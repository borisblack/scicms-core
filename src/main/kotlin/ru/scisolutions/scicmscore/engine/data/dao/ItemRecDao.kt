package ru.scisolutions.scicmscore.engine.data.dao

import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.persistence.entity.Item

interface ItemRecDao {
    fun findByIdForRead(item: Item, id: String, selectAttrNames: Set<String>): ItemRec?

    fun findByIdForWrite(item: Item, id: String, selectAttrNames: Set<String>): ItemRec?

    fun findByIdForCreate(item: Item, id: String, selectAttrNames: Set<String>): ItemRec?

    fun findByIdForDelete(item: Item, id: String, selectAttrNames: Set<String>): ItemRec?

    fun findByIdForAdministration(item: Item, id: String, selectAttrNames: Set<String>): ItemRec?

    fun findByIdFor(item: Item, id: String, selectAttrNames: Set<String>, accessMask: Set<Int>): ItemRec?

    fun findByKeyAttrNameForRead(item: Item, keyAttrName: String, keyAttrValue: String, selectAttrNames: Set<String>): ItemRec?

    fun findByKeyAttrNameForWrite(item: Item, keyAttrName: String, keyAttrValue: String, selectAttrNames: Set<String>): ItemRec?

    fun findByKeyAttrNameForCreate(item: Item, keyAttrName: String, keyAttrValue: String, selectAttrNames: Set<String>): ItemRec?

    fun findByKeyAttrNameForDelete(item: Item, keyAttrName: String, keyAttrValue: String, selectAttrNames: Set<String>): ItemRec?

    fun findByKeyAttrNameForAdministration(item: Item, keyAttrName: String, keyAttrValue: String, selectAttrNames: Set<String>): ItemRec?

    fun findByKeyAttrNameFor(
        item: Item,
        keyAttrName: String,
        keyAttrValue: String,
        selectAttrNames: Set<String>,
        accessMask: Set<Int>
    ): ItemRec?
}