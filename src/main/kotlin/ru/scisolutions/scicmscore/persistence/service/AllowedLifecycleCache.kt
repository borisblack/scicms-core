package ru.scisolutions.scicmscore.persistence.service

import ru.scisolutions.scicmscore.persistence.entity.AllowedLifecycle

interface AllowedLifecycleCache {
    operator fun get(itemName: String): List<AllowedLifecycle>
}