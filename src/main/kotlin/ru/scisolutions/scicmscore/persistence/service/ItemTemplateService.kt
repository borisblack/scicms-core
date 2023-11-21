package ru.scisolutions.scicmscore.persistence.service

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.ItemTemplate
import ru.scisolutions.scicmscore.persistence.repository.ItemTemplateRepository
import ru.scisolutions.scicmscore.util.Acl
import javax.persistence.EntityManager

@Service
@Repository
@Transactional
class ItemTemplateService(
    private val em: EntityManager,
    private val permissionCache: PermissionCache,
    private val itemTemplateRepository: ItemTemplateRepository
) {
    @Transactional(readOnly = true)
    fun findAll(): Iterable<ItemTemplate> = itemTemplateRepository.findAll()

    @Transactional(readOnly = true)
    fun findByName(name: String): ItemTemplate?  = itemTemplateRepository.findByName(name)

    @Transactional(readOnly = true)
    fun findByNameForWrite(name: String): ItemTemplate? = findByNameWithACL(name, Acl.Mask.WRITE)

    private fun findByNameWithACL(name: String, accessMask: Acl.Mask): ItemTemplate? =
        itemTemplateRepository.findByNameWithACL(name, permissionCache.idsByAccessMask(accessMask))

    fun save(itemTemplate: ItemTemplate): ItemTemplate {
        val savedItemTemplate = itemTemplateRepository.save(itemTemplate)
        em.flush()
        em.detach(savedItemTemplate)

        return savedItemTemplate
    }

    fun deleteByName(name: String) = itemTemplateRepository.deleteByName(name)
}