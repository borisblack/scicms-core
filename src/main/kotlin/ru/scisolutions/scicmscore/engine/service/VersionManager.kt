package ru.scisolutions.scicmscore.engine.service

import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.persistence.entity.Item

interface VersionManager {
    fun assignVersionAttributes(item: Item, itemRec: ItemRec, majorRev: String?)

    fun assignVersionAttributes(item: Item, prevItemRec: ItemRec, itemRec: ItemRec, majorRev: String?)
}