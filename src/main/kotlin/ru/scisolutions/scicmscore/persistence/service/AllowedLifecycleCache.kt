package ru.scisolutions.scicmscore.persistence.service

import ru.scisolutions.scicmscore.persistence.entity.AllowedLifecycle

interface AllowedLifecycleCache {
    fun findAllByItemName(itemName: String): List<AllowedLifecycle>
}