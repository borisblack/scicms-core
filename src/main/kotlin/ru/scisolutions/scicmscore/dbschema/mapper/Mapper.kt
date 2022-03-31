package ru.scisolutions.scicmscore.dbschema.mapper

interface ModelMapper<T, U> {
    fun map(source: T): U
}