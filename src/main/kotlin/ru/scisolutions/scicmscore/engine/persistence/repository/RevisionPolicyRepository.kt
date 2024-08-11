package ru.scisolutions.scicmscore.engine.persistence.repository

import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.engine.persistence.entity.RevisionPolicy

interface RevisionPolicyRepository : CrudRepository<RevisionPolicy, String> {
    fun getById(id: String): RevisionPolicy
}
