package ru.scisolutions.scicmscore.persistence.repository

import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.Dashboard

interface DashboardRepository : CrudRepository<Dashboard, String>