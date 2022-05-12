package ru.scisolutions.scicmscore.service

import ru.scisolutions.scicmscore.persistence.entity.Lifecycle

interface LifecycleService {
    fun getDefaultLifecycle(): Lifecycle

    fun getById(id: String): Lifecycle
}