package ru.scisolutions.scicmscore.engine.data.service

import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.persistence.entity.Item

interface LifecycleManager {
    fun assignLifecycleAttributes(item: Item, itemRec: ItemRec)
}