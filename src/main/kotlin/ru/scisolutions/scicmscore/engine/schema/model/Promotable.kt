package ru.scisolutions.scicmscore.engine.schema.model

interface Promotable {
    fun promote(itemName: String, id: String, state: String)
}