package ru.scisolutions.scicmscore.persistence.service

import jakarta.persistence.EntityManager
import org.hibernate.Session
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.Property
import ru.scisolutions.scicmscore.persistence.repository.PropertyRepository

@Service
@Repository
@Transactional
class PropertyService(
    private val em: EntityManager,
    private val propertyRepository: PropertyRepository
) {
    @Transactional(readOnly = true)
    fun findAll(): Iterable<Property> = propertyRepository.findAll()

    @Transactional(readOnly = true)
    fun findByName(name: String): Property? = findByNaturalId(name)

    private fun findByNaturalId(name: String): Property? {
        val session = em.delegate as Session
        return session.byNaturalId(Property::class.java)
            .using("name", name)
            .load()
    }

    @Transactional(readOnly = true)
    fun getByName(name: String): Property = findByNaturalId(name)
        ?: throw IllegalArgumentException("Property [$name] not found.")

    fun deleteByName(name: String) = propertyRepository.deleteByName(name)
}