package ru.scisolutions.scicmscore.persistence.service

import jakarta.persistence.EntityManager
import org.hibernate.Session
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.Datasource
import ru.scisolutions.scicmscore.persistence.repository.DatasourceRepository
import ru.scisolutions.scicmscore.util.Acl

@Service
@Repository
@Transactional
class DatasourceService(
    private val em: EntityManager,
    private val permissionService: PermissionService,
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

    @Transactional(readOnly = true)
    fun findByIdForRead(name: String): Datasource? =
        findByIdFor(name, Acl.Mask.READ)

    private fun findByIdFor(id: String, accessMask: Acl.Mask): Datasource? =
        datasourceRepository.findByIdWithACL(id, permissionService.idsByAccessMask(accessMask))

    @Transactional(readOnly = true)
    fun findByNameForRead(name: String): Datasource? =
        findByNameFor(name, Acl.Mask.READ)

    private fun findByNameFor(name: String, accessMask: Acl.Mask): Datasource? =
        datasourceRepository.findByNameWithACL(name, permissionService.idsByAccessMask(accessMask))
}