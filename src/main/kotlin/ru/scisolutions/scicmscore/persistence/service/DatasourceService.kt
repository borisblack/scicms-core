package ru.scisolutions.scicmscore.persistence.service

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.Datasource
import ru.scisolutions.scicmscore.persistence.repository.DatasourceRepository

@Service
@Repository
@Transactional
class DatasourceService(private val datasourceRepository: DatasourceRepository) {
    fun findAll(): Iterable<Datasource> =
        datasourceRepository.findAll()

    fun getByName(name: String): Datasource =
        datasourceRepository.getByName(name)
}