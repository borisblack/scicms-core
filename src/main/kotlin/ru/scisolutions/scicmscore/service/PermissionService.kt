package ru.scisolutions.scicmscore.service

import ru.scisolutions.scicmscore.persistence.entity.Permission

interface PermissionService {
    val defaultPermission: Permission
}