package ru.scisolutions.scicmscore.service

import ru.scisolutions.scicmscore.persistence.entity.Permission
import ru.scisolutions.scicmscore.util.ACL.Mask

interface PermissionService {
    val defaultPermission: Permission

    fun getIdsForRead(): Set<String>

    fun getIdsForWrite(): Set<String>

    fun getIdsForCreate(): Set<String>

    fun getIdsForDelete(): Set<String>

    fun getIdsForAdministration(): Set<String>

    fun getIdsFor(accessMask: Mask): Set<String>

    fun findAllForRead(): List<Permission>

    fun findAllForWrite(): List<Permission>

    fun findAllForCreate(): List<Permission>

    fun findAllForDelete(): List<Permission>

    fun findAllForAdministration(): List<Permission>
}