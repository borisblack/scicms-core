package ru.scisolutions.scicmscore.engine.persistence.service

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.engine.persistence.entity.Dataset
import ru.scisolutions.scicmscore.engine.persistence.repository.DatasetRepository
import ru.scisolutions.scicmscore.engine.util.Acl.Mask

@Service
@Repository
@Transactional
class DatasetService(
    private val permissionService: PermissionService,
    private val datasetRepository: DatasetRepository,
) {
    fun getById(id: String): Dataset = datasetRepository.findById(id).orElseThrow { IllegalArgumentException("Dataset [$id] not found") }

    @Transactional(readOnly = true)
    fun findByNameForRead(name: String): Dataset? = findByNameFor(name, Mask.READ)

    private fun findByNameFor(name: String, accessMask: Mask): Dataset? = datasetRepository.findByNameWithACL(name, permissionService.idsByAccessMask(accessMask))

    @Transactional(readOnly = true)
    fun existsByDatasourceId(id: String): Boolean = datasetRepository.existsByDatasourceId(id)
}
