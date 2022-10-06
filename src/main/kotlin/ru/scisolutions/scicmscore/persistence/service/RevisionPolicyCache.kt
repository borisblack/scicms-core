package ru.scisolutions.scicmscore.persistence.service

import ru.scisolutions.scicmscore.persistence.entity.RevisionPolicy

interface RevisionPolicyCache {
    operator fun get(id: String): RevisionPolicy?

    fun getOrThrow(id: String): RevisionPolicy

    fun getDefault(): RevisionPolicy
}