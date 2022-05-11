package ru.scisolutions.scicmscore.service

import ru.scisolutions.scicmscore.persistence.entity.RevisionPolicy

interface RevisionPolicyService {
    val defaultRevisionPolicy: RevisionPolicy

    fun getById(id: String): RevisionPolicy
}