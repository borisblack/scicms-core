package ru.scisolutions.scicmscore.engine.model.input

abstract class AbstractFilterInput<T : AbstractFilterInput<T>>(
    val andFilterList: List<T>?,
    val orFilterList: List<T>?,
    val notFilter: T?
)