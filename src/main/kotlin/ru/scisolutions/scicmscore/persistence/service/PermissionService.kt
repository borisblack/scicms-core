package ru.scisolutions.scicmscore.persistence.service

import ru.scisolutions.scicmscore.persistence.entity.Permission
import ru.scisolutions.scicmscore.util.Acl.Mask

interface PermissionService {
    fun findById(id: String): Permission?

    fun findIdsFor(mask: Set<Int>, username: String, roles: Set<String>): Set<String>

    fun findAllForRead(): List<Permission>

    fun findAllForWrite(): List<Permission>

    fun findAllForCreate(): List<Permission>

    fun findAllForDelete(): List<Permission>

    fun findAllForAdministration(): List<Permission>
}