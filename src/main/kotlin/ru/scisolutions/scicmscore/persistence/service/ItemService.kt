package ru.scisolutions.scicmscore.persistence.service

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.Item
import ru.scisolutions.scicmscore.persistence.repository.ItemRepository
import ru.scisolutions.scicmscore.util.Acl
import javax.persistence.EntityManager

@Service
@Repository
@Transactional
class ItemService(
    private val em: EntityManager,
    private val permissionCache: PermissionCache,
    private val itemRepository: ItemRepository
) {
    @Transactional(readOnly = true)
    fun findAll(): Iterable<Item> = itemRepository.findAll()

    @Transactional(readOnly = true)
    fun findByName(name: String): Item? = itemRepository.findByName(name)

    @Transactional(readOnly = true)
    fun findByNameForWrite(name: String): Item? = findByNameWithACL(name, Acl.Mask.WRITE)

    fun findByIdForDelete(id: String): Item? = findByIdWithACL(id, Acl.Mask.DELETE)

    @Transactional(readOnly = true)
    fun canCreate(name: String): Boolean = findByNameWithACL(name, Acl.Mask.CREATE) != null

    private fun findByIdWithACL(id: String, accessMask: Acl.Mask): Item? =
        itemRepository.findByIdWithACL(id, permissionCache.idsByAccessMask(accessMask))

    private fun findByNameWithACL(name: String, accessMask: Acl.Mask): Item? =
        itemRepository.findByNameWithACL(name, permissionCache.idsByAccessMask(accessMask))

    fun save(item: Item): Item {
        val savedItem = itemRepository.save(item)
        em.flush()
        em.detach(savedItem)

        return savedItem
    }

    fun deleteByName(name: String) = itemRepository.deleteByName(name)
}