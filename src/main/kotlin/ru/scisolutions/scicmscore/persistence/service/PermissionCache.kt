package ru.scisolutions.scicmscore.persistence.service

import ru.scisolutions.scicmscore.persistence.entity.Permission
import ru.scisolutions.scicmscore.util.Acl.Mask

interface PermissionCache {
    fun getDefault(): Permission

    fun findIdsForRead(): Set<String>

    fun findIdsForWrite(): Set<String>

    fun findIdsForCreate(): Set<String>

    fun findIdsForDelete(): Set<String>

    fun findIdsForAdministration(): Set<String>

    fun findIdsFor(accessMask: Mask): Set<String>
}