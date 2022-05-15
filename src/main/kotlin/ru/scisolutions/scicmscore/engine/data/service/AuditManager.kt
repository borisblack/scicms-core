package ru.scisolutions.scicmscore.engine.data.service

import ru.scisolutions.scicmscore.engine.data.model.ItemRec

interface AuditManager {
    fun assignAuditAttributes(itemRec: ItemRec)

    fun assignUpdateAttributes(itemRec: ItemRec)
}