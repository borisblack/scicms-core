package ru.scisolutions.scicmscore.persistence.service

import ru.scisolutions.scicmscore.util.Acl.Mask

interface PermissionCache {
    fun idsForRead(): Set<String>

    fun idsForWrite(): Set<String>

    fun idsForCreate(): Set<String>

    fun idsForDelete(): Set<String>

    fun idsForAdministration(): Set<String>

    fun idsByAccessMask(accessMask: Mask): Set<String>
}