package ru.scisolutions.scicmscore.engine.persistence.service

import jakarta.persistence.EntityManager
import org.hibernate.Session
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.engine.persistence.entity.Item
import ru.scisolutions.scicmscore.engine.persistence.repository.ItemRepository
import ru.scisolutions.scicmscore.engine.util.Acl

@Service
@Repository
@Transactional
class ItemService(
    private val permissionService: PermissionService,
    private val em: EntityManager,
    private val itemRepository: ItemRepository,
) {
    @Transactional(readOnly = true)
    fun findAll(): Iterable<Item> = itemRepository.findAll()

    @Transactional(readOnly = true)
    fun getById(id: String): Item = itemRepository.findById(id).orElseThrow { IllegalArgumentException("Item with ID [$id] not found.") }

    @Transactional(readOnly = true)
    fun findByName(name: String): Item? = findByNaturalId(name)

    private fun findByNaturalId(name: String): Item? {
        val session = em.delegate as Session
        return session.byNaturalId(Item::class.java)
            .using("name", name)
            .load()
    }

    @Transactional(readOnly = true)
    fun getByName(name: String): Item = findByNaturalId(name)
        ?: throw IllegalArgumentException("Item [$name] not found.")

    @Transactional(readOnly = true)
    fun getMedia(): Item = findByNaturalId(Item.MEDIA_ITEM_NAME)
        ?: throw IllegalArgumentException("Media item not found.")

    @Transactional(readOnly = true)
    fun findByNameForWrite(name: String): Item? = findByNameWithACL(name, Acl.Mask.WRITE)

    @Transactional(readOnly = true)
    fun findByIdForDelete(id: String): Item? = findByIdWithACL(id, Acl.Mask.DELETE)

    @Transactional(readOnly = true)
    fun canCreate(name: String): Boolean = findByNameWithACL(name, Acl.Mask.CREATE) != null

    private fun findByIdWithACL(id: String, accessMask: Acl.Mask): Item? = itemRepository.findByIdWithACL(id, permissionService.idsByAccessMask(accessMask))

    private fun findByNameWithACL(name: String, accessMask: Acl.Mask): Item? = itemRepository.findByNameWithACL(name, permissionService.idsByAccessMask(accessMask))

    @Transactional(readOnly = true)
    fun existsByDatasourceId(id: String): Boolean = itemRepository.existsByDatasourceId(id)

    fun save(item: Item): Item = itemRepository.save(item)

    fun deleteByName(name: String) = itemRepository.deleteByName(name)
}
