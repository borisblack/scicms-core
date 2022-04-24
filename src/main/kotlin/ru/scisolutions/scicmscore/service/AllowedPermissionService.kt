package ru.scisolutions.scicmscore.service

import ru.scisolutions.scicmscore.persistence.entity.AllowedPermission

interface AllowedPermissionService {
    fun save(allowedPermission: AllowedPermission): AllowedPermission
}