package ru.scisolutions.scicmscore.engine.hook

interface GenerateIdHook {
    fun generateId(itemName: String): String
}
