package ru.scisolutions.scicmscore.engine.data.service

import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.persistence.entity.Item

interface LocalizationManager {
    fun assignLocaleAttribute(item: Item, itemRec: ItemRec, locale: String?)
}