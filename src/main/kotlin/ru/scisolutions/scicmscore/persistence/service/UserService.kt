package ru.scisolutions.scicmscore.persistence.service

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.persistence.entity.User
import ru.scisolutions.scicmscore.persistence.repository.UserRepository

@Service
@Repository
@Transactional
class UserService(
    private val userRepository: UserRepository
) {
    @Transactional(readOnly = true)
    fun findByUsername(username: String): User? = userRepository.findByUsername(username)

    fun save(user: User): User =
        userRepository.save(user)
}