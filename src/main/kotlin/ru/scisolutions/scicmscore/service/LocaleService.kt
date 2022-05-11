package ru.scisolutions.scicmscore.service

interface LocaleService {
    fun existsByName(name: String): Boolean
}