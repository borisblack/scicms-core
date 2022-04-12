package ru.scisolutions.scicmscore.repository

import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.entity.AllowedPermission

interface AllowedPermissionRepository : CrudRepository<AllowedPermission, String>