package ru.scisolutions.scicmscore.util

object Maps {
    fun merge(source: Map<String, Any?>, target: Map<String, Any?>): Map<String, Any?> {
        val merged = target.toMutableMap()
        merged.putAll(source)

        return merged
    }
}