package ru.scisolutions.scicmscore.persistence.service

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.Lifecycle
import ru.scisolutions.scicmscore.persistence.repository.LifecycleRepository

@Service
@Repository
@Transactional
class LifecycleService(
    private val lifecycleRepository: LifecycleRepository
) {
    @Transactional(readOnly = true)
    fun findById(id: String): Lifecycle? = lifecycleRepository.findById(id).orElse(null)
}