package ru.scisolutions.scicmscore.engine.service

import ru.scisolutions.scicmscore.engine.model.ItemRec
import ru.scisolutions.scicmscore.persistence.entity.Item

interface LifecycleManager {
    fun assignLifecycleAttributes(item: Item, itemRec: ItemRec)
}