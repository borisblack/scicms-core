package ru.scisolutions.scicmscore.engine.data.model

interface Promotable {
    fun promote(itemName: String, id: String, state: String)
}