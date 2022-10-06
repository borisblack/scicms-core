package ru.scisolutions.scicmscore.persistence.service.impl

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.Lifecycle
import ru.scisolutions.scicmscore.persistence.repository.LifecycleRepository
import ru.scisolutions.scicmscore.persistence.service.LifecycleService

@Service
@Repository
@Transactional
class LifecycleServiceImpl(
    private val lifecycleRepository: LifecycleRepository
) : LifecycleService {
    @Transactional(readOnly = true)
    override fun findById(id: String): Lifecycle? = lifecycleRepository.findById(id).orElse(null)
}