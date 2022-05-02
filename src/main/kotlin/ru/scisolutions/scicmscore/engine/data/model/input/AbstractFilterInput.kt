package ru.scisolutions.scicmscore.engine.data.model.input

abstract class AbstractFilterInput<T : AbstractFilterInput<T>>(
    val andFilterList: List<T>?,
    val orFilterList: List<T>?,
    val notFilter: T?
) {
    companion object {
        const val AND_KEY = "and"
        const val OR_KEY = "or"
        const val NOT_KEY = "not"
    }
}