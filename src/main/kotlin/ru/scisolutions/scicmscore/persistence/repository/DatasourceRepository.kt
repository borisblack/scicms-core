package ru.scisolutions.scicmscore.persistence.repository

import org.springframework.data.repository.CrudRepository
import ru.scisolutions.scicmscore.persistence.entity.Datasource

interface DatasourceRepository : CrudRepository<Datasource, String>