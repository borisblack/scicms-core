package ru.scisolutions.scicmscore.persistence.service

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.Dataset
import ru.scisolutions.scicmscore.persistence.repository.DatasetRepository
import ru.scisolutions.scicmscore.util.Acl

@Service
@Repository
@Transactional
class DatasetService(
    private val permissionCache: PermissionCache,
    private val datasetRepository: DatasetRepository
) {
    fun getById(id: String): Dataset =
        datasetRepository.findById(id).orElseThrow { IllegalArgumentException("Dataset with ID [$id] not found") }

    @Transactional(readOnly = true)
    fun findByNameForRead(name: String): Dataset? =
        findByNameFor(name, Acl.Mask.READ)

    private fun findByNameFor(name: String, accessMask: Acl.Mask): Dataset? =
        datasetRepository.findByNameWithACL(name, permissionCache.idsByAccessMask(accessMask))

    @Transactional(readOnly = true)
    fun existsByDatasourceId(id: String): Boolean =
        datasetRepository.existsByDatasourceId(id)
}