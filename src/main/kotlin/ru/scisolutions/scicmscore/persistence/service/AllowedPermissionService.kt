package ru.scisolutions.scicmscore.persistence.service

import ru.scisolutions.scicmscore.persistence.entity.AllowedPermission

interface AllowedPermissionService {
    fun findAllByItemName(itemName: String): List<AllowedPermission>

    fun save(allowedPermission: AllowedPermission): AllowedPermission
}