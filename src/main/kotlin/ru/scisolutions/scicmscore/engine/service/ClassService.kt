package ru.scisolutions.scicmscore.engine.service

interface ClassService {
    fun <T> getInstance(clazz: Class<T>): T

    fun <T> getCastInstance(className: String?, castType: Class<T>): T?
}