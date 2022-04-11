package ru.scisolutions.scicmscore.repository

import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.entity.Permission

interface PermissionRepository : CrudRepository<Permission, String> {
    fun getById(id: String): Permission
}