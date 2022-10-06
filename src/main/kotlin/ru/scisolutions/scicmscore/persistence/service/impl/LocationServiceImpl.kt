package ru.scisolutions.scicmscore.persistence.service.impl

import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.Location
import ru.scisolutions.scicmscore.persistence.repository.LocationRepository
import ru.scisolutions.scicmscore.persistence.service.LocationService
import ru.scisolutions.scicmscore.util.Acl.Mask

@Service
@Repository
@Transactional
class LocationServiceImpl(private val locationRepository: LocationRepository) : LocationService {
    @Transactional(readOnly = true)
    override fun findById(id: String): Location? = locationRepository.findById(id).orElse(null)

    @Transactional(readOnly = true)
    override fun findByIdForRead(id: String): Location? = findByIdFor(id, Mask.READ)

    @Transactional(readOnly = true)
    override fun findByIdForDelete(id: String): Location? = findByIdFor(id, Mask.DELETE)

    private fun findByIdFor(id: String, accessMask: Mask): Location? {
        val authentication = SecurityContextHolder.getContext().authentication ?: return null
        return locationRepository.findByIdWithACL(id, accessMask.mask, authentication.name, AuthorityUtils.authorityListToSet(authentication.authorities))
    }

    @Transactional(readOnly = true)
    override fun getById(id: String): Location = locationRepository.getById(id)

    override fun existsById(id: String): Boolean = locationRepository.existsById(id)

    override fun save(location: Location): Location = locationRepository.save(location)

    override fun delete(location: Location) {
        locationRepository.delete(location)
    }
}