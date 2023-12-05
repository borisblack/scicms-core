package ru.scisolutions.scicmscore.persistence.service

import jakarta.persistence.EntityManager
import org.hibernate.Session
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.Datasource
import ru.scisolutions.scicmscore.persistence.repository.DatasourceRepository

@Service
@Repository
@Transactional
class DatasourceService(
    private val em: EntityManager,
    private val datasourceRepository: DatasourceRepository
) {
    @Transactional(readOnly = true)
    fun findAll(): Iterable<Datasource> =
        datasourceRepository.findAll()

    @Transactional(readOnly = true)
    fun getById(id: String): Datasource =
        datasourceRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Datasource [$id] not found.") }

    @Transactional(readOnly = true)
    fun findByName(name: String): Datasource? =
        findByNaturalId(name)

    private fun findByNaturalId(name: String): Datasource? {
        val session = em.delegate as Session
        return session.byNaturalId(Datasource::class.java)
            .using("name", name)
            .load()
    }

    @Transactional(readOnly = true)
    fun getByName(name: String): Datasource =
        findByNaturalId(name) ?: throw IllegalArgumentException("Datasource [$name] not found.")
}