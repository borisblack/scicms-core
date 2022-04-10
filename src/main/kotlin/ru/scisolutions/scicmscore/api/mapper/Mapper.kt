package ru.scisolutions.scicmscore.api.mapper

interface Mapper<S, T> {
    fun map(source: S): T
}