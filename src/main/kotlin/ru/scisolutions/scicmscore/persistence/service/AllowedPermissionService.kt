package ru.scisolutions.scicmscore.persistence.service

import ru.scisolutions.scicmscore.persistence.entity.AllowedPermission

interface AllowedPermissionService {
    fun findPermissionIdsByItemName(itemName: String): List<String>

    fun save(allowedPermission: AllowedPermission): AllowedPermission
}