package ru.scisolutions.scicmscore.engine.data.service

import ru.scisolutions.scicmscore.engine.data.model.ItemRec

interface AuditManager {
    fun assignAuditAttributes(itemRec: ItemRec)

    fun assignAuditAttributes(prevItemRec: ItemRec, itemRec: ItemRec)
}