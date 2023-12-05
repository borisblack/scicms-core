package ru.scisolutions.scicmscore.persistence.service

import jakarta.persistence.EntityManager
import org.hibernate.Session
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.User
import ru.scisolutions.scicmscore.persistence.repository.UserRepository

@Service
@Repository
@Transactional
class UserService(
    private val em: EntityManager,
    private val userRepository: UserRepository
) {
    @Transactional(readOnly = true)
    fun findByUsername(username: String): User? = findByNaturalId(username)

    private fun findByNaturalId(username: String): User? {
        val session = em.delegate as Session
        return session.byNaturalId(User::class.java)
            .using("username", username)
            .load()
    }

    @Transactional(readOnly = true)
    fun getByUsername(username: String): User =
        findByNaturalId(username) ?: throw IllegalArgumentException("User [$username] not found.")

    @Transactional(readOnly = true)
    fun getCurrent(): User {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw AccessDeniedException("User is not authenticated.")

        return getByUsername(authentication.name)
    }

    fun save(user: User): User =
        userRepository.save(user)
}