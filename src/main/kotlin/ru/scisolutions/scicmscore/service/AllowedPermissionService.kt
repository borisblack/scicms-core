package ru.scisolutions.scicmscore.service

import ru.scisolutions.scicmscore.persistence.entity.AllowedPermission

interface AllowedPermissionService {
    fun findPermissionIdsByItemName(itemName: String): Set<String>

    fun save(allowedPermission: AllowedPermission): AllowedPermission
}