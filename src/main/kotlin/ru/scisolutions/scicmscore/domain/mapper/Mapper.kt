package ru.scisolutions.scicmscore.domain.mapper

interface Mapper<S, T> {
    fun map(source: S): T
    fun copy(source: S, target: T)
}