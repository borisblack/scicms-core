package ru.scisolutions.scicmscore.persistence.service.impl

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.ItemTemplate
import ru.scisolutions.scicmscore.persistence.repository.ItemTemplateRepository
import ru.scisolutions.scicmscore.persistence.service.ItemTemplateService
import ru.scisolutions.scicmscore.persistence.service.PermissionCache
import ru.scisolutions.scicmscore.util.Acl
import javax.persistence.EntityManager

@Service
@Repository
@Transactional
class ItemTemplateServiceImpl(
    private val em: EntityManager,
    private val permissionCache: PermissionCache,
    private val itemTemplateRepository: ItemTemplateRepository
) : ItemTemplateService {
    @Transactional(readOnly = true)
    override fun findAll(): Iterable<ItemTemplate> = itemTemplateRepository.findAll()

    @Transactional(readOnly = true)
    override fun findByName(name: String): ItemTemplate?  = itemTemplateRepository.findByName(name)

    @Transactional(readOnly = true)
    override fun findByNameForWrite(name: String): ItemTemplate? = findByNameWithACL(name, Acl.Mask.WRITE)

    private fun findByNameWithACL(name: String, accessMask: Acl.Mask): ItemTemplate? =
        itemTemplateRepository.findByNameWithACL(name, permissionCache.idsByAccessMask(accessMask))

    override fun save(itemTemplate: ItemTemplate): ItemTemplate {
        val savedItemTemplate = itemTemplateRepository.save(itemTemplate)
        em.flush()
        em.detach(savedItemTemplate)

        return savedItemTemplate
    }

    override fun deleteByName(name: String) = itemTemplateRepository.deleteByName(name)
}