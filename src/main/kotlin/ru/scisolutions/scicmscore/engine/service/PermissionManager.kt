package ru.scisolutions.scicmscore.engine.service

import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.persistence.entity.Item

interface PermissionManager {
    fun assignPermissionAttribute(item: Item, itemRec: ItemRec)

    fun assignPermissionAttribute(item: Item, prevItemRec: ItemRec, itemRec: ItemRec)

    fun checkPermissionId(item: Item, permissionId: String?): String

    fun checkPermissionId(item: Item, prevPermissionId: String?, permissionId: String?): String
}