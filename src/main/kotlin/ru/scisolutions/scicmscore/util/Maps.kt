package ru.scisolutions.scicmscore.util

object Maps {
    fun <T> merge(from: Map<String, T>, to: Map<String, T>, replace: Boolean = true): Map<String, T> {
        val merged = to.toMutableMap()
        if (replace) {
            merged.putAll(from)
        } else {
            from.forEach { (k, v) -> merged.putIfAbsent(k, v) }
        }

        return merged
    }
}
