package ru.scisolutions.scicmscore.engine.data.service

import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.persistence.entity.Item

interface VersionManager {
    fun assignVersionAttributes(item: Item, itemRec: ItemRec, majorRev: String?)

    fun assignVersionAttributes(item: Item, prevItemRec: ItemRec, itemRec: ItemRec, majorRev: String?)
}