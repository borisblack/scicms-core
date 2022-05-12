package ru.scisolutions.scicmscore.service

interface AllowedLifecycleService {
    fun findLifecycleIdsByItemName(itemName: String): Set<String>
}