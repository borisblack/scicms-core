package ru.scisolutions.scicmscore.persistence.service.impl

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.persistence.entity.User
import ru.scisolutions.scicmscore.persistence.repository.UserRepository
import ru.scisolutions.scicmscore.persistence.service.UserService
import java.util.concurrent.TimeUnit

@Service
@Repository
@Transactional
class UserServiceImpl(
    dataProps: DataProps,
    private val userRepository: UserRepository
) : UserService {
    private val userCache: Cache<String, User> = CacheBuilder.newBuilder()
        .expireAfterWrite(dataProps.cacheExpirationMinutes, TimeUnit.MINUTES)
        .build()

    @Transactional(readOnly = true)
    override fun getCurrentUser(): User {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw AccessDeniedException("User is not authenticated")

        return getByUsername(authentication.name)
    }

    @Transactional(readOnly = true)
    override fun getByUsername(username: String): User = userCache.get(username) { userRepository.getByUsername(username) }
}