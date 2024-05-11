package ru.scisolutions.scicmscore.engine.persistence.service

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.engine.persistence.entity.Lifecycle
import ru.scisolutions.scicmscore.engine.persistence.repository.LifecycleRepository

@Service
@Repository
@Transactional
class LifecycleService(
    private val lifecycleRepository: LifecycleRepository
) {
    @Transactional(readOnly = true)
    fun findById(id: String): Lifecycle? = lifecycleRepository.findById(id).orElse(null)

    @Transactional(readOnly = true)
    fun getById(id: String): Lifecycle = lifecycleRepository.findById(id).orElseThrow {
        IllegalArgumentException("Lifecycle [$id] not found.")
    }

    @Transactional(readOnly = true)
    fun getDefault(id: String): Lifecycle = lifecycleRepository.findById(Lifecycle.DEFAULT_LIFECYCLE_ID).orElseThrow {
        IllegalArgumentException("Default lifecycle not found.")
    }
}