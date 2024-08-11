package ru.scisolutions.scicmscore.engine.persistence.repository

import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.engine.persistence.entity.Lifecycle

interface LifecycleRepository : CrudRepository<Lifecycle, String>
