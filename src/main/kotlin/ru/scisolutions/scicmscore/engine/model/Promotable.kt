package ru.scisolutions.scicmscore.engine.model

interface Promotable {
    fun promote(itemName: String, id: String, state: String)
}