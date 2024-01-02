package ru.scisolutions.scicmscore.persistence.service

import jakarta.persistence.EntityManager
import org.hibernate.Session
import org.hibernate.stat.Statistics
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.Access
import ru.scisolutions.scicmscore.persistence.entity.Permission
import ru.scisolutions.scicmscore.persistence.repository.AccessRepository
import ru.scisolutions.scicmscore.persistence.repository.PermissionRepository
import ru.scisolutions.scicmscore.util.Acl

@Service
@Repository
@Transactional
class PermissionService(
    private val accessRepository: AccessRepository,
    private val permissionRepository: PermissionRepository,
    private val cacheService: CacheService,
    private val entityManager: EntityManager
) {
    @Transactional(readOnly = true)
    fun findById(id: String): Permission? = permissionRepository.findById(id).orElse(null)

    @Transactional(readOnly = true)
    fun getDefault(): Permission = findById(Permission.DEFAULT_PERMISSION_ID)
        ?: throw IllegalArgumentException("Default permission not found")

    // @Cacheable("nativeQueryCache")
    @Transactional(readOnly = true)
    fun idsForRead(): Set<String> =
        idsByAccessMask(Acl.Mask.READ)

    // @Cacheable("nativeQueryCache")
    @Transactional(readOnly = true)
    fun idsByAccessMask(accessMask: Acl.Mask): Set<String> =
        idsByMask(accessMask.mask)

    @Transactional(readOnly = true)
    fun idsByMask(mask: Set<Int>): Set<String> {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw AccessDeniedException("User is not authenticated")

        val accessList = accessRepository.findAllByMask(
            mask,
            authentication.name,
            AuthorityUtils.authorityListToSet(authentication.authorities)
        ).sortedWith(Access.AccessComparator())

        val permissionAccessMap = accessList.groupBy { it.sourceId }

        val session: Session = entityManager.delegate as Session
        val stats: Statistics = session.sessionFactory.statistics

        // val testAccessQueryString = "select a from Access a where a.label = :label";
        // val testAccessQuery = entityManager.createQuery(testAccessQueryString, Access::class.java)
        //     .setParameter("label", "Default ROLE_ADMIN Access")
        //     .setHint(org.hibernate.jpa.HibernateHints.HINT_CACHEABLE, true)
        // val testAccList: List<Access> = testAccessQuery.resultList

        // val accessQuery = entityManager.createQuery(Acl.ACCESS_JPQL_SNIPPET, Access::class.java)
        //     .setParameter("mask", mask)
        //     .setParameter("username", authentication.name)
        //     .setParameter("roles", AuthorityUtils.authorityListToSet(authentication.authorities))
        //     .setHint(org.hibernate.jpa.HibernateHints.HINT_CACHEABLE, true)
        // val accList: List<Access> = accessQuery.resultList

        // val itemStats = stats.getNaturalIdStatistics(Item::class.qualifiedName as String)
        // val testAccessQueryStats = stats.getQueryStatistics(testAccessQueryString)
        // val accessStats = stats.getQueryStatistics(Acl.ACCESS_JPQL_SNIPPET)
        //
        // cacheService.printStatistics()

        return permissionAccessMap.filterValues { it[0].granting }.keys.toSet()
    }
}