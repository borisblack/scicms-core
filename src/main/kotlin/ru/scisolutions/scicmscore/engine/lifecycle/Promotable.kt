package ru.scisolutions.scicmscore.engine.lifecycle

interface Promotable {
    fun promote(itemName: String, id: String, state: String)
}
