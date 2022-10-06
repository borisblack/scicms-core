package ru.scisolutions.scicmscore.persistence.service

import ru.scisolutions.scicmscore.persistence.entity.RevisionPolicy

interface RevisionPolicyService {
    fun findById(id: String): RevisionPolicy?
}