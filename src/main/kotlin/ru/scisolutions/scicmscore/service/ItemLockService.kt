package ru.scisolutions.scicmscore.service

interface ItemLockService {
    fun lock(): Boolean

    fun unlock(): Boolean
}