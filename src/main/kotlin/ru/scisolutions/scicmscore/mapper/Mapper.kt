package ru.scisolutions.scicmscore.mapper

interface Mapper<S, T> {
    fun map(source: S): T
    fun copy(source: S, target: T)
}