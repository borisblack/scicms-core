package ru.scisolutions.scicmscore.service

interface SchemaLockService {
    fun lock(): Boolean

    fun lockOrThrow()

    fun unlock(): Boolean

    fun unlockOrThrow()
}