package ru.scisolutions.scicmscore.engine.data.db

import ru.scisolutions.scicmscore.engine.data.model.ItemRec
import ru.scisolutions.scicmscore.persistence.entity.Item

interface ItemRecDao {
    fun findById(item: Item, id: String, fields: Set<String>): ItemRec?
}