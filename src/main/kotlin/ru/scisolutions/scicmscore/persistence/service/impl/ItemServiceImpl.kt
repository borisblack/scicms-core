package ru.scisolutions.scicmscore.persistence.service.impl

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.repository.ItemRepository
import ru.scisolutions.scicmscore.persistence.service.ItemService
import ru.scisolutions.scicmscore.persistence.service.PermissionCache
import ru.scisolutions.scicmscore.util.Acl.Mask
import javax.persistence.EntityManager

@Service
@Repository
@Transactional
class ItemServiceImpl(
    private val em: EntityManager,
    private val permissionCache: PermissionCache,
    private val itemRepository: ItemRepository
) : ItemService {
    @Transactional(readOnly = true)
    override fun findAll(): Iterable<Item> = itemRepository.findAll()

    @Transactional(readOnly = true)
    override fun findByName(name: String): Item? = itemRepository.findByName(name)

    @Transactional(readOnly = true)
    override fun findByNameForWrite(name: String): Item? = findByNameWithACL(name, Mask.WRITE)

    override fun findByIdForDelete(id: String): Item? = findByIdWithACL(id, Mask.DELETE)

    @Transactional(readOnly = true)
    override fun canCreate(name: String): Boolean = findByNameWithACL(name, Mask.CREATE) != null

    private fun findByIdWithACL(id: String, accessMask: Mask): Item? =
        itemRepository.findByIdWithACL(id, permissionCache.idsByAccessMask(accessMask))

    private fun findByNameWithACL(name: String, accessMask: Mask): Item? =
        itemRepository.findByNameWithACL(name, permissionCache.idsByAccessMask(accessMask))

    override fun save(item: Item): Item {
        val savedItem = itemRepository.save(item)
        em.flush()
        em.detach(savedItem)

        return savedItem
    }

    override fun deleteByName(name: String) = itemRepository.deleteByName(name)
}