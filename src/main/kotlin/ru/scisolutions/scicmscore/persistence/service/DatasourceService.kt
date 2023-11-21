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
    @Transactional(readOnly = true)
    fun findAll(): Iterable<Datasource> =
        datasourceRepository.findAll()

    @Transactional(readOnly = true)
    fun getById(id: String): Datasource =
        datasourceRepository.getById(id)

    @Transactional(readOnly = true)
    fun getByName(name: String): Datasource =
        datasourceRepository.getByName(name)
}