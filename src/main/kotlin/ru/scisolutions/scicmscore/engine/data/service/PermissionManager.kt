package ru.scisolutions.scicmscore.engine.data.service

import ru.scisolutions.scicmscore.engine.data.model.ItemRec

interface PermissionManager {
    fun assignPermissionAttribute(itemRec: ItemRec)
}