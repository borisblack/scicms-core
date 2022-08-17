package ru.scisolutions.scicmscore.persistence.service

interface AllowedLifecycleService {
    fun findLifecycleIdsByItemName(itemName: String): Set<String>
}