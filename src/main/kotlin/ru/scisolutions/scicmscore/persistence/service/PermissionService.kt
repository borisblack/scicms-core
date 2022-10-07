package ru.scisolutions.scicmscore.persistence.service

import ru.scisolutions.scicmscore.persistence.entity.Permission

interface PermissionService {
    fun findById(id: String): Permission?

    fun getDefault(): Permission

    fun findIdsFor(mask: Set<Int>, username: String, roles: Set<String>): Set<String>
}