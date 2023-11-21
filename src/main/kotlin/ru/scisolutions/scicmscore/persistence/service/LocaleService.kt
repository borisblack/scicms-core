package ru.scisolutions.scicmscore.persistence.service

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.repository.LocaleRepository

@Service
@Repository
@Transactional
class LocaleService(private val localeRepository: LocaleRepository) {
    @Transactional(readOnly = true)
    fun existsByName(name: String): Boolean = localeRepository.existsByName(name)
}