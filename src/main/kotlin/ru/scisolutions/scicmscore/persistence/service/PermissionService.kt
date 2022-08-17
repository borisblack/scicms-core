package ru.scisolutions.scicmscore.persistence.service

import ru.scisolutions.scicmscore.persistence.entity.Permission
import ru.scisolutions.scicmscore.util.ACL.Mask

interface PermissionService {
    fun getDefaultPermission(): Permission

    fun findIdsForRead(): Set<String>

    fun findIdsForWrite(): Set<String>

    fun findIdsForCreate(): Set<String>

    fun findIdsForDelete(): Set<String>

    fun findIdsForAdministration(): Set<String>

    fun findIdsFor(accessMask: Mask): Set<String>

    fun findAllForRead(): List<Permission>

    fun findAllForWrite(): List<Permission>

    fun findAllForCreate(): List<Permission>

    fun findAllForDelete(): List<Permission>

    fun findAllForAdministration(): List<Permission>
}