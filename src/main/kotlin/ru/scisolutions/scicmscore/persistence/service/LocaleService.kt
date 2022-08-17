package ru.scisolutions.scicmscore.persistence.service

interface LocaleService {
    fun existsByName(name: String): Boolean
}