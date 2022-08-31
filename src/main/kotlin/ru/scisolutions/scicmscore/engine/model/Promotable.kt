package ru.scisolutions.scicmscore.engine.model

import java.util.UUID

interface Promotable {
    fun promote(itemName: String, id: UUID, state: String)
}