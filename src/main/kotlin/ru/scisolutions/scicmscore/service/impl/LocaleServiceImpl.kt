package ru.scisolutions.scicmscore.service.impl

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.repository.LocaleRepository
import ru.scisolutions.scicmscore.service.LocaleService

@Service
@Repository
@Transactional
class LocaleServiceImpl(private val localeRepository: LocaleRepository) : LocaleService {
    @Transactional(readOnly = true)
    override fun existsByName(name: String): Boolean = localeRepository.existsByName(name)
}