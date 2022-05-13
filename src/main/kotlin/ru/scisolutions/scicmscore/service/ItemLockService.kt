package ru.scisolutions.scicmscore.service

interface ItemLockService {
    fun lock(): Boolean

    fun lockOrThrow()

    fun unlock(): Boolean

    fun unlockOrThrow()
}