package ru.scisolutions.scicmscore.persistence.service

interface ClassService {
    fun getClass(className: String): Class<*>

    fun <T> getInstance(clazz: Class<T>): T

    fun <T> getCastInstance(className: String?, castType: Class<T>): T?
}