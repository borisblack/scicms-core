package ru.scisolutions.scicmscore.persistence.repository

import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.Permission

interface PermissionRepository : CrudRepository<Permission, String>