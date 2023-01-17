package ru.scisolutions.scicmscore.persistence.service.impl

import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.Dataset
import ru.scisolutions.scicmscore.persistence.repository.DatasetRepository
import ru.scisolutions.scicmscore.persistence.service.DatasetService
import ru.scisolutions.scicmscore.util.Acl.Mask

@Service
@Repository
@Transactional
class DatasetServiceImpl(private val datasetRepository: DatasetRepository) : DatasetService {
    @Transactional(readOnly = true)
    override fun findByNameForRead(id: String): Dataset? = findByNameFor(id, Mask.READ)

    private fun findByNameFor(id: String, accessMask: Mask): Dataset? {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw AccessDeniedException("User is not authenticated")

        return datasetRepository.findByNameWithACL(id, accessMask.mask, authentication.name, AuthorityUtils.authorityListToSet(authentication.authorities))
    }
}