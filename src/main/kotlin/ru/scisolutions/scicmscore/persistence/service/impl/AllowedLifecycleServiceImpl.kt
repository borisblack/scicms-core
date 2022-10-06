package ru.scisolutions.scicmscore.persistence.service.impl

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.AllowedLifecycle
import ru.scisolutions.scicmscore.persistence.repository.AllowedLifecycleRepository
import ru.scisolutions.scicmscore.persistence.service.AllowedLifecycleService

@Service
@Repository
@Transactional
class AllowedLifecycleServiceImpl(
    private val allowedLifecycleRepository: AllowedLifecycleRepository
) : AllowedLifecycleService {
    @Transactional(readOnly = true)
    override fun findAllByItemName(itemName: String): List<AllowedLifecycle> = allowedLifecycleRepository.findAllByItemName(itemName)
}