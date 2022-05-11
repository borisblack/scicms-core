package ru.scisolutions.scicmscore.service.impl

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.persistence.entity.User
import ru.scisolutions.scicmscore.persistence.repository.UserRepository
import ru.scisolutions.scicmscore.service.UserService
import java.util.concurrent.TimeUnit

@Service
@Repository
@Transactional
class UserServiceImpl(
    dataProps: DataProps,
    private val userRepository: UserRepository
) : UserService {
    private val userCache: Cache<String, User> = CacheBuilder.newBuilder()
        .expireAfterWrite(dataProps.userCacheExpirationMinutes, TimeUnit.MINUTES)
        .build()

    @Transactional(readOnly = true)
    override fun findByUsername(username: String): User? = userRepository.findByUsername(username)

    @Transactional(readOnly = true)
    override fun getByUsername(username: String): User = userCache.get(username) { userRepository.getByUsername(username) }
}