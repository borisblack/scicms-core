package ru.scisolutions.scicmscore.persistence.repository

import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.Lifecycle

interface LifecycleRepository : CrudRepository<Lifecycle, String>