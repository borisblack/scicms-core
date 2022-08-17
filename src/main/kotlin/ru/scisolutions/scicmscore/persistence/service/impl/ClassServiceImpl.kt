package ru.scisolutions.scicmscore.persistence.service.impl

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import ru.scisolutions.scicmscore.persistence.service.ClassService

@Service
class ClassServiceImpl(private val applicationContext: ApplicationContext):
    ru.scisolutions.scicmscore.persistence.service.ClassService {
    private val classCache: Cache<String, Class<*>> = CacheBuilder.newBuilder().build()
    private val instanceCache: Cache<Class<*>, Any> = CacheBuilder.newBuilder().build()

    override fun getClass(className: String): Class<*> = classCache.get(className) { Class.forName(className) }

    override fun <T> getInstance(clazz: Class<T>): T = instanceCache.get(clazz) {
        try {
            applicationContext.getBean(clazz)
        } catch (e: BeansException) {
            clazz.getConstructor().newInstance()
        }
    } as T

    override fun <T> getCastInstance(className: String?, castType: Class<T>): T? {
        val clazz = if (className != null) getClass(className) else return null
        return if (castType.isAssignableFrom(clazz)) getInstance(clazz) as T else null
    }
}