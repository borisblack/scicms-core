package ru.scisolutions.scicmscore.persistence.service

interface SchemaLockService {
    fun lock(): Boolean

    fun lockOrThrow()

    fun unlock(): Boolean

    fun unlockOrThrow()
}