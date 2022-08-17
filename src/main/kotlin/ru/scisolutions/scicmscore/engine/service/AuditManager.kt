package ru.scisolutions.scicmscore.engine.service

import ru.scisolutions.scicmscore.engine.model.ItemRec

interface AuditManager {
    fun assignAuditAttributes(itemRec: ItemRec)

    fun assignUpdateAttributes(itemRec: ItemRec)
}