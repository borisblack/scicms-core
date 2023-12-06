package ru.scisolutions.scicmscore.engine.service

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
class ClassService(private val applicationContext: ApplicationContext) {
    fun <T> getInstance(clazz: Class<T>): T =
        try {
            applicationContext.getBean(clazz)
        } catch (e: BeansException) {
            clazz.getConstructor().newInstance()
        }

    fun <T> getCastInstance(className: String?, castType: Class<T>): T? {
        val clazz = if (className != null) Class.forName(className) else return null
        return if (castType.isAssignableFrom(clazz)) getInstance(clazz) as T else null
    }
}