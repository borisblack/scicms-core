package ru.scisolutions.scicmscore.engine.service.impl

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.config.props.DataProps
import ru.scisolutions.scicmscore.engine.service.ClassService
import java.util.concurrent.TimeUnit

@Service
class ClassServiceImpl(
    dataProps: DataProps,
    private val applicationContext: ApplicationContext
) : ClassService {
    private val instanceCache: Cache<Class<*>, Any> = CacheBuilder.newBuilder()
        .expireAfterWrite(dataProps.cacheExpirationMinutes, TimeUnit.MINUTES)
        .build()

    override fun <T> getInstance(clazz: Class<T>): T = instanceCache.get(clazz) {
        try {
            applicationContext.getBean(clazz)
        } catch (e: BeansException) {
            clazz.getConstructor().newInstance()
        }
    } as T

    override fun <T> getCastInstance(className: String?, castType: Class<T>): T? {
        val clazz = if (className != null) Class.forName(className) else return null
        return if (castType.isAssignableFrom(clazz)) getInstance(clazz) as T else null
    }
}