package ru.scisolutions.scicmscore.engine.service

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class DefaultIdGenerator {
    fun generateId(): String =
        UUID.randomUUID().toString()
}