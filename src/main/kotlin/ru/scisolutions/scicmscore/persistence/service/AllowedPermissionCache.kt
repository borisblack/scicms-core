package ru.scisolutions.scicmscore.persistence.service

import ru.scisolutions.scicmscore.persistence.entity.AllowedPermission

interface AllowedPermissionCache {
    operator fun get(itemName: String): List<AllowedPermission>
}