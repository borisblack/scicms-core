package ru.scisolutions.scicmscore.persistence.repository

import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.RevisionPolicy

interface RevisionPolicyRepository : CrudRepository<RevisionPolicy, String> {
    fun getById(id: String): RevisionPolicy
}