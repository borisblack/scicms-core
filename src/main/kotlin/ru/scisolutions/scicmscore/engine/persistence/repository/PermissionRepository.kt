package ru.scisolutions.scicmscore.engine.persistence.repository

import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.engine.persistence.entity.Permission

interface PermissionRepository : CrudRepository<Permission, String>
