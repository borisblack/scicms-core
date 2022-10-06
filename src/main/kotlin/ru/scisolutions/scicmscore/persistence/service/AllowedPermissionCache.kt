package ru.scisolutions.scicmscore.persistence.service

import ru.scisolutions.scicmscore.persistence.entity.AllowedPermission

interface AllowedPermissionCache {
    fun findAllByItemName(itemName: String): List<AllowedPermission>
}