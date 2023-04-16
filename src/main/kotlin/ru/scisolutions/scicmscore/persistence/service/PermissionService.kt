package ru.scisolutions.scicmscore.persistence.service

import ru.scisolutions.scicmscore.persistence.entity.Permission

interface PermissionService {
    fun findById(id: String): Permission?

    fun getDefault(): Permission

    fun findIdsByMask(mask: Set<Int>): Set<String>
}