package ru.scisolutions.scicmscore.service

interface ClassService {
    fun getClass(className: String): Class<*>

    fun <T> getInstance(clazz: Class<T>): T

    fun <T> getCastInstance(className: String?, targetClass: Class<T>): T?
}