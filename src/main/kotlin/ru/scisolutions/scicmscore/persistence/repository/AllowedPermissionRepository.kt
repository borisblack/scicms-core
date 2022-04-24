package ru.scisolutions.scicmscore.persistence.repository

import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.AllowedPermission

interface AllowedPermissionRepository : CrudRepository<AllowedPermission, String>