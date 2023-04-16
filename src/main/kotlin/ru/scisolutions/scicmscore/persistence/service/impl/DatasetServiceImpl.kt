package ru.scisolutions.scicmscore.persistence.service.impl

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.Dataset
import ru.scisolutions.scicmscore.persistence.repository.DatasetRepository
import ru.scisolutions.scicmscore.persistence.service.DatasetService
import ru.scisolutions.scicmscore.persistence.service.PermissionCache
import ru.scisolutions.scicmscore.util.Acl.Mask

@Service
@Repository
@Transactional
class DatasetServiceImpl(
    private val permissionCache: PermissionCache,
    private val datasetRepository: DatasetRepository
) : DatasetService {
    override fun getById(id: String): Dataset =
        datasetRepository.findById(id).orElseThrow { IllegalArgumentException("Dataset with ID [$id] not found") }

    override fun findByNameForRead(name: String): Dataset? =
        findByNameFor(name, Mask.READ)

    private fun findByNameFor(name: String, accessMask: Mask): Dataset? =
        datasetRepository.findByNameWithACL(name, permissionCache.idsByAccessMask(accessMask))
}