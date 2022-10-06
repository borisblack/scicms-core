package ru.scisolutions.scicmscore.persistence.service

import ru.scisolutions.scicmscore.persistence.entity.Lifecycle

interface LifecycleService {
    fun findById(id: String): Lifecycle?
}