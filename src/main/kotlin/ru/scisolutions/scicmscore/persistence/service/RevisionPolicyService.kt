package ru.scisolutions.scicmscore.persistence.service

import ru.scisolutions.scicmscore.persistence.entity.RevisionPolicy

interface RevisionPolicyService {
    fun getDefaultRevisionPolicy(): RevisionPolicy

    fun getById(id: String): RevisionPolicy
}