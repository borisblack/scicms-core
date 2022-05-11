package ru.scisolutions.scicmscore.service

import ru.scisolutions.scicmscore.persistence.entity.Sequence

interface SequenceService {
    fun getByName(name: String): Sequence

    fun nextByName(name: String): String
}