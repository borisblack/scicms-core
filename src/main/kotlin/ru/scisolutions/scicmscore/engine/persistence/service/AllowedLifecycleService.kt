package ru.scisolutions.scicmscore.engine.persistence.service

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.engine.persistence.entity.AllowedLifecycle
import ru.scisolutions.scicmscore.engine.persistence.repository.AllowedLifecycleRepository

@Service
@Repository
@Transactional
class AllowedLifecycleService(
    private val allowedLifecycleRepository: AllowedLifecycleRepository,
) {
    @Transactional(readOnly = true)
    fun findAllByItemName(itemName: String): List<AllowedLifecycle> = allowedLifecycleRepository.findAllByItemName(itemName)
}
