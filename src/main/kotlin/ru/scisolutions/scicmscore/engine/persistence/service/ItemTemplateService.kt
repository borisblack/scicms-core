package ru.scisolutions.scicmscore.engine.persistence.service

import jakarta.persistence.EntityManager
import org.hibernate.Session
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.engine.persistence.entity.ItemTemplate
import ru.scisolutions.scicmscore.engine.persistence.repository.ItemTemplateRepository
import ru.scisolutions.scicmscore.engine.util.Acl

@Service
@Repository
@Transactional
class ItemTemplateService(
    private val permissionService: PermissionService,
    private val em: EntityManager,
    private val itemTemplateRepository: ItemTemplateRepository
) {
    @Transactional(readOnly = true)
    fun findAll(): Iterable<ItemTemplate> = itemTemplateRepository.findAll()

    @Transactional(readOnly = true)
    fun getById(id: String): ItemTemplate =
        itemTemplateRepository.findById(id).orElseThrow { IllegalArgumentException("Item template with ID [$id] not found.") }

    @Transactional(readOnly = true)
    fun findByName(name: String): ItemTemplate?  = findByNaturalId(name)

    private fun findByNaturalId(name: String): ItemTemplate? {
        val session = em.delegate as Session
        return session.byNaturalId(ItemTemplate::class.java)
            .using("name", name)
            .load()
    }

    @Transactional(readOnly = true)
    fun getByName(name: String): ItemTemplate = findByNaturalId(name)
        ?: throw IllegalArgumentException("Item template [$name] not found.")

    @Transactional(readOnly = true)
    fun findByNameForWrite(name: String): ItemTemplate? = findByNameWithACL(name, Acl.Mask.WRITE)

    private fun findByNameWithACL(name: String, accessMask: Acl.Mask): ItemTemplate? =
        itemTemplateRepository.findByNameWithACL(name, permissionService.idsByAccessMask(accessMask))

    fun save(itemTemplate: ItemTemplate): ItemTemplate =
        itemTemplateRepository.save(itemTemplate)

    fun deleteByName(name: String) = itemTemplateRepository.deleteByName(name)
}