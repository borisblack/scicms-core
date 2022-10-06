package ru.scisolutions.scicmscore.persistence.service

import ru.scisolutions.scicmscore.persistence.entity.Lifecycle

interface LifecycleCache {
    fun getDefault(): Lifecycle

    operator fun get(id: String): Lifecycle?

    fun getOrThrow(id: String): Lifecycle
}