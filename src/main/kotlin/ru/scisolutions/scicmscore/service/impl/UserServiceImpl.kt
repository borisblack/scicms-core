package ru.scisolutions.scicmscore.service.impl

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.entity.User
import ru.scisolutions.scicmscore.repository.UserRepository
import ru.scisolutions.scicmscore.service.UserService

@Service
@Repository
@Transactional
class UserServiceImpl(private val userRepository: UserRepository) : UserService {
    @Transactional(readOnly = true)
    override fun findByUsername(username: String): User? = userRepository.findByUsername(username)
}